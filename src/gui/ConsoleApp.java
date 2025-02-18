package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConsoleApp extends Application {

    private TextArea console;
    private TextField inputField;

    @Override
    public void start(Stage primaryStage) {
        console = new TextArea();
        console.setEditable(false);
        console.setWrapText(true);

        inputField = new TextField();
        inputField.setPromptText("Enter text here...");

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            String inputText = inputField.getText();
            if (!inputText.isEmpty()) {
                console.appendText(inputText + "\n");
                inputField.clear();
            }
        });

        VBox inputBox = new VBox(inputField, sendButton);
        inputBox.setSpacing(5);

        SplitPane splitPane = new SplitPane(console, inputBox);
        splitPane.setDividerPositions(0.8); // 设置初始分隔条位置

        Scene scene = new Scene(splitPane, 1024, 768);

        primaryStage.setTitle("JavaFX Console");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
