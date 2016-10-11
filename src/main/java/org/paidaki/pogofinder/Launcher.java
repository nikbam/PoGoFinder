package org.paidaki.pogofinder;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.paidaki.pogofinder.util.threading.ThreadManager;
import org.paidaki.pogofinder.web.Browser;

public class Launcher extends Application {

    private static final Image[] APP_ICONS = new Image[]{
            new Image(Launcher.class.getResourceAsStream("/app/icons/app_icon_16x16.png")),
            new Image(Launcher.class.getResourceAsStream("/app/icons/app_icon_24x24.png")),
            new Image(Launcher.class.getResourceAsStream("/app/icons/app_icon_32x32.png")),
            new Image(Launcher.class.getResourceAsStream("/app/icons/app_icon_48x48.png")),
            new Image(Launcher.class.getResourceAsStream("/app/icons/app_icon_64x64.png")),
            new Image(Launcher.class.getResourceAsStream("/app/icons/app_icon_72x72.png")),
            new Image(Launcher.class.getResourceAsStream("/app/icons/app_icon_96x96.png")),
            new Image(Launcher.class.getResourceAsStream("/app/icons/app_icon_128x128.png"))
    };

    @Override
    public void start(Stage stage) {
        stage.setTitle("PoGoFinder - Find Pokémon all around you!");
        Scene scene = new Scene(new Browser(), 1200, 800, Color.web("#666970"));
        stage.getIcons().addAll(APP_ICONS);
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event -> ThreadManager.shutdownAllThreads());
    }

    public static void main(String args[]) {
        launch(args);
    }
}
