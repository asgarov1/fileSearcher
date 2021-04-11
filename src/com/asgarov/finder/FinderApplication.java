package com.asgarov.finder;

import com.asgarov.finder.helper.UIHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static com.asgarov.finder.helper.UIHelper.*;
import static com.asgarov.finder.service.FinderService.getFinder;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FinderApplication extends Application {

    private static final long DELAY_BETWEEN_NEW_REQUEST_CHECKS_IN_MS = 200;
    public static final String STANDARD_DIRECTORY = "C:/Users/";

    private final VBox resultsBox = createCenteredVBox();
    private final TextField fileInput = createTextField(400);
    private final TextField startDirectory = createTextField(400, STANDARD_DIRECTORY);
    private final Label infoLabel = createGrayLabel("");

    private final ScheduledExecutorService scheduledService = newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture = scheduleResultsTextAreaUpdater();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        DirectoryChooser directoryChooser = createDirectoryChooser();
        Button selectDirectory = new Button("Choose");
        selectDirectory.setOnAction(e -> chooseDirectory(primaryStage, directoryChooser));

        Button searchButton = createButton("Search");
        searchButton.setOnAction(event -> search(fileInput.getText(), startDirectory.getText()));
        searchButton.setDefaultButton(true);

        Button stopButton = createButton("Stop");
        stopButton.setOnAction(event -> {
            infoLabel.setText("Stopped search");
            scheduledFuture.cancel(true);
            getFinder().stopSearch();
        });

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(searchButton, stopButton);

        HBox inputComponents = new HBox(10);
        inputComponents.setAlignment(Pos.CENTER);
        inputComponents.getChildren().addAll(createBlackLabel("FileName:"), fileInput, createBlackLabel("Starting Directory:"), startDirectory, selectDirectory);

        final VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(resultsBox);
        scrollPane.setPadding(new Insets(20));
        scrollPane.setFitToWidth(true);

        root.getChildren().addAll(infoLabel, inputComponents, buttons, scrollPane);

        Scene scene = new Scene(root, 900, 500);
        primaryStage.setTitle("My search app");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            getFinder().shutdown();
            scheduledService.shutdownNow();
        });
    }

    private void chooseDirectory(Stage primaryStage, DirectoryChooser directoryChooser) {
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            startDirectory.setText(selectedDirectory.getAbsolutePath());
        }
    }

    public ScheduledFuture<?> scheduleResultsTextAreaUpdater() {
        return scheduledService.scheduleWithFixedDelay(() -> Platform.runLater(() -> updateSearchResultsPane(resultsBox)),
                0, DELAY_BETWEEN_NEW_REQUEST_CHECKS_IN_MS, MILLISECONDS);
    }

    public void search(String fileInput, String startDirectory) {
        getFinder().clearSearchResults();

        if (!fileInput.isEmpty() && !startDirectory.isEmpty()) {
            infoLabel.setText("Searching...");
            if (!scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(true);
            }
            scheduledFuture = scheduleResultsTextAreaUpdater();
            try {
                Files.list(Paths.get(startDirectory)).forEach((path) -> getFinder().submit(fileInput, path, 25));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateSearchResultsPane(Pane box) {
        try {
            box.getChildren().clear();
            if (!getFinder().getSearchResults().isEmpty()) {
                box.getChildren().add(createGrayLabel("Left click -> opens, right click -> copies to clipboard"));
                getFinder().getSearchResults().stream().map(UIHelper::mapToHyperlink).forEach(box.getChildren()::add);
            }
            if (!getFinder().stillSearching()) {
                infoLabel.setText("Capped at 100 results. Consider refining your search");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
