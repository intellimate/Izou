package intellimate.izou.fullplugintesting;

import intellimate.izou.events.Event;
import intellimate.izou.output.OutputExtensionimpl;
import intellimate.izou.system.Context;
import intellimate.izou.resource.Resource;

import java.util.stream.Collectors;

/**
 * Created by julianbrendl on 10/7/14.
 */
@SuppressWarnings("SameParameterValue")
public class TestOE extends OutputExtensionimpl<TestOD> {

    public TestOE(String id, Context context) {
        super(id, context);
        setPluginId("test-OP");
        addResourceIdToWishList("test_ID");
    }

    /**
     * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
     * to the outputPlugin
     *
     * @param event
     */
    @Override
    public TestOD generate(Event event) {
        System.out.println("3");
        String finalOutput = "";
        finalOutput = event.getListResourceContainer().provideResource("test_ID").stream()
                .map(Resource::getResource)
                .filter(object -> object instanceof String)
                .map(object -> (String) object)
                .collect(Collectors.joining());
        return new TestOD(finalOutput);
    }
}

