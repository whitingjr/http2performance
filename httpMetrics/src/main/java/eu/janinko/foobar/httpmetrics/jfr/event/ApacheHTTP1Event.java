package eu.janinko.foobar.httpmetrics.jfr.event;

import jdk.jfr.Event;
import jdk.jfr.Category;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Description;
import jdk.jfr.StackTrace;

@Name (ApacheHTTP1Event.NAME)
@Label (ApacheHTTP1Event.NAME + " events")
@Category ({"Java Application", "Apache", "HTTP1", "Requests"})
@Description ("Apache HTTP1 invocation")
@StackTrace(false)
public class ApacheHTTP1Event extends Event
{
    static final String NAME = "ApacheHTTP1";
    public static final ThreadLocal<ApacheHTTP1Event> EVENT =
        new ThreadLocal<ApacheHTTP1Event>() {
            @Override protected ApacheHTTP1Event initialValue() {
                return new ApacheHTTP1Event();
            }
        };
}
