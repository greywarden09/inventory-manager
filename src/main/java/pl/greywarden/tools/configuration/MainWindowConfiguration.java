package pl.greywarden.tools.configuration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.ResourceBundle;

@Configuration
@RequiredArgsConstructor
public class MainWindowConfiguration {
    private final ConfigurableApplicationContext springContext;
    private final ResourceBundle resourceBundle;

    @Value("classpath:css/application.css")
    private Resource applicationCss;

    @Value("classpath:fxml/main-window.fxml")
    private Resource mainWindowFxml;

    @SneakyThrows
    public Scene mainWindow() {
        var fxmlLoader = new FXMLLoader();

        fxmlLoader.setControllerFactory(springContext::getBean);
        fxmlLoader.setResources(resourceBundle);
        fxmlLoader.setLocation(mainWindowFxml.getURL());

        var rootNode = fxmlLoader.load();

        var scene = new Scene((Parent) rootNode);
        scene.getStylesheets().add(applicationCss.getURL().toExternalForm());

        return scene;
    }
}
