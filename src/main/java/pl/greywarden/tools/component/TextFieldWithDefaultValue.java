package pl.greywarden.tools.component;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.controlsfx.control.textfield.CustomTextField;

public class TextFieldWithDefaultValue extends CustomTextField {
    private final ObjectProperty<String> defaultValue = new SimpleObjectProperty<>(null);

    public TextFieldWithDefaultValue() {
        super();
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue.setValue(defaultValue);
    }

    public String getDefaultValue() {
        return this.defaultValue.getValue();
    }

}
