package org.intellimate.izou.security;

import org.intellimate.izou.security.exceptions.IzouSocketPermissionException;

/**
 * The SocketPermissionModule stores addOns that registered themselves to use socket connections.
 */
public final class SocketPermissionModule extends PermissionModule {
    @Override
    public boolean checkPermission(String addOnID) throws IzouSocketPermissionException {
        return isRegistered(addOnID);
    }
}