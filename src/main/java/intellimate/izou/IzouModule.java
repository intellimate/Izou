package intellimate.izou;

import intellimate.izou.main.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
abstract class IzouModule implements MainProvider {
    protected Main main;
    protected final Logger fileLogger = LogManager.getLogger(this.getClass());

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
