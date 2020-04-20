package pl.greywarden.tools.component;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;

public class PasswordInputDialog extends Dialog<String> {
    private final PasswordField passwordField = new PasswordField();

    public PasswordInputDialog(String contentText) {
        super();
        super.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        setContentText(contentText);
        initComponent();
    }

    private void initComponent() {
        var content = new HBox();
        content.setSpacing(10);
        content.getChildren().addAll(new Label(getContentText()), passwordField);
        getDialogPane().setContent(content);
        setResultConverter(dialogButton -> dialogButton == ButtonType.OK ? passwordField.getText() : null);
    }
}
