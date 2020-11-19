package eu.janinko.foobar.httpmetrics;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.frame.RawFrame;
import org.apache.hc.core5.http2.impl.nio.H2StreamListener;
import org.apache.hc.core5.http2.impl.nio.bootstrap.H2RequesterBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;

import eu.janinko.foobar.httpmetrics.jfr.event.ApacheHTTP2Event;
import jdk.jfr.Event;

/**
 *
 * @author jbrazdil
 */
public class Appache2TaskProvider implements TaskProvider {

    private static final Logger logger = Logger.getLogger(Appache2TaskProvider.class.getName());

    final H2Config config = H2Config.custom().setPushEnabled(false).build();
    final HttpAsyncRequester requester = H2RequesterBootstrap.bootstrap()
            .setH2Config(config)
            .setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2)
            .setStreamListener(new H2StreamListener() {
                
                @Override
                public void onOutputFlowControl(HttpConnection connection, int streamId, int delta, int actualSize) {
                }
                @Override
                public void onInputFlowControl(HttpConnection connection, int streamId, int delta, int actualSize) {
                }
                @Override
                public void onHeaderOutput(HttpConnection connection, int streamId, List<? extends Header> headers) {
                }
                @Override
                public void onHeaderInput(HttpConnection connection, int streamId, List<? extends Header> headers) {
                }
                @Override
                public void onFrameOutput(HttpConnection connection, int streamId, RawFrame frame) {
                }
                @Override
                public void onFrameInput(HttpConnection connection, int streamId, RawFrame frame) {
                }
            })
            .create();
//    private final List<BasicRequestProducer> requests;
    private final List<String> requests;
    private HttpHost targetHost;

    public Appache2TaskProvider(List<String> urls) {
//        targetHost = new HttpHost("http", "localhost", 80);
        targetHost = new HttpHost("localhost");
//        this.requests = urls.stream().map((u) -> {
//            BasicHttpRequest basicRequest = new BasicHttpRequest(Method.GET.name(), targetHost, u);
//            basicRequest.setScheme("http");
//            BasicRequestProducer r = new BasicRequestProducer(basicRequest, null);
//            return r;
//            }).collect(Collectors.toList());
        this.requests = urls;
        Runtime.getRuntime().addShutdownHook(new ApacheProviderShutdown(requester));
        requester.start();
    }

    @Override
    public List<Callable<Long>> getTasks() {
        return requests.stream().map(r -> (Callable<Long>) () -> testHttp(r)).collect(Collectors.toList());
    }

//    private Long testHttp(BasicRequestProducer request) throws Exception {
    private Long testHttp(String uri) throws Exception {
        return time ( () -> {
               final Future<AsyncClientEndpoint> future = requester.connect(targetHost, Timeout.ofSeconds(10));
               CountDownLatch ended = new CountDownLatch(1);
               final AsyncClientEndpoint endpoint = future.get(10l, TimeUnit.SECONDS);
               endpoint.execute(
                       new BasicRequestProducer(Method.GET, targetHost, uri),
                       new BasicResponseConsumer<>(new StringAsyncEntityConsumer()),
                       new FutureCallback<Message<HttpResponse, String>>() {
                           @Override
                           public void completed(Message<HttpResponse, String> result) {
                               endpoint.releaseAndReuse();
                               ended.countDown();
                           }
                           @Override
                           public void failed(Exception ex) {
                               endpoint.releaseAndReuse();
                               ended.countDown();
                               logger.severe("Failed request. exception message: " + ex.getMessage());
                           }
                           @Override
                           public void cancelled() {
                               endpoint.releaseAndReuse();
                               ended.countDown();
                           }
                       }
                    );
               return ended;
            }, getRequestEvent()
         );
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

    class ApacheProviderShutdown extends Thread {
        @Override
        public void run() {
            this.requester.close(CloseMode.GRACEFUL);
        }
        final HttpAsyncRequester requester;
        public ApacheProviderShutdown(HttpAsyncRequester requester) {
            this.requester = requester;
        }
    }
}
