package com.asgarov.finder.util;

import java.nio.file.Path;

public class PathUtil {
    public static boolean matches(String fileName, Path path) {
        try {
            return path.getFileName().toString().toLowerCase().contains(fileName.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }
}
