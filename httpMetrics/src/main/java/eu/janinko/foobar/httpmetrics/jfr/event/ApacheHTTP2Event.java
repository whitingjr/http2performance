package eu.janinko.foobar.httpmetrics.jfr.event;

import jdk.jfr.Event;
import jdk.jfr.Category;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Description;
import jdk.jfr.StackTrace;

@Name (ApacheHTTP2Event.NAME)
@Label (ApacheHTTP2Event.NAME + " events")
@Category ({"Java Application", "Apache", "HTTP2", "Requests"})
@Description ("Apache HTTP2 invocation")
@StackTrace(false)
public class ApacheHTTP2Event extends Event
{
    static final String NAME = "ApacheHTTP2";
    public static final ThreadLocal<ApacheHTTP2Event> EVENT =
        new ThreadLocal<ApacheHTTP2Event>() {
            @Override protected ApacheHTTP2Event initialValue() {
                return new ApacheHTTP2Event();
            }
        };
}
