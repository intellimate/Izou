package intellimate.izou.system;

import intellimate.izou.main.Main;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Class to init JavaFX ToolKit - should be ignored
 */
public class JavaFXInitializer extends Application {

    /**
     * No param constructor needed by {@link javafx.application.Application}
     */
    public JavaFXInitializer() {
    }

    /**
     * Initializes the sound engine by starting the JavaFX ToolKit
     */
    public static void initToolKit() {
        Thread t = new Thread(Application::launch);
        t.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.jfxToolKitInit.set(true);
    }
}
