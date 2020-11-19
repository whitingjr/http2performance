package eu.janinko.foobar.httpmetrics;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import eu.janinko.foobar.httpmetrics.jfr.event.JavaHTTP1Event;
import eu.janinko.foobar.httpmetrics.jfr.event.JavaHTTP2Event;
import jdk.jfr.Event;

/**
 *
 * @author jbrazdil
 */
public class JavaHttpClientTaskprovider implements TaskProvider {

    private final HttpClient httpClient;
    private final List<HttpRequest> requests;
    private static final String DIR = "/tmp/www/html/";
    private static final Logger logger = Logger.getLogger(JavaHttpClientTaskprovider.class.getName());

    public JavaHttpClientTaskprovider(List<String> urls, HttpClient.Version version) {
        this.httpClient = HttpClient.newBuilder().version(version).build();
        this.requests = urls.stream().map(l -> {
            try {
                return HttpRequest.newBuilder().uri(new URI(l)).GET().build();
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        switch (httpClient.version()) {
            case HTTP_1_1:
                return "Java HTTP/1.1";
            case HTTP_2:
                return "Java HTTP/2";
            default:
                throw new IllegalArgumentException("Unknown version");
        }
    }

    @Override
    public List<Callable<Long>> getTasks() {
        return requests.stream().map(r -> (Callable<Long>) () -> testHttp(r)).collect(Collectors.toList());
    }

    @Override
    public Event getRequestEvent()
    {
        switch (httpClient.version()) {
        case HTTP_1_1:
            return JavaHTTP1Event.EVENT.get();
        case HTTP_2:
            return JavaHTTP2Event.EVENT.get();
        default:
            throw new IllegalArgumentException("Unknown version");
        }
    }

    private Long testHttp(HttpRequest request) throws Exception {
        return time( () -> {
           HttpResponse<byte[]> body = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
//        httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
/*           URI uri = request.uri();
           byte[] b = body.body();
           Path p = Paths.get(uri.getPath());
           File parent = new File(DIR, p.getParent().toString());
           parent.mkdirs();
           try (OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(parent, p.getFileName().toString())) ) ) {
               os.write(b);
           }
*/
           if (body.statusCode() != 200) logger.severe("Failed http call "+ request.uri().toString());
           return null;
        }
        , getRequestEvent() );
    }

}
