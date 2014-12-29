package intellimate.izou.addon;

import ro.fortsoft.pf4j.DefaultExtensionFinder;
import ro.fortsoft.pf4j.ExtensionFactory;
import ro.fortsoft.pf4j.ExtensionWrapper;
import ro.fortsoft.pf4j.PluginManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation for ExtensionFinder. All extensions declared in a plugin are indexed in a file
 * "META-INF/extensions.idx". This class lookup extensions in all extensions index files "META-INF/extensions.idx".
 *
 * @author LeanderK
 * @version 1.0
 */
public class IzouExtensionFinder extends DefaultExtensionFinder {
    public IzouExtensionFinder(PluginManager pluginManager, ExtensionFactory extensionFactory) {
        super(pluginManager, extensionFactory);
    }

    @Override
    public <T> List<ExtensionWrapper<T>> find(Class<T> type) {
        List<ExtensionWrapper<T>>  extensionWrappers = super.find(type);

        //evil hack
        //hammertime
        //remove duplicates created by read readClassPathIndex
        //the first duplicate gets removed
        List<ExtensionWrapper<T>>  filteredExtensionWrappers = new ArrayList<>();
        for (int i = 0; i < extensionWrappers.size(); i++) {
            ExtensionWrapper<T> extensionWrapper = extensionWrappers.get(i);
            URL url =extensionWrapper.getDescriptor().getExtensionClass().getProtectionDomain().getCodeSource()
                    .getLocation();
            boolean found = false;
            for (int j = i + 1; j < extensionWrappers.size(); j++) {
                ExtensionWrapper<T> otherExtensionWrapper = extensionWrappers.get(j);
                URL otherUrl =otherExtensionWrapper.getDescriptor().getExtensionClass().getProtectionDomain()
                        .getCodeSource().getLocation();
                if (url.sameFile(otherUrl))
                    found = true;
            }
            if(!found)
                filteredExtensionWrappers.add(extensionWrapper);
        }
        return filteredExtensionWrappers;
    }
}
