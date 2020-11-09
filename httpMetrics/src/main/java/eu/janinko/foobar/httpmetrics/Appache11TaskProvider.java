package eu.janinko.foobar.httpmetrics;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author jbrazdil
 */
public class Appache11TaskProvider implements TaskProvider {

    private final BasicResponseHandler basicResponseHandler = new BasicResponseHandler();
    private final HttpClient httpClient = HttpClients.createDefault();
    private final List<HttpUriRequest> requests;

    public Appache11TaskProvider(List<String> urls) {
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
        return "Apache HTTP/1.1";
    }
}
