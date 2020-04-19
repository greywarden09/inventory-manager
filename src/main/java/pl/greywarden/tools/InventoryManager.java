package pl.greywarden.tools;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.greywarden.tools.configuration.MainWindowConfiguration;

@SpringBootApplication
public class InventoryManager extends Application {
    private MainWindowConfiguration mainWindowConfiguration;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        var springContext = SpringApplication.run(InventoryManager.class);
        mainWindowConfiguration = springContext.getBean("mainWindowConfiguration", MainWindowConfiguration.class);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = mainWindowConfiguration.mainWindow();

        primaryStage.setTitle("Inventory Manager");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
