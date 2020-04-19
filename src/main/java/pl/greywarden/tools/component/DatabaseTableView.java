package pl.greywarden.tools.component;

import javafx.collections.ObservableMap;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class DatabaseTableView extends TableView<ObservableMap<String, Object>> {
    public DatabaseTableView() {
        super.setPlaceholder(new Label());
        super.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }
}
