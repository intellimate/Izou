package intellimate.izou.system;

import intellimate.izou.addon.AddOn;
import intellimate.izou.main.Main;

/**
 * This class provides much of the general Communication with Izou.
 */
public class Context {
    AddOn addOn;
    Main main;
    public Context(AddOn addOn, Main main) {
        this.addOn = addOn;
        this.main = main;
    }
}
