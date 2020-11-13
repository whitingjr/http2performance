package eu.janinko.foobar.httpmetrics.jfr.event;

import jdk.jfr.Event;
import jdk.jfr.Category;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Description;
import jdk.jfr.StackTrace;

@Name (HttpImplementationEvent.NAME)
@Label (HttpImplementationEvent.NAME + " events")
@Category ({"Java Application", HttpImplementationEvent.NAME})
@Description ("Http Implementation Event")
@StackTrace(false)
public class HttpImplementationEvent extends Event
{
    static final String NAME = "HttpImplementation";
    public static final ThreadLocal<HttpImplementationEvent> EVENT =
        new ThreadLocal<HttpImplementationEvent>() {
            @Override protected HttpImplementationEvent initialValue() {
                return new HttpImplementationEvent();
            }
        };

    @Label("Type")
    public String type;
}
