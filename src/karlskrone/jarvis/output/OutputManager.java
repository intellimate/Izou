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
    public void addOutputExtension(OutputExtension outputExtension, String outputPluginId) throws InterruptedException {
        OutputPlugin outputPlugin;
        for(int i = 0; i < outputPluginsList.size(); i++) {
            if(outputPluginsList.get(i).getId().equals(outputPluginId)) {
                outputPlugin = outputPluginsList.get(i);

                //loop tries to add 3 times in case of failure, then it prints out the exception
                int addingTries = 0;
                while(addingTries < 3) {
                    try {
                        outputPlugin.addOutputExtension(outputExtension);
                        addingTries = 4; //quit the loop since it added successfully
                    } catch(IOException ioExcep) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException inExcep) {
                            System.out.println(inExcep.toString());
                        }

                        addingTries++;
                        if(addingTries >= 3) {
                            System.out.println(ioExcep.toString());
                        }
                    }
                }
                break;
            }
        }
    }

    public void passDataToOutputPlugin(Object data) {

    }
}
