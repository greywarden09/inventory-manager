package pl.greywarden.tools;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.greywarden.tools.configuration.MainWindowConfiguration;
import pl.greywarden.tools.controller.MainWindowController;

@SpringBootApplication
public class InventoryManager extends Application {
    private MainWindowConfiguration mainWindowConfiguration;
    private MainWindowController mainWindowController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        var springContext = SpringApplication.run(InventoryManager.class);
        mainWindowConfiguration = springContext.getBean("mainWindowConfiguration", MainWindowConfiguration.class);
        mainWindowController = springContext.getBean(MainWindowController.class);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = mainWindowConfiguration.mainWindow();

        primaryStage.setTitle("Inventory Manager");
        primaryStage.setMaximized(mainWindowConfiguration.isMaximized());
        primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> mainWindowConfiguration.setMaximized(newValue));
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> mainWindowController.exit());
        primaryStage.show();
    }

}
