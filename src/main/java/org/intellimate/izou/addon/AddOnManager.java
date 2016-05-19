package org.intellimate.izou.addon;

import org.apache.logging.log4j.Level;
import org.intellimate.izou.identification.AddOnInformationManager;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.SecurityFunctions;
import org.intellimate.izou.security.storage.SecureStorageImpl;
import org.intellimate.izou.system.Context;
import org.intellimate.izou.system.context.ContextImplementation;
import org.intellimate.izou.util.AddonThreadPoolUser;
import org.intellimate.izou.util.IdentifiableSet;
import org.intellimate.izou.util.IzouModule;
import ro.fortsoft.pf4j.*;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Manages all the AddOns.
 */
//TODO isolate pf4j calls & catch errors accordingly
public class AddOnManager extends IzouModule implements AddonThreadPoolUser {
    private IdentifiableSet<AddOnModel> addOns = new IdentifiableSet<>();
    private HashMap<AddOnModel, PluginWrapper> pluginWrappers = new HashMap<>();
    private Set<AspectOrAffected> aspectOrAffectedSet = new HashSet<>();
    private List<Runnable> initializedCallback = new ArrayList<>();
    private AddOnInformationManager addOnInformationManager;

    /**
     * Creates a new instance of the AddOnManager
     *
     * @param main The main instance
     */
    public AddOnManager(Main main) {
        super(main);
        addOnInformationManager = main.getAddOnInformationManager();
    }

    /**
    * Retrieves and registers all AddOns.
    */
    public void retrieveAndRegisterAddOns() {
        addOns.addAll(loadAddOns());
        registerAllAddOns(addOns);
        initialized();
    }

    /**
     * Adds AddOns without registering them.
     *
     * @param addOns a List containing all the AddOns
     */
    public void addAddOnsWithoutRegistering(List<AddOnModel> addOns) {
        this.addOns.addAll(addOns);
    }

    /**
     * Registers all AddOns.
     *
     * @param addOns a List containing all the AddOns
     */
    public void addAndRegisterAddOns(List<AddOnModel> addOns) {
        this.addOns.addAll(addOns);
        registerAllAddOns(this.addOns);
        initialized();
    }

    /**
     * TODO: Missing doc
     *
     * @param addOns
     */
    public void registerAllAddOns(IdentifiableSet<AddOnModel> addOns) {
        initAddOns(addOns);
        createAddOnInfos(addOns);
        List<CompletableFuture<Void>> futures = addOns.stream()
                .map(addOn -> submit((Runnable) addOn::register))
                .collect(Collectors.toList());
        try {
            timeOut(futures, 30000);
        } catch (InterruptedException e) {
            debug("interrupted while trying to time out the addOns", e);
        }
    }

    /**
     * Checks that addOns have all required properties and creating the addOn information list if they do
     */
    private void createAddOnInfos(IdentifiableSet<AddOnModel> addOns) {
        addOns.stream().forEach(addOn -> addOnInformationManager.registerAddOn(addOn));
    }

    /**
     * TODO: Missing doc
     *
     * @param addOns
     */
    private void initAddOns(IdentifiableSet<AddOnModel> addOns) {
        List<CompletableFuture<Void>> futures = addOns.stream()
                .map(addOn -> {
                    Context context = new ContextImplementation(addOn, main, Level.DEBUG.name());
                    return submit(() -> addOn.initAddOn(context));
                })
                .collect(Collectors.toList());
        try {
            timeOut(futures, 30000);
        } catch (InterruptedException e) {
            debug("interrupted while trying to time out the addOns", e);
        }
    }

    /**
     * This method searches all the "/lib"-directory for AddOns and adds them to the addOnList
     *
     * @return the retrieved addOns
     */
    private List<AddOnModel> loadAddOns() {
        debug("searching for addons in: " + getMain().getFileSystemManager().getLibLocation());
        PluginManager pluginManager = new DefaultPluginManager(getMain().getFileSystemManager().getLibLocation(),
                new ArrayList<>(aspectOrAffectedSet));
        // load the plugins
        debug("loading plugins");
        pluginManager.loadPlugins();
        debug("loaded: " + pluginManager.getPlugins().toString());

        // start (active/resolved) the plugins
        try {
            debug("starting plugins");
            pluginManager.startPlugins();
        } catch (Exception | NoClassDefFoundError e) {
            error("Error while trying to start the PF4J-Plugins", e);
        }
        try {
            debug("retrieving addons from the plugins");
            List<AddOnModel> addOns = pluginManager.getExtensions(AddOnModel.class);
            debug("retrieved: " + addOns.toString());
            KeyManager keyManager = new KeyManager();
            addOns.stream()
                    .filter(addOn -> addOn.getClass().getClassLoader() instanceof IzouPluginClassLoader)
                    .forEach(addOn -> {
                        IzouPluginClassLoader izouPluginClassLoader = (IzouPluginClassLoader) addOn.getClass()
                                .getClassLoader();
                        PluginWrapper plugin = pluginManager.getPlugin(izouPluginClassLoader.getPluginDescriptor()
                                .getPluginId());
                        keyManager.manageAddOnKey(plugin.getDescriptor());
                        pluginWrappers.put(addOn, plugin);
                        addOn.setPlugin(plugin);
                    });
            keyManager.saveAddOnKeys();
            return addOns;
        } catch (Exception e) {
            log.fatal("Error while trying to start the AddOns", e);
            return new ArrayList<>();
        }
    }

