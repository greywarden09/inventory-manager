package pl.greywarden.tools.configuration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;
import java.util.ResourceBundle;

@Configuration
@RequiredArgsConstructor
public class MainWindowConfiguration {
    private final ConfigurableApplicationContext springContext;

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
}
