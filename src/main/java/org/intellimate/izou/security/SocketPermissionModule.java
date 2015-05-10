package org.intellimate.izou.security;

import org.intellimate.izou.security.exceptions.IzouSocketPermissionException;

/**
 * The SocketPermissionModule stores addOns that registered themselves to use socket connections.
 */
public final class SocketPermissionModule extends PermissionModule {
    @Override
    public void checkPermission(String addOnID) throws IzouSocketPermissionException {
        if (!isRegistered(addOnID)) {
            throw new IzouSocketPermissionException("Socket Permission Denied: " + addOnID + "is not registered to use "
                    + "socket connections, please add the required information to the addon_config.properties file of "
                    + "your addOn.");
        }
    }
}