package intellimate.izou;

import intellimate.izou.main.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The base class for each IzouModule
 * @author Leander Kurscheidt
 * @version 1.0
 */
public abstract class IzouModule implements MainProvider {
    protected Main main;
    protected final Logger log = LogManager.getLogger(this.getClass());

    public IzouModule(Main main) {
        this.main = main;
    }

    /**
     * returns the instance of Main
     *
     * @return Main
     */
    @Override
    public Main getMain() {
        return main;
    }
}
