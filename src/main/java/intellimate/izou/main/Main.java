package intellimate.izou.main;

import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.addon.AddOn;
import intellimate.izou.addon.AddOnManager;
import intellimate.izou.contentgenerator.ContentGeneratorManager;
import intellimate.izou.events.EventManager;
import intellimate.izou.output.OutputManager;
import intellimate.izou.system.FileManager;
import intellimate.izou.system.FileSystemManager;
import intellimate.izou.system.IzouLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Main Class.
 *
 * This is where our journey begins and all the Managers get initialized
 */
@SuppressWarnings("FieldCanBeLocal")
public class Main {
    private final OutputManager outputManager;
    private final EventManager eventManager;
    private final ContentGeneratorManager contentGeneratorManager;
    private final ActivatorManager activatorManager;
    private final AddOnManager addOnManager;
    private final Thread threadEventManager;
    private final FileManager fileManager;
    private final IzouLogger izouLogger;

    private Main() {
        FileSystemManager fileSystemManager = new FileSystemManager();
        try {
            fileSystemManager.createIzouFileSystem();
        } catch (IOException e) {
            e.printStackTrace();
        }

        izouLogger = new IzouLogger();
        outputManager = new OutputManager();
        eventManager = new EventManager(outputManager);
        threadEventManager = new Thread(eventManager);
        threadEventManager.start();
        contentGeneratorManager = new ContentGeneratorManager(eventManager);
        activatorManager = new ActivatorManager(eventManager);
        addOnManager = new AddOnManager(outputManager,eventManager,contentGeneratorManager,activatorManager, this);
        addOnManager.retrieveAndRegisterAddOns();

        FileManager fileManagerTemp;
        try {
            fileManagerTemp = new FileManager();
        } catch (IOException e) {
            fileManagerTemp = null;
            e.printStackTrace();
            //TODO: implement error handling
        }
        fileManager = fileManagerTemp;
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOn> addOns) {
        FileSystemManager fileSystemManager = new FileSystemManager();
        try {
            fileSystemManager.createIzouFileSystem();
        } catch (IOException e) {
            e.printStackTrace();
        }

        izouLogger = new IzouLogger();
        outputManager = new OutputManager();
        eventManager = new EventManager(outputManager);
        threadEventManager = new Thread(eventManager);
        threadEventManager.start();
        contentGeneratorManager = new ContentGeneratorManager(eventManager);
        activatorManager = new ActivatorManager(eventManager);
        addOnManager = new AddOnManager(outputManager,eventManager,contentGeneratorManager,activatorManager, this);
        addOnManager.addAndRegisterAddOns(addOns);

        FileManager fileManagerTemp;
        try {
            fileManagerTemp = new FileManager();
        } catch (IOException e) {
            fileManagerTemp = null;
            e.printStackTrace();
            //TODO: implement error handling
        }
        fileManager = fileManagerTemp;
    }

    public static void main(String[] args) {
        @SuppressWarnings("UnusedAssignment") Main main = new Main();
    }

    public synchronized OutputManager getOutputManager() {
        return outputManager;
    }

    public synchronized EventManager getEventManager() {
        return eventManager;
    }

    public synchronized ContentGeneratorManager getContentGeneratorManager() {
        return contentGeneratorManager;
    }

    public synchronized ActivatorManager getActivatorManager() {
        return activatorManager;
    }

    public synchronized AddOnManager getAddOnManager() {
        return addOnManager;
    }

    public synchronized Thread getThreadEventManager() {
        return threadEventManager;
    }

    public synchronized FileManager getFileManager() {
        return fileManager;
    }

    public synchronized IzouLogger getIzouLogger() {
        return izouLogger;
    }
}
