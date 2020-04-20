package pl.greywarden.tools.configuration;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import pl.greywarden.tools.controller.CreateDatabaseController;

import javax.annotation.PostConstruct;
import java.util.Locale;
import java.util.ResourceBundle;

@Order(1)
@Component
@RequiredArgsConstructor
public class CreateDatabaseDialogConfiguration {
    private final ConfigurableApplicationContext springContext;
    private final CreateDatabaseController createDatabaseController;

    @Value("classpath:css/application.css")
    private Resource applicationCss;

    @Value("classpath:fxml/create-database.fxml")
    private Resource createDatabaseFxml;

    @PostConstruct
    @SneakyThrows
    public void createDatabaseDialog() {
        Platform.runLater(() -> springContext.getBeanFactory().registerSingleton("createDatabaseDialog", getStage()));
    }

    @SneakyThrows
    private Stage getStage() {
        var stage = new Stage();
        var fxmlLoader = new FXMLLoader();
        var bundle = ResourceBundle.getBundle("i18n/strings", Locale.getDefault());

        fxmlLoader.setLocation(createDatabaseFxml.getURL());
        fxmlLoader.setControllerFactory(springContext::getBean);
        fxmlLoader.setResources(bundle);
        var root = fxmlLoader.load();

        var scene = new Scene((Parent) root);

        scene.getStylesheets().add(applicationCss.getURL().toExternalForm());

        stage.initStyle(StageStyle.UTILITY);
        stage.setScene(scene);
        stage.setTitle(bundle.getString("create-database.window-title"));
        stage.setOnCloseRequest(event -> {
            createDatabaseController.cancel();
            event.consume();
        });

        return stage;
    }
}
