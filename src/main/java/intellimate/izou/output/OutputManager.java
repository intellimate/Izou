package intellimate.izou.output;

import intellimate.izou.contentgenerator.ContentData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * OutputManager manages all output plugins and is the main class anyone outside the output package should talk to.
 * It can register/remove new output-plugins and add/delete output-extensions
 */
public class OutputManager {

    /**
     * a list that contains all the registered output-plugins of Jarvis
     */
    private List<OutputPlugin> outputPluginsList;

    /**
     * responsible for running output-plugins in different threads
     */
    private final ExecutorService executor;

    /**
     * hashmap that stores the future objects of the output-plugins
     */
    private HashMap<String, Future> futureHashMap;

    /**
     * a HashMap that stores all outputExtensions which were to be added to a still non-existent output-plugins,
     * this HashMap gets checked every time a new output-plugin is added for relevant output-extensions
     */
    private HashMap<String, List<OutputExtension>> tempExtensionStorage;

    /**
     * Creates a new output-manager with a list of output-plugins
     */
    public OutputManager() {
        outputPluginsList = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
        futureHashMap = new HashMap<>();
        tempExtensionStorage = new HashMap<>();
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
     * adds outputPlugin to outputPluginList, starts a new thread for the outputPlugin, and stores the future object in a HashMap
     * @param outputPlugin OutputPlugin to add
     */
    public void addOutputPlugin(OutputPlugin outputPlugin) {
        if (!futureHashMap.containsKey(outputPlugin.getId())) {
            outputPlugin.setExecutor(executor);
            outputPluginsList.add(outputPlugin);
            futureHashMap.put(outputPlugin.getId(), executor.submit(outputPlugin));
        } else {
            if (futureHashMap.get(outputPlugin.getId()).isDone()) {
                outputPlugin.setExecutor(executor);
                futureHashMap.remove(outputPlugin.getId());
                futureHashMap.put(outputPlugin.getId(), executor.submit(outputPlugin));
            }
        }

        if (tempExtensionStorage.containsKey(outputPlugin.getId())) {
            for(OutputExtension oE: tempExtensionStorage.get(outputPlugin.getId())) {
                outputPlugin.addOutputExtension(oE);
            }
            tempExtensionStorage.remove(outputPlugin.getId());
        }
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
     * removes the output-plugin of id: pluginId from outputPluginList and ends the thread
     *
     * @param pluginId the id of the output-plugin to remove
     */
    public void removeOutputPlugin(String pluginId) {
        for(OutputPlugin oPlug: outputPluginsList) {
            if(oPlug.getId().equals(pluginId)) {
                outputPluginsList.remove(oPlug);
                oPlug.setExecutor(null);
                futureHashMap.get(oPlug.getId()).cancel(true);
                futureHashMap.remove(oPlug.getId());
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
        boolean found = false;
        for (OutputPlugin oPlug: outputPluginsList) {
            if (oPlug.getId().equals(outputPluginId)) {
                oPlug.addOutputExtension(outputExtension);
                outputExtension.setPluginId(oPlug.getId());
                found = true;
                break;
            }
        }
        if (!found) {
            if(tempExtensionStorage.containsKey(outputPluginId)) {
                tempExtensionStorage.get(outputPluginId).add(outputExtension);
            }
            else {
                List<OutputExtension> outputExtensionList = new ArrayList<>();
                outputExtensionList.add(outputExtension);
                tempExtensionStorage.put(outputPluginId, outputExtensionList);
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
        for(OutputPlugin outputPlugin: outputPluginsList) {
                outputPlugin.addContentDataList(dataList);
        }
    }
}
