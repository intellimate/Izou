package karlskrone.jarvis.output;

import karlskrone.jarvis.contentgenerator.ContentData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * OutputManager manages all output plugins and is the main class anyone outside the output package should talk to.
 * It can register/remove new output-plugins and add/delete output-extensions
 *
 * Created by Julian Brendl on 9/27/14.
 */
public class OutputManager {
    /**
     * a list that contains all the registered output-plugins of Jarvis
     */
    private List<OutputPlugin> outputPluginsList;

    /**
     * Creates a new output-manager with a list of output-plugins
     */
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
        for(OutputPlugin oPlug: outputPluginsList) {
            if(oPlug.getId().equals(outputPluginId)) {
                oPlug.addOutputExtension(outputExtension);
                break;
            }
        }
    }

    /**
     * removes the outputplugin of id: id from outputPluginList
     *
     * @param id the id of the output-plugin to remove
     */
    public void removeOutputExtension(String id) {
        for(OutputPlugin oPlug: outputPluginsList) {
            if(oPlug.getId().equals(id)) {
                outputPluginsList.remove(oPlug);
                break;
            }
        }
    }

    /**
     * gets a list of ContentData's and sends it to the right outputPlugin for further processsing
     *
     * passDataToOutputPlugin is the main method of OutputManger. It is called whenever the output process has to be started
     *
     * @param dataList list filled with content-data objects. ContentData holds the output of the DataGenerator Package
     */
    public void passDataToOutputPlugin(List<ContentData> dataList, String outputPluginId) {
        OutputPlugin outputPlugin = null;
        for(int i = 0; i < outputPluginsList.size(); i++) {
            if(outputPluginsList.get(i).getId().equals(outputPluginId)) {
                outputPlugin = outputPluginsList.get(i);
                outputPlugin.setContentDataList(dataList);
                break;
            }
        }
        if(outputPlugin != null)
            outputPlugin.run();
    }
}
