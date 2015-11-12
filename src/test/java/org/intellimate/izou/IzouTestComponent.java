package org.intellimate.izou;

import org.intellimate.izou.activator.ActivatorManager;
import org.intellimate.izou.addon.AddOnManager;
import org.intellimate.izou.events.EventDistributor;
import org.intellimate.izou.events.LocalEventManager;
import org.intellimate.izou.identification.InternalIdentificationManager;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.output.OutputManager;
import org.intellimate.izou.resource.ResourceManager;
import org.intellimate.izou.security.SecurityManager;
import org.intellimate.izou.support.SystemMail;
import org.intellimate.izou.system.file.FileManager;
import org.intellimate.izou.system.file.FilePublisher;
import org.intellimate.izou.system.file.FileSystemManager;
import org.intellimate.izou.system.sound.SoundManager;
import org.intellimate.izou.threadpool.ThreadPoolManager;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is class mocks the org.intellimate.izou.main class of izou with all its components and returns the mocked object.
 * It can be used in test that require the org.intellimate.izou.main class.
 */
public class IzouTestComponent {
    private Main main;

    /**
     * Creates a new IzouTestComponent
     */
    public IzouTestComponent() {
        main = createMain();
    }

    /**
     * Creates and returns a new mocked main object with real components
     *
     * @return a new mocked main object
     */
    private Main createMain() {
        Main main = mock(Main.class);
        when(main.getActivatorManager()).thenReturn(new ActivatorManager(main));
        when(main.getAddOnManager()).thenReturn(new AddOnManager(main));
        when(main.getEventDistributor()).thenReturn(new EventDistributor(main));
        when(main.getFilePublisher()).thenReturn(new FilePublisher(main));
        when(main.getFileSystemManager()).thenReturn(new FileSystemManager(main));
        when(main.getInternalIdentificationManager()).thenReturn(new InternalIdentificationManager(main));
        when(main.getLocalEventManager()).thenReturn(new LocalEventManager(main));
        when(main.getOutputManager()).thenReturn(new OutputManager(main));
        when(main.getResourceManager()).thenReturn(new ResourceManager(main));
        when(main.getSoundManager()).thenReturn(new SoundManager(main));
        when(main.getThreadPoolManager()).thenReturn(new ThreadPoolManager(main));

        try {
            when(main.getSecurityManager()).thenReturn(SecurityManager
                    .createSecurityManager(SystemMail.createSystemMail(), main));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            when(main.getFileManager()).thenReturn(new FileManager(main));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return main;
    }

    /**
     * Gets the current mocked main object
     *
     * @return the current mocked main object
     */
    public Main getMain() {
        return main;
    }

    /**
     * Resets the current mocked main object to the "factory state"
     */
    public void resetMain() {
        main = createMain();
    }
}
