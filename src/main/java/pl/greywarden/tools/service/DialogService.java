package pl.greywarden.tools.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.stereotype.Service;

@Service
public class DialogService {
    public void bringAlertTop(Stage owner, Alert alert) {
        var stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setAlwaysOnTop(true);
        Platform.runLater(stage::requestFocus);
    }
}
