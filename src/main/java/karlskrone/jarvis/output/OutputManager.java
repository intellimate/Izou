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

    private static OutputManager oMStatic;

    /**
     * Creates a new output-manager with a list of output-plugins
     */
    public OutputManager() {
        outputPluginsList = new ArrayList<>();
        oMStatic = this;
    }

    /**
     * returns the list containing all the registered outputPlugins of the output-manager
     *
     * @return list containing all the registered outputPlugins
     */
    public List<OutputPlugin> getOutputPluginsList() {
        return outputPluginsList;
    }

    /**
     * adds outputPlugin to outputPluginList
     */
    public void addOutputPlugin(OutputPlugin outputPlugin) {
        outputPluginsList.add(outputPlugin);
    }

    /**
     * method to get outputManager instance anywhere in the project
     * @return current instance of outputManager
     */
    public static OutputManager getoMStatic() {
        return oMStatic;
    }

    public OutputPlugin getOutputPlugin(String id) {
        for (OutputPlugin oP: outputPluginsList) {
            if (oP.getId().equals(id)) {
                return oP;
            }
        }
        return null;
    }

    /**
     * removes the output-plugin of id: pluginId from outputPluginList
     *
     * @param pluginId the id of the output-plugin to remove
     */
    public void removeOutputPlugin(String pluginId) {
        for(OutputPlugin oPlug: outputPluginsList) {
            if(oPlug.getId().equals(pluginId)) {
                outputPluginsList.remove(oPlug);
                break;
            }
        }
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
     * removes the output-extension of id: extensionId from outputPluginList
     *
     * @param pluginId the id of the output-plugin in which the output-extension should be removed
     * @param extensionId the id of output-extension to be removed
     */
    public void removeOutputExtension(String pluginId, String extensionId) {
        for(OutputPlugin oPlug: outputPluginsList) {
            if(oPlug.getId().equals(pluginId)) {
                oPlug.removeOutputExtension(extensionId);
                break;
            }
        }
    }

    /**
     * gets a list of ContentData's and sends it to the right outputPlugin for further processsing
     *
     * passDataToOutputPlugins is the main method of OutputManger. It is called whenever the output process has to be started
     *
     * @param dataList list filled with content-data objects. ContentData holds the output of the DataGenerator Package
     */
    public void passDataToOutputPlugins(List<ContentData> dataList) {
        /*
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
            */
    }
}