    /**
     * Returns the (optional) PluginWrapper for the AddonModel.
     * If the return is empty, it means that the AddOn was not loaded through pf4j
     *
     * @param addOnModel the AddOnModel
     * @return the PluginWrapper if loaded through pf4j or empty if added as an argument
     */
    public Optional<PluginWrapper> getPluginWrapper(AddOnModel addOnModel) {
        return Optional.of(pluginWrappers.get(addOnModel));
    }

    /**
     * Checks whether the AddOn was loaded through pf4j
     *
     * @param addOnModel the AddOnModel to check
     * @return true if loaded, false if not
     */
    public boolean loadedThroughPF4J (AddOnModel addOnModel) {
        return pluginWrappers.get(addOnModel) != null;
    }

    /**
     * Adds an aspect-class url to the list. Must be done before loading of the addons!
     *
     * @param aspectOrAffected the aspect or affected to add
     */
    public void addAspectOrAffected(AspectOrAffected aspectOrAffected) {
        if (!aspectOrAffectedSet.add(aspectOrAffected)) {
            error("set is already containing an instance of " + aspectOrAffected);
        }
    }

    /**
     * adds an listener to the initialized state (all addons registered).
     * @param runnable the runnable to add
     */
    public void addInitializedListener(Runnable runnable) {
        initializedCallback.add(runnable);
    }

    /**
     * Called after the addons were initialized
     */
    private void initialized() {
        initializedCallback.forEach(this::submit);
        initializedCallback = new LinkedList<>();
    }

    /**
     * The KeyManager in the AddOnManager loads or creates a {@link SecretKey} for each AddOn at addOn load time,
     * depending if one already exists. It then distributes them to each addOn being loaded, and then finaly saves them
     * again.
     * <p>
     *     This is necessary for the {@link SecureStorageImpl} in order to save
     *     data matching to each addOn. This secret key serves as key to the data of an addOn being saved. In other
     *     words, each addOn data is matched with the secret key instead of the plugin descriptor itself in order to
     *     avoid serialization of the addon descriptor, which would entail a huge mess. So the secret key of each addOn
     *     is pretty much a "signature" of each addOn, easily identifying it.
     * </p>
     */
    private class KeyManager {
        private HashMap<String, SecretKey> addOnKeys;
        boolean changed;

        /**
         * Creates a new KeyManager object
         */
        private KeyManager() {
            addOnKeys = new HashMap<>();
            retrieveAddonKeys();
        }

        /**
         * Check if a SecretKey already exists for the plugin descriptor, if not creates a new one, and then gives it to
         * the plugin descriptor.
         * @param descriptor The plugin descriptor to give a SecretKey
         */
        private void manageAddOnKey(PluginDescriptor descriptor) {
            SecretKey secretKey = addOnKeys.get(descriptor.getPluginId());

            if (secretKey == null) {
                SecurityFunctions module = new SecurityFunctions();
                secretKey = module.generateKey();
                addOnKeys.put(descriptor.getPluginId(), secretKey);
                changed = true;
            }

            descriptor.setSecureID(secretKey);
        }

        /**
         * Retrieves all saved addOnKeys (they cannot change, since there are dependencies on them, so they are saved
         * and retrieved)
         */
        private void retrieveAddonKeys() {
            changed = false;

            try {
                final String keyStoreFile = getMain().getFileSystemManager().getSystemDataLocation() + File.separator
                        + "addon_keys.keystore";
                KeyStore keyStore = createKeyStore(keyStoreFile, "4b[X:+H4CS&avY<)");

                KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection("Ev45j>eP}QTR?K9_"
                        .toCharArray());
                Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    KeyStore.Entry entry = keyStore.getEntry(alias, keyPassword);
                    SecretKey key = ((KeyStore.SecretKeyEntry) entry).getSecretKey();
                    addOnKeys.put(alias, key);
                }
            } catch(NullPointerException e) {
                return;
            } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException e) {
                error("Unable to retrieve key", e);
            }
        }

        /**
         * Save all addOnKeys in the instance variable {@code addOnKeys} in a keystore
         */
        private void saveAddOnKeys() {
            if (!changed) {
                return;
            }
            final String keyStoreFile = getMain().getFileSystemManager().getSystemDataLocation()
                    + File.separator + "addon_keys.keystore";
            KeyStore keyStore = createKeyStore(keyStoreFile, "4b[X:+H4CS&avY<)");

            for (String mapKey : addOnKeys.keySet()) {
                try {
                    KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(addOnKeys.get(mapKey));
                    KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection("Ev45j>eP}QTR?K9_"
                            .toCharArray());
                    keyStore.setEntry(mapKey, keyStoreEntry, keyPassword);
                } catch (KeyStoreException e) {
                    error("Unable to store key", e);
                }
            }

            try {
                keyStore.store(new FileOutputStream(keyStoreFile), "4b[X:+H4CS&avY<)".toCharArray());
            } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
                error("Unable to store key", e);
            }
        }

        /**
         * Creates a new keystore for addOn secret keys
         *
         * @param fileName the path to the keystore
         * @param password the password to use with the keystore
         * @return the newly created keystore
         */
        private KeyStore createKeyStore(String fileName, String password)  {
            File file = new File(fileName);
            KeyStore keyStore = null;
            try {
                keyStore = KeyStore.getInstance("JCEKS");
                if (file.exists()) {
                    keyStore.load(new FileInputStream(file), password.toCharArray());
                } else {
                    keyStore.load(null, null);
                    keyStore.store(new FileOutputStream(fileName), password.toCharArray());
                }
            } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
                error("Unable to create key store", e);
            }

            return keyStore;
        }
    }
}
