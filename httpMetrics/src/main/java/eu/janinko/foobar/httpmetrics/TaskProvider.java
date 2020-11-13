package eu.janinko.foobar.httpmetrics;

import java.util.List;
import java.util.concurrent.Callable;

import eu.janinko.foobar.httpmetrics.jfr.event.HttpImplementationEvent;
import jdk.jfr.Event;

/**
 *
 * @author jbrazdil
 */
public interface TaskProvider {

    String getName();

    List<Callable<Long>> getTasks();

    Event getRequestEvent();

    default public Event getProviderEvent()
    {
        HttpImplementationEvent e = HttpImplementationEvent.EVENT.get();
        e.type = this.getClass().getName();
        return e;
    }

    default Long time( Callable<?> c, final Event e) 
            throws Exception {
        final boolean isEnabled = e.isEnabled();
        if (isEnabled) {
            e.begin();
        }
        try {
            long start = System.nanoTime();
            c.call();
            long stop = System.nanoTime();
            return stop - start;
        } finally {
            if (isEnabled) {
                e.end();
                if (e.shouldCommit()) {
                    e.commit();
                }
            }
        }
    }
}
