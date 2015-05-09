package org.intellimate.izou.security;

/**
 * The PermissionManager handles all permission conflicts within Izou. For example, if two addOns want to play music at
 * the same time, the PermissionManager will interfere and decide who gets the play the music. The PermissionManager
 * has nothing to do with general system security, it is only there to avoid "collisions" between addOns. If you are
 * looking for system security, look at the {@link SecurityManager}.
 */
public final class PermissionManager {
    private static boolean exists = false;
    private final AudioPermissionModule audioPermissionModule;
    private final SocketPermissionModule socketPermissionModule;

    /**
     * Creates an PermissionManager. There can only be one single PermissionManager, so calling this method twice
     * will cause an illegal access exception.
     *
     * @return an PermissionManager
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    static PermissionManager createPermissionManager() throws IllegalAccessException {
        if (!exists) {
            PermissionManager permissionManager = new PermissionManager();
            exists = true;
            return permissionManager;
        }

        throw new IllegalAccessException("Cannot create more than one instance of PermissionManager");
    }

    /**
     * Creates a new PermissionManager instance if and only if none has been created yet
     *
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    private PermissionManager() throws IllegalAccessException {
        if (exists) {
            throw new IllegalAccessException("Cannot create more than one instance of PermissionManager");
        }

        audioPermissionModule = new AudioPermissionModule();
        socketPermissionModule = new SocketPermissionModule();
    }

    /**
     * Gets the AudioPermissionModule
     *
     * @return the AudioPermissionModule
     */
    public AudioPermissionModule getAudioPermissionModule() {
        return audioPermissionModule;
    }

    /**
     * Gets the SocketPermissionModule
     *
     * @return the SocketPermissionModule
     */
    public SocketPermissionModule getSocketPermissionModule() {
        return socketPermissionModule;
    }
}
