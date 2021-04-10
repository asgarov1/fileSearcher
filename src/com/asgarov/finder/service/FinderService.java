package com.asgarov.finder.service;

import com.asgarov.finder.util.FileVisitorImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class FinderService {

    private final Set<String> searchResults = new ConcurrentSkipListSet<>();
    private ExecutorService executorService = Executors.newWorkStealingPool(16);

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
        executorService.shutdownNow();
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    public void clearSearchResults() {
        searchResults.clear();
    }

    public void submit(String fileInput, String startDirectory, int temp) {
        if (executorService.isShutdown()) {
            executorService = Executors.newWorkStealingPool();
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


}
