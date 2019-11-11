package com.rabobank.argos.test;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ReportHelper {

    private ReportHelper() {
    }

    static void generateReport() throws IOException {
        try (final Stream<Path> list = Files.list(Paths.get("target/surefire-reports"))) {
            final List<String> jsonPaths = list
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> path.toAbsolutePath().toString())
                    .collect(Collectors.toList());

            final Configuration config = new Configuration(new File("target"), "argos");
            final ReportBuilder reportBuilder = new ReportBuilder(jsonPaths, config);
            reportBuilder.generateReports();
        }
    }
}
