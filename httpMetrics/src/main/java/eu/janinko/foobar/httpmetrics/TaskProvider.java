package eu.janinko.foobar.httpmetrics;

import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author jbrazdil
 */
public interface TaskProvider {

    String getName();

    List<Callable<Long>> getTasks();

}
