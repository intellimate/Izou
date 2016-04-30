package org.intellimate.izou.addon;

import org.apache.commons.cli.MissingArgumentException;
import org.intellimate.izou.identification.Identifiable;

/**
 * This class holds all public information of an addOn. That means all information that other addOns are allowed to know
 * about a given addOn is contained in here. Each addOn has its own instance of AddOnInformation which are stored in
 * {@link AddOnInformationManager}.
 */
public final class AddOnInformation implements Identifiable {
    private final String name;
    private final String version;
    private final String provider;
    private final String id;

    /**
     * Creates a new instance of AddOnInformation, which holds all the public information of an addOn and is registered
     * in the {@link AddOnInformationManager}
     *
     * @param name The name of the AddOn.
     * @param version The version of the AddOn.
     * @param provider The author of the AddOn.
     * @param id The unique ID of the AddOn.
     *
     * @throws MissingArgumentException Thrown if the config file of an addon is not complete, in other words if
     * an argument is missing in the file
     */
    public AddOnInformation(String name, String provider, String version, String id) throws MissingArgumentException {
        if (!checkField(name) || !checkField(provider) || !checkField(version) || !checkField(id)) {
            throw new MissingArgumentException("AddOnInformation is not complete - an argument is missing");
        }

        this.name = name;
        this.version = version;
        this.provider = provider;
        this.id = id;
    }

    /**
     * Gets the name of the addOn.
     *
     * @return The name of the addOn.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the version of the addOn.
     *
     * @return The version of the addOn.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the provider, or the author, of the addOn.
     *
     * @return the author of the addOn.
     */
    public String getProvider() {
        return provider;
    }

    @Override
    public String getID() {
        return id;
    }

    /**
     * Checks whether the given string is filled out or not.
     *
     * @param field The string that should not be null.
     * @return True if the string is not null or empty or has a value of 'null' and false otherwise.
     */
    private boolean checkField(String field) {
        return field != null && !field.equals("") && !field.toLowerCase().equals("null");
    }
}
