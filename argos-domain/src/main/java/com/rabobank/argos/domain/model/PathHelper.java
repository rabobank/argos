package com.rabobank.argos.domain.model;

public class PathHelper {

    private PathHelper() {
    }

    public static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        return path.replace("\\\\", "/").replace("\\", "/");
    }
}
