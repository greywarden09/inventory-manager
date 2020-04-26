package pl.greywarden.tools.component;

import javafx.scene.control.Button;
import org.kordamp.ikonli.javafx.FontIcon;

public class ButtonWithIcon extends Button {
    private final FontIcon icon = new FontIcon();

    public ButtonWithIcon() {
        super();
        setGraphic(icon);
        icon.setIconSize(24);
    }

    public void setIcon(String iconLiteral) {
        this.icon.setIconLiteral(iconLiteral);
    }

    public String getIcon() {
        return icon.getIconLiteral();
    }
}
