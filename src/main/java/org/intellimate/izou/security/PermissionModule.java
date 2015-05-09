package org.intellimate.izou.security;

import java.security.Permission;

/**
 * Created by julianbrendl on 5/9/15.
 */
public abstract class PermissionModule extends SecurityModule {
    public abstract boolean checkPermission(Permission permission);
}
