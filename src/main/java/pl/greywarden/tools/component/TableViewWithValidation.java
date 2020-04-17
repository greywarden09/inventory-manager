package pl.greywarden.tools.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

import java.util.function.Function;

public class TableViewWithValidation<T> extends TableView<T> {
    private final BooleanProperty validProperty = new SimpleBooleanProperty(false);
    private final Tooltip errorMessageTooltip = new Tooltip();

    public void validate(Function<ObservableList<T>, Boolean> validator, String errorMessage) {
        var isValid = validator.apply(getItems());
        if (isValid) {
            validProperty.set(true);
            getStyleClass().remove("error");
            setTooltip(null);
        } else {
            validProperty.set(false);
            if (!getStyleClass().contains("error")) {
                getStyleClass().add("error");
            }
            errorMessageTooltip.setText(errorMessage);
            setTooltip(errorMessageTooltip);
        }
    }

    public void invalidate() {
        validProperty.set(false);
        setTooltip(null);
    }

    public BooleanProperty validProperty() {
        return validProperty;
    }
}
