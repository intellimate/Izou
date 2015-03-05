package intellimate.izou;

import intellimate.izou.main.Main;

/**
 * this interface signals that the class provides an instance of main 
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface MainProvider {
    /**
     * returns the instance of Main
     * @return Main
     */
    public Main getMain();
}
