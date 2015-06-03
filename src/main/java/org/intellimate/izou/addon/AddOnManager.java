package org.intellimate.izou.addon;

import org.apache.logging.log4j.Level;
import org.intellimate.izou.AddonThreadPoolUser;
import org.intellimate.izou.IdentifiableSet;
import org.intellimate.izou.IzouModule;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.SecurityModule;
import org.intellimate.izou.system.Context;
import org.intellimate.izou.system.context.ContextImplementation;
import org.intellimate.izou.system.file.FileSystemManager;
import ro.fortsoft.pf4j.*;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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
public class AddOnManager extends IzouModule implements AddonThreadPoolUser {
    private IdentifiableSet<AddOnModel> addOns = new IdentifiableSet<>();
    private HashMap<AddOnModel, PluginWrapper> pluginWrappers = new HashMap<>();
    private List<URL> aspectsOrAffected = new ArrayList<>();
    
    public AddOnManager(Main main) {
        super(main);
    }

    /**
    * retrieves and registers all AddOns.
    */
    public void retrieveAndRegisterAddOns() {
        addOns.addAll(loadAddOns());
        registerAllAddOns(addOns);
    }

    /**
     * Adds AddOns without registering them.
     * @param addOns a List containing all the AddOns
     */
    public void addAddOnsWithoutRegistering(List<AddOnModel> addOns) {
        this.addOns.addAll(addOns);
    }

    /**
     * registers all AddOns.
     *
     * @param addOns a List containing all the AddOns
     */
    public void addAndRegisterAddOns(List<AddOnModel> addOns) {
        this.addOns.addAll(addOns);
        registerAllAddOns(this.addOns);
    }
    
    public void registerAllAddOns(IdentifiableSet<AddOnModel> addOns) {
        initAddOns(addOns);
        List<CompletableFuture<Void>> futures = addOns.stream()
                .map(addOn -> submit((Runnable) addOn::register))
                .collect(Collectors.toList());
        try {
            timeOut(futures, 3000);
        } catch (InterruptedException e) {
            debug("interrupted while trying to mite out the addOns", e);
        }
    }

    private void initAddOns(IdentifiableSet<AddOnModel> addOns) {
        addOns.forEach(addOn -> {
            Context context = new ContextImplementation(addOn, main, Level.DEBUG.name());
            submit(() -> addOn.initAddOn(context));
        });
    }

    /**
     * This method searches all the "/lib"-directory for AddOns and adds them to the addOnList
     * @return the retrieved addOns
     */
    private List<AddOnModel> loadAddOns() {
        debug("searching for addons in: " + getMain().getFileSystemManager().getLibLocation());
        PluginManager pluginManager = new DefaultPluginManager(getMain().getFileSystemManager().getLibLocation(), aspectsOrAffected);
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
                    });
            keyManager.saveAddOnKeys();
            return addOns;
        } catch (Exception e) {
            log.fatal("Error while trying to start the AddOns", e);
            return new ArrayList<>();
        }
    }

    /**
     * returns the addOn loaded from the ClassLoader
     * @param classLoader the classLoader
     * @return the (optional) AddOnModel
     */
    public Optional<AddOnModel> getAddOnForClassLoader(ClassLoader classLoader) {
        return addOns.stream()
                .filter(addOnModel -> addOnModel.getClass().getClassLoader().equals(classLoader))
                .findFirst();
    }

    /**
     * returns the (optional) PluginWrapper for the AddonModel.
     * If the return is empty, it means that the AddOn was not loaded through pf4j
     * @param addOnModel the AddOnModel
     * @return the PluginWrapper if loaded through pf4j or empty if added as an argument
     */
    public Optional<PluginWrapper> getPluginWrapper(AddOnModel addOnModel) {
        return Optional.of(pluginWrappers.get(addOnModel));
    }

    /**
     * checks whether the AddOn was loaded through pf4j
     * @param addOnModel the AddOnModel to check
     * @return true if loaded, false if not
     */
    public boolean loadedThroughPF4J (AddOnModel addOnModel) {
        return pluginWrappers.get(addOnModel) != null;
    }

    /**
     * adds an aspect-or an affected class url to the list. Must be done before loading of the addons!
     * @param url the url to add.
     */
    public void addAspectOrAffectedURL(URL url) {
        aspectsOrAffected.add(url);
    }

    private class KeyManager {
        private HashMap<String, SecretKey> addOnKeys;
        boolean changed;

        private KeyManager() {
            addOnKeys = new HashMap<>();
            retrieveAddonKeys();
        }

        private void manageAddOnKey(PluginDescriptor descriptor) {
            SecretKey secretKey = addOnKeys.get(descriptor.getPluginId());

            if (secretKey == null) {
                SecurityModule module = new SecurityModule();
                secretKey = module.generateKey();
                addOnKeys.put(descriptor.getPluginId(), secretKey);
                changed = true;
            }

            descriptor.setSecureID(secretKey);
        }

        private void retrieveAddonKeys() {
            changed = false;

            try {
                String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;
                final String keyStoreFile = workingDir + File.separator + "system" + File.separator + "izou.keystore";
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

        private void saveAddOnKeys() {
            if (!changed) {
                return;
            }

            String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;
            final String keyStoreFile = workingDir + File.separator + "system" + File.separator + "addon_keys.keystore";
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
         * Creates a new keystore for the izou aes key
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
