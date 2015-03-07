package intellimate.izouSDK.contentgenerator;

import intellimate.izou.resource.ResourceBuilder;

/**
 * The Task of an ContentGenerator is to generate a Resources-Object when a Event it subscribed to was fired.
 * <p>
 *     When an Event this ContentGenerator subscribed to was fired, the ContentGeneratorManager will run the instance
 *     of it in a ThreadPool and generate(String eventID) will be called.
 * </p>
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface ContentGenerator extends ResourceBuilder {

    /**
     * This method ensures that each content generator has its own event id so that it can be triggered by activators.
     *
     * @param description the description of the event id
     * @param eventIDName the event id name
     * @param eventID the actual event id
     */
    public void setContentID(String description, String eventIDName, String eventID);
}
