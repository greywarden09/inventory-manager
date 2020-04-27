package pl.greywarden.tools.configuration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import pl.greywarden.tools.service.ApplicationSettingsService;

import java.util.Locale;
import java.util.ResourceBundle;

@Configuration
@RequiredArgsConstructor
public class MainWindowConfiguration {
    private final ConfigurableApplicationContext springContext;
    private final ApplicationSettingsService applicationSettingsService;

    @SneakyThrows
    public Scene mainWindow() {
        var fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(springContext::getBean);
        fxmlLoader.setResources(ResourceBundle.getBundle("i18n/strings", Locale.getDefault()));
        fxmlLoader.setLocation(getClass().getResource("/fxml/main-window.fxml"));

        var rootNode = fxmlLoader.load();

        Scene scene = new Scene((Parent) rootNode);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("css/application.css").toExternalForm());

        return scene;
    }

    public boolean isMaximized() {
        return applicationSettingsService.getBoolean("main-window.maximized", true);
    }

    public double getPrefWidth() {
        return applicationSettingsService.getDouble("main-window.pref-width", 800.0);
    }

    public double getPrefHeight() {
        return applicationSettingsService.getDouble("main-window.pref-height", 600.0);
    }

    public void setMaximized(boolean maximized) {
        applicationSettingsService.setProperty("main-window.maximized", maximized);
    }

    public void setPrefWidth(double width) {
        applicationSettingsService.setProperty("main-window.pref-width", width);
    }

    public void setPrefHeight(double height) {
        applicationSettingsService.setProperty("main-window.pref-height", height);
    }
}
