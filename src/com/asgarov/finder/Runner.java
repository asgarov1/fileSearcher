package com.asgarov.finder;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import static com.asgarov.finder.helper.UIHelper.*;
import static com.asgarov.finder.service.FinderService.getFinder;

public class Runner extends Application {

    public static final String DEFAULT_TEXT = "Please enter filename: ";

    private final Text searchResultsTextArea = new Text(800, 200, DEFAULT_TEXT);
    private final TextField fileInput = createTextField(400);
    private final TextField startDirectory = createTextField(400);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        startDirectory.setText("C:/Users/");

        //create labels for Shape and Color
        Label searchLabel = createLabel("FileName:", Color.BLACK, Font.font("Arial", 15));
        Label startDirectoryLabel = createLabel("Starting Directory:", Color.BLACK, Font.font("Arial", 15));

        // Create Button and define event handling to it (on action -> processAction)
        Button searchButton = createButton("Search");
        searchButton.setOnAction(event -> getFinder().search(searchResultsTextArea, fileInput.getText(), startDirectory.getText()));

        Button stopButton = createButton("Stop");
        stopButton.setOnAction(event -> getFinder().stopSearch());

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(searchButton, stopButton);

        // Defining HBox to hold all the input components (1 labels and 1 textField)
        HBox inputComponents = new HBox(10);
        inputComponents.setAlignment(Pos.CENTER);
        inputComponents.getChildren().addAll(searchLabel, fileInput, startDirectoryLabel, startDirectory);

        ScrollPane scrollPane = new ScrollPane(searchResultsTextArea);
        BorderPane borderPane = new BorderPane(scrollPane);
        borderPane.setPadding(new Insets(20));

        // main component to hold all the other components
        final VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(inputComponents, buttons, borderPane);
        setEnterEventHandler(root);

        getFinder().scheduleResultsTextAreaUpdater(searchResultsTextArea);

        // Primary scene defined
        Scene scene = new Scene(root, 900, 500);
        primaryStage.setTitle("My search app");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> shutdownApplication());
    }

    private void shutdownApplication() {
        getFinder().shutdown();
        System.exit(0);
    }

    private void setEnterEventHandler(Node root) {
        root.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                getFinder().search(searchResultsTextArea, fileInput.getText(), startDirectory.getText());
                ev.consume();
            }
        });
    }
}
