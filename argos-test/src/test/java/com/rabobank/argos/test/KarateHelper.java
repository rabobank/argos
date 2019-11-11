package com.rabobank.argos.test;

import com.intuit.karate.cucumber.CucumberRunner;
import com.intuit.karate.cucumber.KarateStats;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class KarateHelper {

    private static final String SERVER_BASEURL = "server.baseurl";
    private static final String REPORT_DIR = "target/surefire-reports";
    private KarateHelper() {

    }

    static KarateStats runKarateTests(Class clazz) {
        Properties properties = Properties.getInstance();
        log.info("karate base url : {}" , properties.getApiBaseUr());
        System.setProperty(SERVER_BASEURL,properties.getApiBaseUr());
        return CucumberRunner.parallel(clazz, 1, REPORT_DIR);
    }
}
