package intellimate.izou.fullplugintesting;

import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.system.Context;
import intellimate.izou.events.Event;
import intellimate.izou.resource.Resource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("SameParameterValue")
public class TestCG extends ContentGenerator {

    public TestCG(Context context) {
        super(TestCG.class.getCanonicalName(), context);
    }

    /**
     * this method is called to register what resources the object provides.
     * just pass a List of Resources without Data in it.
     *
     * @return a List containing the resources the object provides
     */
    @Override
    public List<Resource> announceResources() {
        return Arrays.asList(new Resource("test_ID"));
    }

    /**
     * this method is called to register for what Events it wants to provide Resources.
     *
     * @return a List containing ID's for the Events
     */
    @Override
    public List<String> announceEvents() {
        return Arrays.asList("1");
    }

    /**
     * this method is called when an object wants to get a Resource.
     * it has as an argument resource instances without data, which just need to get populated.
     *
     * @param resources a list of resources without data
     * @param event     if an event caused the action, it gets passed. It can also be null.
     * @return a list of resources with data
     */
    @Override
    public List<Resource> provideResource(List<Resource> resources, Optional<Event> event) {
        System.out.println("2");
        Resource<String> resource = new Resource<>("test_ID");
        resource.setResource("IT WORKS1111!!!!!!!!!!!");
        return Arrays.asList(resource);
    }
}
