package pl.greywarden.tools;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pl.greywarden.tools.configuration.MainWindowConfiguration;
import pl.greywarden.tools.controller.MainWindowController;
import pl.greywarden.tools.service.ApplicationSettingsService;

@SpringBootApplication
public class InventoryManager extends Application {
    private ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        springContext = SpringApplication.run(InventoryManager.class);
    }

    @Override
    public void start(Stage primaryStage) {
        var mainWindowConfiguration = springContext.getBean(MainWindowConfiguration.class);
        var applicationSettingsService = springContext.getBean(ApplicationSettingsService.class);
        var mainWindowController = springContext.getBean(MainWindowController.class);

        var scene = mainWindowConfiguration.mainWindow();

        primaryStage.setTitle("Inventory Manager");
        primaryStage.setMaximized(applicationSettingsService.isMaximized());
        primaryStage.maximizedProperty().addListener(observable -> applicationSettingsService.setMaximized(((ReadOnlyBooleanProperty) observable).get()));
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> mainWindowController.exit());
        primaryStage.show();
    }

}
