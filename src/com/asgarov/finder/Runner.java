package com.asgarov.finder;

import com.asgarov.finder.helper.UIHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;

import static com.asgarov.finder.helper.UIHelper.*;
import static com.asgarov.finder.service.FinderService.getFinder;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Runner extends Application {

    private static final long DELAY_BETWEEN_NEW_REQUEST_CHECKS_IN_MS = 200;
    public static final String STANDARD_DIRECTORY = "C:/Users/";

    private final VBox resultsBox = createCenteredVBox();
    private final TextField fileInput = createTextField(400);
    private final TextField startDirectory = createTextField(400, STANDARD_DIRECTORY);

    private boolean firstRun = true;
    private ScheduledExecutorService scheduledService = newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        DirectoryChooser directoryChooser = createDirectoryChooser();
        Button selectDirectory = new Button("Choose");
        selectDirectory.setOnAction(e -> chooseDirectory(primaryStage, directoryChooser));

        Label infoLabel = createGrayLabel("");
        Button searchButton = createButton("Search");
        searchButton.setOnAction(event -> {
            infoLabel.setText("Searching...");
            search(resultsBox, fileInput.getText(), startDirectory.getText());
        });
        searchButton.setDefaultButton(true);

        Button stopButton = createButton("Stop");
        stopButton.setOnAction(event -> {
            infoLabel.setText("Stopped search");
            getFinder().stopSearch();
            scheduledService.shutdownNow();
        });

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(searchButton, stopButton);

        HBox inputComponents = new HBox(10);
        inputComponents.setAlignment(Pos.CENTER);
        inputComponents.getChildren().addAll(createBlackLabel("FileName:"), fileInput, createBlackLabel("Starting Directory:"), startDirectory, selectDirectory);

        final VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(infoLabel, inputComponents, buttons, resultsBox);

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
        startDirectory.setText(selectedDirectory.getAbsolutePath());
    }

    public void scheduleResultsTextAreaUpdater(VBox vbox) {
        scheduledService.scheduleWithFixedDelay(() -> Platform.runLater(() -> updateSearchResultsPane(vbox)),
                0, DELAY_BETWEEN_NEW_REQUEST_CHECKS_IN_MS, MILLISECONDS);
    }

    public void search(VBox box, String fileInput, String startDirectory) {
        getFinder().clearSearchResults();

        if (!fileInput.isEmpty() && !startDirectory.isEmpty()) {
            if (firstRun) {
                scheduleResultsTextAreaUpdater(box);
                firstRun = false;
            } else if (scheduledService.isShutdown()) {
                scheduledService = newSingleThreadScheduledExecutor();
                scheduleResultsTextAreaUpdater(box);
            }

            for (int i = 5; i < 100; i += 5) {
                getFinder().submit(fileInput, startDirectory, i);
            }
        }
    }

    public void updateSearchResultsPane(Pane box) {
        try {
            box.getChildren().clear();
            if(!getFinder().getSearchResults().isEmpty()) {
                box.getChildren().add(createGrayLabel("Left click -> opens, right click -> copies to clipboard"));
                getFinder().getSearchResults().stream().map(UIHelper::mapToHyperlink).forEach(box.getChildren()::add);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
