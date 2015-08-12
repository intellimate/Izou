package org.intellimate.izou.internal.identification;

import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.internal.util.IzouModule;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.internal.main.Main;
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
    public AddOnModel getAddonModel(IdentificationImpl identification) {
        Identifiable identifiable = identification.getIdentifiable();
        if (identifiable.getClass().getClassLoader() instanceof IzouPluginClassLoader && !identifiable.getClass().getName().toLowerCase()
                .contains(IzouPluginClassLoader.PLUGIN_PACKAGE_PREFIX_IZOU_SDK)) {
            return getMain().getAddOnManager().getAddOnForClassLoader(identifiable.getClass().getClassLoader())
                    .orElse(null);
        }
        return null;
    }

    /**
     * returns true if the Identification is valid, false if not
     *
     * @param identification the Identification to test
     * @return true if valid, false if nit
     */
    public boolean verify(Identification identification) {
        return identification instanceof IdentificationImpl;
    }
}