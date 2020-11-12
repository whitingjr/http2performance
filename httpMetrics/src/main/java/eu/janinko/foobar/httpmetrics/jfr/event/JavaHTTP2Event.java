package eu.janinko.foobar.httpmetrics.jfr.event;

import jdk.jfr.Event;
import jdk.jfr.Category;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Description;
import jdk.jfr.StackTrace;

@Name (JavaHTTP2Event.NAME)
@Label (JavaHTTP2Event.NAME + " events")
@Category ({"Java Application", "Java", "HTTP2", "Requests"})
@Description ("Java HTTP2 invocation")
@StackTrace(false)
public class JavaHTTP2Event extends Event
{
    static final String NAME = "JavaHTTP2";
    public static final ThreadLocal<JavaHTTP2Event> EVENT =
        new ThreadLocal<JavaHTTP2Event>() {
            @Override protected JavaHTTP2Event initialValue() {
                return new JavaHTTP2Event();
            }
        };
}
