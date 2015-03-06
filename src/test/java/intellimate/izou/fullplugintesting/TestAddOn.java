package intellimate.izou.fullplugintesting;

import intellimate.izou.activator.Activator;
import intellimate.izouSDK.addon.AddOnImpl;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.events.EventsController;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;

/**
 * Created by julianbrendl on 11/20/14.
 */
public class TestAddOn extends AddOnImpl {

    public TestAddOn(String addOnID) {
        super(addOnID);
    }

    @Override
    public void prepare() {

    }

    @Override
    public Activator[] registerActivator() {
        Activator[] activators = new Activator[1];
        activators[0] = new TestAct(getContext());
        return activators;
    }

    @Override
    public ContentGenerator[] registerContentGenerator() {
        ContentGenerator[] contentGenerators = new ContentGenerator[1];
        contentGenerators[0] = new TestCG(getContext());
        return contentGenerators;
    }

    /**
     * use this method to register (if needed) your EventControllers.
     *
     * @return Array containing Instances of EventControllers
     */
    @Override
    public EventsController[] registerEventController() {
        return new EventsController[0];
    }

    @Override
    public OutputPlugin[] registerOutputPlugin() {
        OutputPlugin[] outputPlugins = new OutputPlugin[1];
        outputPlugins[0] = new TestOP("test-OP", getContext());
        return outputPlugins;
    }

    @Override
    public OutputExtension[] registerOutputExtension() {
        OutputExtension[] outputExtensions= new OutputExtension[1];
        outputExtensions[0] = new TestOE("test-OE", getContext());
        return outputExtensions;
    }

    /**
     * An ID must always be unique.
     * A Class like Activator or OutputPlugin can just provide their .class.getCanonicalName()
     * If you have to implement this interface multiple times, just concatenate unique Strings to
     * .class.getCanonicalName()
     *
     * @return A String containing an ID
     */
    @Override
    public String getID() {
        return TestAddOn.class.getCanonicalName();
    }
}
