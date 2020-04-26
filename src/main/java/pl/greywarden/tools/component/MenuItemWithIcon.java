package pl.greywarden.tools.component;

import javafx.scene.control.MenuItem;
import org.kordamp.ikonli.javafx.FontIcon;

public class MenuItemWithIcon extends MenuItem {
    private final FontIcon icon = new FontIcon();

    public MenuItemWithIcon() {
        super();
        super.setGraphic(icon);
    }

    public void setIcon(String iconLiteral) {
        icon.setIconLiteral(iconLiteral);
    }

    public String getIcon() {
        return icon.getIconLiteral();
    }
}
