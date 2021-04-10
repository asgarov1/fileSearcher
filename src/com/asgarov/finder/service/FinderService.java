package com.asgarov.finder.service;

import com.asgarov.finder.util.FileVisitorImpl;
import javafx.scene.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.joining;

public class FinderService {

    private static final long DELAY_BETWEEN_NEW_REQUEST_CHECKS_IN_MS = 50;

    private final Set<String> searchResults = new ConcurrentSkipListSet<>();
    private ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService executorService = Executors.newWorkStealingPool();

    private static final FinderService instance = new FinderService();

    private final AtomicBoolean stillSearching = new AtomicBoolean(true);

    private FinderService() {
    }

    public static FinderService getFinder() {
        return instance;
    }

    public void addSearchResult(String searchResult) {
        searchResults.add(searchResult);
    }

    public String getSearchResults() {
        return searchResults.stream().collect(joining(System.lineSeparator()));
    }

    public void scheduleResultsTextAreaUpdater(Text searchResultsTextArea) {
        scheduledService.scheduleWithFixedDelay(() -> searchResultsTextArea.setText(getFinder().getSearchResults()),
                0, DELAY_BETWEEN_NEW_REQUEST_CHECKS_IN_MS, MILLISECONDS);
    }

    public void search(Text searchResultsTextArea, String fileInput, String startDirectory) {
        clearSearchResults();
        if (scheduledService.isShutdown()) {
            scheduledService = Executors.newSingleThreadScheduledExecutor();
            getFinder().scheduleResultsTextAreaUpdater(searchResultsTextArea);
        }
        if (!fileInput.isEmpty() && !startDirectory.isEmpty()) {
            stillSearching.set(true);
            executorService.shutdownNow();
            executorService = Executors.newFixedThreadPool(20);
            for (int i = 5; i < 100; i += 5) {
                final int temp = i;
                executorService.submit(() -> {
                    try {
                        updateSearchResults(fileInput, startDirectory, temp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private void updateSearchResults(String fileName, String startDirectory, int depth) throws IOException {
        if (stillSearching.get()) {
            Path searchPath;
            if (!startDirectory.isEmpty() && Files.exists(Paths.get(startDirectory))) {
                searchPath = Paths.get(startDirectory);
            } else {
                searchPath = Paths.get("").toAbsolutePath().getRoot();
            }
            Files.walkFileTree(searchPath, new HashSet<>(), depth, new FileVisitorImpl(fileName));
        }
    }

    public void stopSearch() {
        stillSearching.set(false);
        scheduledService.shutdownNow();
        executorService.shutdownNow();
    }

    public void shutdown() {
        scheduledService.shutdownNow();
        executorService.shutdownNow();
    }

    public void clearSearchResults() {
        searchResults.clear();
    }
}
