package eu.janinko.foobar.httpmetrics;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import eu.janinko.foobar.httpmetrics.jfr.event.ApacheHTTP2Event;
import eu.janinko.foobar.httpmetrics.jfr.event.HttpImplementationEvent;
import jdk.jfr.Event;

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

    private Long testHttp(HttpUriRequest request) throws Exception {
        return time ( () -> httpClient.execute(request, basicResponseHandler), getRequestEvent() );
    }

    @Override
    public String getName() {
        return "Apache HTTP/2";
    }
    
    @Override
    public Event getRequestEvent()
    {
        return ApacheHTTP2Event.EVENT.get();
    }

}
