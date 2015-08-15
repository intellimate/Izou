package org.intellimate.izou.internal.identification;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.internal.main.Main;
import org.intellimate.izou.internal.util.IzouModule;
import ro.fortsoft.pf4j.IzouPluginClassLoader;

import java.util.Optional;

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
     * @param identificationImpl the Identification
     * @return an AddonModel or null
     */
    public AddOnModel getAddonModel(IdentificationImpl identificationImpl) {
        Identifiable identifiable = identificationImpl.getIdentifiable();
        if (identifiable.getClass().getClassLoader() instanceof IzouPluginClassLoader && !identifiable.getClass().getName().toLowerCase()
                .contains(IzouPluginClassLoader.PLUGIN_PACKAGE_PREFIX_IZOU_SDK)) {
            return getMain().getAddOnManager().getAddOnForClassLoader(identifiable.getClass().getClassLoader())
                    .orElse(null);
        }
        return null;
    }

    /**
     * gets the AddonModel for the Identification, or null if none found
     * @param identification the Identification
     * @return an AddonModel or null
     */
    public Optional<AddOnModel> getAddonModel(Identification identification) {
        if (identification instanceof IdentificationImpl) {
            AddOnModel addonModel = getAddonModel((IdentificationImpl) identification);
            if (addonModel != null)
                return Optional.of(addonModel);
        }
        return Optional.empty();
    }

    /**
     * returns true if the Identification is valid, false if not
     *
     * @param identification the Identification to test
     * @return true if valid, false if nit
     */
    public boolean verify(org.intellimate.izou.identification.Identification identification) {
        return identification instanceof IdentificationImpl;
    }
}
