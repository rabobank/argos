package com.rabobank.argos.domain;

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
