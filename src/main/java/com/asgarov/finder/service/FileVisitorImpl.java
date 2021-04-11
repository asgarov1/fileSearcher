package com.asgarov.finder.service;

import com.asgarov.finder.util.PathUtil;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static com.asgarov.finder.service.FinderService.getFinder;
import static java.nio.file.FileVisitResult.*;

public class FileVisitorImpl implements FileVisitor<Path> {

    private final String searchedFileName;

    public FileVisitorImpl(String searchedFileName) {
        this.searchedFileName = searchedFileName;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (getFinder().stillSearching()) {
            return CONTINUE;
        } else {
            return TERMINATE;
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (PathUtil.matches(searchedFileName, file)) {
            getFinder().addSearchResult(file.toString());
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        if (exc instanceof AccessDeniedException || exc instanceof NoSuchFileException) {
            return SKIP_SUBTREE;
        }
        throw exc;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return CONTINUE;
    }
}
