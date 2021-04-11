package com.asgarov.finder.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.asgarov.finder.helper.ApplicationProperties.MAX_RESULTS;
import static java.lang.Runtime.getRuntime;

public class FinderService {

    private final Set<String> searchResults = new ConcurrentSkipListSet<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(getRuntime().availableProcessors());
    private static final FinderService instance = new FinderService();

    private FinderService() {
    }

    public static FinderService getFinder() {
        return instance;
    }

    public void addSearchResult(String searchResult) {
        searchResults.add(searchResult);
    }

    private void updateSearchResults(String fileName, Path startDirectory, int depth) throws IOException {
        if(stillSearching()) {
            Files.walkFileTree(startDirectory, Set.of(), depth, new FileVisitorImpl(fileName));
        }
    }

    public void stopSearch() {
        executorService.shutdownNow();
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    public void clearSearchResults() {
        searchResults.clear();
    }

    public void submit(String fileInput, Path startDirectory, int depth) {
        if (executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(getRuntime().availableProcessors());
        }
        executorService.submit(() -> {
            try {
                updateSearchResults(fileInput, startDirectory, depth);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Set<String> getSearchResults() {
        return searchResults;
    }

    public boolean stillSearching() {
        return searchResults.size() < MAX_RESULTS;
    }
}
