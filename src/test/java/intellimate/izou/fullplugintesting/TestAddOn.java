package intellimate.izou.fullplugintesting;

import intellimate.izou.activator.Activator;
import intellimate.izou.addon.AddOn;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.events.EventController;
import intellimate.izou.events.EventManager;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;

/**
 * Created by julianbrendl on 11/20/14.
 */
public class TestAddOn extends AddOn {

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
        contentGenerators[0] = new TestCG(EventManager.FULL_WELCOME_EVENT, getContext());
        return contentGenerators;
    }

    @Override
    public EventController[] registerEventController() {
        return new EventController[0];
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
}
