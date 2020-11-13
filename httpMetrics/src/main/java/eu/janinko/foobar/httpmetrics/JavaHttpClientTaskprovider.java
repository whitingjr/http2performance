package eu.janinko.foobar.httpmetrics;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.Callable;
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
        return time( () -> httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray()), getRequestEvent() );
    }

}
