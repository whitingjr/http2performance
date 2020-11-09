package eu.janinko.foobar.httpmetrics;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClients;

/**
 *
 * @author jbrazdil
 */
public class Appache2TaskProvider implements TaskProvider {

    private final BasicHttpClientResponseHandler basicResponseHandler = new BasicHttpClientResponseHandler();
    private final HttpClient httpClient = HttpClients.createDefault();
    private final List<HttpUriRequest> requests;

    public Appache2TaskProvider(List<String> urls) {
        this.requests = urls.stream().map(HttpGet::new).collect(Collectors.toList());
    }

    @Override
    public List<Callable<Long>> getTasks() {
        return requests.stream().map(r -> (Callable<Long>) () -> testHttp(r)).collect(Collectors.toList());
    }

    private Long testHttp(HttpUriRequest request) throws URISyntaxException, IOException, InterruptedException {
        long start = System.nanoTime();
        String httpResponse = httpClient.execute(request, basicResponseHandler);
        long stop = System.nanoTime();
        return stop - start;
    }

    @Override
    public String getName() {
        return "Apache HTTP/2";
    }
}
