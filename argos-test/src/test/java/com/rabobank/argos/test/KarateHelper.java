package com.rabobank.argos.test;

import com.intuit.karate.cucumber.CucumberRunner;
import com.intuit.karate.cucumber.KarateStats;

class KarateHelper {

    private static final String SERVER_BASEURL = "server.baseurl";
    private static final String REPORT_DIR = "target/surefire-reports";
    private KarateHelper() {

    }

    static KarateStats runKarateTests(Class clazz) {
        Properties properties = Properties.getInstance();
        System.setProperty(SERVER_BASEURL,properties.getApiBaseUr());
        return CucumberRunner.parallel(clazz, 1, REPORT_DIR);
    }
}
