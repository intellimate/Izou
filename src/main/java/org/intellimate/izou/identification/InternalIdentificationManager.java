package org.intellimate.izou.identification;

import org.intellimate.izou.util.IzouModule;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import ro.fortsoft.pf4j.IzouPluginClassLoader;

/**
 * the internal IdentificationManager provides various methods handle Identifiables/Identifications
 * @author LeanderK
 * @version 1.0
 */
public class InternalIdentificationManager extends IzouModule {

    public InternalIdentificationManager(Main main) {
        super(main);
    }

    /**
     * gets the AddonModel for the Identification, or null if none found
     * @param identification the Identification
     * @return an AddonModel or null
     */
    public AddOnModel getAddonModel(Identification identification) {
        Identifiable identifiable = identification.getIdentifiable();
        if (identifiable.getClass().getClassLoader() instanceof IzouPluginClassLoader && !identifiable.getClass().getName().toLowerCase()
                .contains(IzouPluginClassLoader.PLUGIN_PACKAGE_PREFIX_IZOU_SDK)) {
            return getMain().getAddOnInformationManager().getAddOnForClassLoader(identifiable.getClass().getClassLoader())
                    .orElse(null);
        }
        return null;
    }
}
