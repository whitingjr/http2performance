package eu.janinko.foobar.httpmetrics.jfr.event;

import jdk.jfr.Event;
import jdk.jfr.Category;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Description;
import jdk.jfr.StackTrace;

@Name (JavaHTTP1Event.NAME)
@Label (JavaHTTP1Event.NAME + " events")
@Category ({"Java Application", "Java", "HTTP1", "Requests"})
@Description ("Java HTTP1 invocation")
@StackTrace(false)
public class JavaHTTP1Event extends Event
{
    static final String NAME = "JavaHTTP1";
    public static final ThreadLocal<JavaHTTP1Event> EVENT =
        new ThreadLocal<JavaHTTP1Event>() {
            @Override protected JavaHTTP1Event initialValue() {
                return new JavaHTTP1Event();
            }
        };
}
