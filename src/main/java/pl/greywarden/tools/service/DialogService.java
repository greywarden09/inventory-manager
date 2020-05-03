package pl.greywarden.tools.service;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class DialogService {
    private final ResourceBundle resourceBundle;

    public void bringAlertTop(Stage owner, Alert alert) {
        var stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setAlwaysOnTop(true);
        Platform.runLater(stage::requestFocus);
    }

    public void showWarningDialog(Stage parent, String messageKey, String titleKey, Object... args) {
        var formatter = new MessageFormat("");
        formatter.applyPattern(resourceBundle.getString(messageKey));
        var errorMessage = formatter.format(args);
        var alert = new Alert(Alert.AlertType.WARNING, errorMessage);
        alert.setHeaderText(null);
        alert.setTitle(resourceBundle.getString(titleKey));
        bringAlertTop(parent, alert);
        alert.show();
    }

    public void showConfirmationDialog(Stage parent, String messageKey, String titleKey, Consumer<ButtonType> handler, Object... args) {
        var formatter = new MessageFormat("");
        formatter.applyPattern(resourceBundle.getString(messageKey));
        var message = formatter.format(args);
        var alert = new Alert(Alert.AlertType.CONFIRMATION, null, ButtonType.YES, ButtonType.NO);
        alert.getDialogPane().setContent(new Label(message));
        alert.setTitle(resourceBundle.getString(titleKey));
        alert.setHeaderText(null);
        alert.setGraphic(null);

        bringAlertTop(parent, alert);
        alert.showAndWait().ifPresent(handler);
    }
}
