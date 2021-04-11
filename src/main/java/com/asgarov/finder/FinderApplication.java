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

import static com.asgarov.finder.helper.ApplicationProperties.*;
import static com.asgarov.finder.helper.UIHelper.*;
import static com.asgarov.finder.service.FinderService.getFinder;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FinderApplication extends Application {

    private final VBox resultsBox = createCenteredVBox();
    private final TextField fileInput = createTextField(TEXT_FIELD_WIDTH);
    private final TextField startDirectory = createTextField(TEXT_FIELD_WIDTH, STANDARD_DIRECTORY);
    private final Label infoLabel = createGrayLabel("");

    private final ScheduledExecutorService scheduledService = newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture = scheduleResultsTextAreaUpdater();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        DirectoryChooser directoryChooser = createDirectoryChooser();
        Button selectDirectory = new Button(CHOOSE);
        selectDirectory.setOnAction(e -> chooseDirectory(primaryStage, directoryChooser));

        Button searchButton = createButton(SEARCH);
        searchButton.setOnAction(event -> search(fileInput.getText(), startDirectory.getText()));
        searchButton.setDefaultButton(true);

        Button stopButton = createButton(STOP);
        stopButton.setOnAction(event -> {
            infoLabel.setText(STOPPED_SEARCH_MESSAGE);
            scheduledFuture.cancel(true);
            getFinder().stopSearch();
        });

        HBox buttons = new HBox(SPACING);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(searchButton, stopButton);

        HBox inputComponents = new HBox(SPACING);
        inputComponents.setAlignment(Pos.CENTER);
        inputComponents.getChildren().addAll(createBlackLabel(FILE_NAME), fileInput, createBlackLabel(STARTING_DIRECTORY), startDirectory, selectDirectory);

        final VBox root = new VBox(SPACING);
        root.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(resultsBox);
        scrollPane.setPadding(new Insets(SPACING));
        scrollPane.setFitToWidth(true);

        root.getChildren().addAll(infoLabel, inputComponents, buttons, scrollPane);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle(TITLE);
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
            infoLabel.setText(SEARCHING_MESSAGE);
            if (!scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(true);
            }
            scheduledFuture = scheduleResultsTextAreaUpdater();
            try {
                Files.list(Paths.get(startDirectory)).forEach((path) -> getFinder().submit(fileInput, path, DEFAULT_DEPTH));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateSearchResultsPane(Pane box) {
        try {
            box.getChildren().clear();
            if (!getFinder().getSearchResults().isEmpty()) {
                box.getChildren().add(createGrayLabel(LINK_INSTRUCTIONS_MESSAGE));
                getFinder().getSearchResults().stream().map(UIHelper::mapToHyperlink).forEach(box.getChildren()::add);
            }
            if (!getFinder().stillSearching()) {
                infoLabel.setText(RESULTS_MAXED_OUT_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
