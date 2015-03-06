package intellimate.izou.contentgenerator;

import intellimate.izou.resource.ResourceBuilder;
import intellimate.izou.threadpool.ExceptionCallback;

/**
 * The Task of an ContentGenerator is to generate a Resources-Object when a Event it subscribed to was fired.
 * <p>
 *     When an Event this ContentGenerator subscribed to was fired, the ContentGeneratorManager will run the instance
 *     of it in a ThreadPool and generate(String eventID) will be called.
 * </p>
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface ContentGenerator extends ResourceBuilder, ExceptionCallback {
}
