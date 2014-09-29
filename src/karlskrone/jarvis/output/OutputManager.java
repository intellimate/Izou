package karlskrone.jarvis.output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Julian Brendl on 9/27/14.
 */
public class OutputManager {
    private List<OutputPlugin> outputPluginsList;

    public OutputManager() {
        outputPluginsList = new ArrayList<>();
    }

    /**
     * adds output extension to desired outputPlugin
     *
     * adds output extension to desired outputPlugin, so that the output-plugin can start and stop the outputExtension
     * task as needed. The outputExtension is specific to the output-plugin
     *
     * @param outputExtension the outputExtension to be added
     * @param outputPluginId the output-plugin the outputExtension is to be added to
     */
    public void addOutputExtension(OutputExtension outputExtension, String outputPluginId) {
        OutputPlugin outputPlugin;
        for(int i = 0; i < outputPluginsList.size(); i++) {
            if(outputPluginsList.get(i).getId().equals(outputPluginId)) {
                outputPlugin = outputPluginsList.get(i);
                outputPlugin.addOutputExtension(outputExtension);
                break;
            }
        }
    }

    public void passDataToOutputPlugin(Object data) {

    }
}
