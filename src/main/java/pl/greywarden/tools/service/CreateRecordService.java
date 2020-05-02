package pl.greywarden.tools.service;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import pl.greywarden.tools.listener.EventListener;
import pl.greywarden.tools.model.event.request.CreateDatabaseRecordRequest;
import pl.greywarden.tools.model.event.response.CreateDatabaseRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@EventListener
@RequiredArgsConstructor
public class CreateRecordService {
    private final ConfigurableApplicationContext springContext;
    private final ResourceBundle resourceBundle;

    public static final String TEXT_FIELD_CLASS = ".text-field";

    @Value("classpath:css/application.css")
    private Resource applicationCss;

    @Subscribe
    public void showCreateRecordDialog(CreateDatabaseRecordRequest createDatabaseRecordRequest) throws Exception {
        var columns = createDatabaseRecordRequest.getColumns();
        var database = createDatabaseRecordRequest.getDatabase();

        var eventBus = springContext.getBean(EventBus.class);

        var dialog = new Dialog<ObservableMap<String, Object>>();
        var graphic = new FontIcon("mdi-database-plus");
        dialog.setTitle(resourceBundle.getString("create-record.title"));
        dialog.setHeaderText(resourceBundle.getString("create-record.header"));
        graphic.setIconSize(64);
        dialog.setGraphic(graphic);

        var createButtonType = new ButtonType(resourceBundle.getString("create-record.create"), ButtonBar.ButtonData.OK_DONE);
        var cancelButtonType = new ButtonType(resourceBundle.getString("create-record.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(createButtonType, cancelButtonType);

        var grid = new GridPane();
        grid.setVgap(10.0);
        grid.setHgap(10.0);
        grid.setPadding(new Insets(20, 150, 10, 10));

        dialog.getDialogPane().getScene().getStylesheets().add(applicationCss.getURL().toExternalForm());
        var validationSupport = new ValidationSupport();
        var decor = new StyleClassValidationDecoration("error", null);
        validationSupport.setValidationDecorator(decor);

        var rows = 0;
        var inputs = new ArrayList<Node>();

        for (var column : columns) {
            var name = column.getName();
            var type = column.getType();

            var label = new Label(name);
            switch (type) {
                case ID:
                    var wrapper = createIdTextField(name, database, validationSupport);
                    var idTextField = wrapper.lookup(TEXT_FIELD_CLASS);
                    inputs.add(idTextField);
                    grid.addRow(rows++, label, wrapper);
                    break;
                case TEXT:
                    var textArea = createTextArea(name);
                    inputs.add(textArea);
                    grid.addRow(rows++, label, textArea);
                    break;
                case NUMBER:
                    var numberTextField = createNumberTextField(name, validationSupport);
                    inputs.add(numberTextField);
                    grid.addRow(rows++, label, numberTextField);
                    break;
                case BOOLEAN:
                    var checkBox = createCheckBox(name);
                    inputs.add(checkBox);
                    grid.addRow(rows++, label, checkBox);
                    break;
            }
        }
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().lookupButton(createButtonType).disableProperty().bind(validationSupport.invalidProperty());
        dialog.setResultConverter(dialogButton -> dialogButton.equals(createButtonType) ? getResult(inputs) : null);
        Platform.runLater(() -> inputs.get(0).requestFocus());

        dialog.showAndWait()
                .map(CreateDatabaseRecord::new)
                .ifPresent(eventBus::post);
    }

    private CheckBox createCheckBox(String name) {
        var checkBox = new CheckBox();
        checkBox.setId(name);
        return checkBox;
    }

    private TextField createNumberTextField(String name, ValidationSupport validationSupport) {
        var textField = new TextField();
        textField.setText("0");
        validationSupport.registerValidator(textField, Validator.createPredicateValidator(NumberUtils::isParsable, null));
        textField.setId(name);
        return textField;
    }

    private TextArea createTextArea(String name) {
        var textArea = new TextArea();
        textArea.setId(name);
        return textArea;
    }

    private HBox createIdTextField(String columnName, ObservableList<ObservableMap<String, Object>> database, ValidationSupport validationSupport) {
        var wrapper = new HBox();
        wrapper.setSpacing(5.0);
        var textField = new TextField();
        HBox.setHgrow(textField, Priority.ALWAYS);
        var generateIdButton = new Button(resourceBundle.getString("create-record.generate-id"));
        validationSupport.registerValidator(textField, Validator.<String>createPredicateValidator(input ->
                database
                        .stream()
                        .map(entry -> entry.get(columnName).toString())
                        .noneMatch(id -> id.equals(input)), null));
        textField.setId(columnName);
        generateIdButton.setOnAction(event -> textField.setText(UUID.randomUUID().toString()));

        wrapper.getChildren().addAll(textField, generateIdButton);
        return wrapper;
    }

    private ObservableMap<String, Object> getResult(List<Node> inputs) {
        final var result = inputs
                .stream()
                .collect(Collectors.toMap(Node::getId, this::getNodeValue));
        return FXCollections.observableMap(result);
    }

    private Object getNodeValue(Node node) {
        if (node instanceof TextField) {
            return ((TextField) node).getText();
        }
        if (node instanceof TextArea) {
            return ((TextArea) node).getText();
        }
        if (node instanceof CheckBox) {
            return ((CheckBox) node).isSelected();
        }
        throw new UnsupportedOperationException("Node type " + node.getClass() + " is unsupported");
    }
}
