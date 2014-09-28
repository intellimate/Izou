package karlskrone.jarvis.output;

import karlskrone.jarvis.communication.Pipe;

import java.io.IOException;
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
     * a List containing all the pipes for intra-thread communication, so that OutputPlugin can communicate with its output-extensions
     */
    private List<Pipe> pipeList;

    /**
     * responsible for running output-extensions in different threads
     */
    private final ExecutorService executor;


    public OutputPlugin(String id) {
        this.id = id;
        outputExtensionList = new ArrayList<>();
        pipeList = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
    }

    /**
     * Gets the id of the outputPlugin
     * @return id of the outputPlugin
     */
    public String getId() {
        return this.id;
    }

    public void addOutputExtension(OutputExtension outputExtension) throws IOException{
        outputExtensionList.add(outputExtension);
        try {
            Pipe pipe = new Pipe(outputExtension.getId());
            pipeList.add(pipe);
        }
        catch(IOException excep) {
            throw excep;
        }
    }

    /**
     * removes output-extensions from outputExtensionList and pipe from pipeList
     *
     * removes output-extensions from outputExtensionList, outputExtensionList is an ArrayList that stores all
     * outputExtensions of all OutputPlugins, and pipe is the way outputPlugin communicated with outputExtension
     *
     * @param id the id of the output extension to be removed
     */
    public void removeOutputExtension(String id) {
        //removes outputExtension itself
        int index = 0;
        for(int i = 0; i < outputExtensionList.size(); i++) {
            if(outputExtensionList.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        outputExtensionList.remove(index);

        //removes the pipe for outputExtension
        int indexPipe = 0;
        for(int i = 0; i < pipeList.size(); i++) {
            if(pipeList.get(i).getId().equals(id)) {
                indexPipe = i;
                break;
            }
        }
        pipeList.remove(indexPipe);
    }

    @Override
    public void run() {
        for(OutputExtension ext: outputExtensionList)
            executor.submit(ext);
    }
}
