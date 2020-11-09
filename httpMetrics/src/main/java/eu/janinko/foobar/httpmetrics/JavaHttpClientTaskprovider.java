package eu.janinko.foobar.httpmetrics;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

    private Long testHttp(HttpRequest request) throws URISyntaxException, IOException, InterruptedException {
        long start = System.nanoTime();
        HttpResponse<byte[]> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        long stop = System.nanoTime();
        return stop - start;
    }
}
