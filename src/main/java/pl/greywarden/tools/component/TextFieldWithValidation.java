package pl.greywarden.tools.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Tooltip;
import org.controlsfx.control.textfield.CustomTextField;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Function;

public class TextFieldWithValidation extends CustomTextField {
    private final FontIcon errorMark = new FontIcon("gmi-error-outline");
    private final BooleanProperty validProperty = new SimpleBooleanProperty(false);
    private final Tooltip errorMessageTooltip = new Tooltip();

    public TextFieldWithValidation() {
        super();

        errorMark.setIconSize(16);
        errorMark.setVisible(false);
        setRight(errorMark);
    }

    public void validate(Function<String, Boolean> validator, String errorMessage) {
        var isValid = validator.apply(getText());
        if (isValid) {
            validProperty.set(true);
            errorMark.setVisible(false);
            getStyleClass().remove("error");
            setTooltip(null);
        } else {
            validProperty.set(false);
            errorMark.setVisible(true);
            if (!getStyleClass().contains("error")) {
                getStyleClass().add("error");
            }
            errorMessageTooltip.setText(errorMessage);
            setTooltip(errorMessageTooltip);
        }
    }

    public void invalidate() {
        errorMark.setVisible(false);
        validProperty.set(false);
        setTooltip(null);
    }

    public BooleanProperty validProperty() {
        return validProperty;
    }
}
