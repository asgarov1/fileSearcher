package com.asgarov.finder.service;

import com.asgarov.finder.util.FileVisitorImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinderService {

    private final Set<String> searchResults = new ConcurrentSkipListSet<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(8);

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
        if(searchResults.size() < 100) {
            Files.walkFileTree(startDirectory, new HashSet<>(), depth, new FileVisitorImpl(fileName));
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

    public void submit(String fileInput, Path startDirectory, int temp) {
        if (executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(8);
        }
        executorService.submit(() -> {
            try {
                updateSearchResults(fileInput, startDirectory, temp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public Set<String> getSearchResults() {
        return searchResults;
    }

    public boolean stillSearching() {
        return searchResults.size() < 100;
    }
}
