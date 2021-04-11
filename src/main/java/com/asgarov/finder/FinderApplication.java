package com.asgarov.finder;

import com.asgarov.finder.helper.UIHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Predicate;

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

    private boolean isHyperlink(Node intersectedNode) {
        return intersectedNode instanceof Text;
    }

    private void chooseDirectory(Stage primaryStage, DirectoryChooser directoryChooser) {
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory != null) {
            startDirectory.setText(selectedDirectory.getAbsolutePath());
        }
    }

    public ScheduledFuture<?> scheduleResultsTextAreaUpdater() {
        return scheduledService.scheduleWithFixedDelay(() -> Platform.runLater(this::updateSearchResultsPane),
                DELAY_BETWEEN_NEW_REQUEST_CHECKS_IN_MS, DELAY_BETWEEN_NEW_REQUEST_CHECKS_IN_MS, MILLISECONDS);
    }

    public void search(String fileInput, String startDirectory) {
        getFinder().clearSearchResults();
        resultsBox.getChildren().clear();

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

    public void updateSearchResultsPane() {
        if (resultsBox.getChildren().size() == getFinder().getSearchResults().size()) {
            return;
        }

        try {
            if (!getFinder().getSearchResults().isEmpty()) {
                addInstructionsMessageAsAFirstNodeIfNeeded(resultsBox);
                getFinder().getSearchResults()
                        .stream()
                        .filter(Predicate.not(this::isDisplayed))
                        .map(UIHelper::mapToHyperlink)
                        .forEach(resultsBox.getChildren()::add);
            }
            if (!getFinder().stillSearching()) {
                infoLabel.setText(RESULTS_MAXED_OUT_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isDisplayed(String linkText) {
        return resultsBox.getChildren()
                .stream()
                .filter(node -> node instanceof Hyperlink)
                .map(node -> (Hyperlink)node)
                .map(Hyperlink::getText)
                .filter(Objects::nonNull)
                .anyMatch(text -> text.equals(linkText));
    }

    private void addInstructionsMessageAsAFirstNodeIfNeeded(Pane box) {
        if(box.getChildren().isEmpty()) {
            Label grayLabel = createGrayLabel(LINK_INSTRUCTIONS_MESSAGE);
            grayLabel.setPadding(new Insets(0,0,20,0));
            box.getChildren().add(grayLabel);
        }
    }

}
