package pl.greywarden.tools.controller;

import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import pl.greywarden.tools.listener.EventListener;
import pl.greywarden.tools.model.CreateDatabaseRequest;

@Controller
@RequiredArgsConstructor
public class MainWindowController implements EventListener<CreateDatabaseRequest> {
    private final ConfigurableApplicationContext springContext;

    @FXML
    public void exit() {
        Platform.exit();
    }

    @SneakyThrows
    public void createNewDatabase() {
        var createDatabaseDialog = springContext.getBean("createDatabaseDialog", Stage.class);
        if (!createDatabaseDialog.isShowing()) {
            createDatabaseDialog.show();
            createDatabaseDialog.requestFocus();
        }
    }

    @Override
    @Subscribe
    public void handleEvent(CreateDatabaseRequest event) {
        System.out.println("Received event: " + event);
    }
}
