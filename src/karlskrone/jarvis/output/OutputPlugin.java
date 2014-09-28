package karlskrone.jarvis.output;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The OutputPlugin class manages
 *
 * Created by julianbrendl on 9/27/14.
 */
public class OutputPlugin implements Runnable{
    /**
     * id of the of OutputPlugin, it is primarily used by OutputManager to communicate with specific output plugins
     */
    private final String id;
    /**
     * a List containing all the output-extensions the plugin requires to successfully create its final output
     */
    private List<OutputExtension> outputExtensionList;

    /**
     *
     */
    private final ExecutorService executor;


    public OutputPlugin(String id) {
        this.id = id;
        outputExtensionList = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
    }

    public String getId() {
        return this.id;
    }

    public void addOutputExtension(OutputExtension outputExtension) {
        outputExtensionList.add(outputExtension);
    }

    /**
     * removes output-extensions from outputExtensionList
     *
     * removes output-extensions from outputExtensionList, outputExtensionList is an ArrayList that stores all
     * outputExtensions of all OutputPlugins
     *
     * @param id the id of the output extension to be removed
     */
    public void removeOutputExtension(String id) {
        int index = 0;
        for(int i = 0; i < outputExtensionList.size(); i++) {
            if(outputExtensionList.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        outputExtensionList.remove(index);
    }

    @Override
    public void run() {
        for(OutputExtension ext: outputExtensionList)
            executor.submit(ext);
    }
}
