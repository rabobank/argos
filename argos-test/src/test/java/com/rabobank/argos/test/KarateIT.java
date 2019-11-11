package com.rabobank.argos.test;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = "classpath:feature", tags = "~@ignore")
public class KarateIT  extends BaseKarate {
    //@Test
    //@DisplayName("integration tests")
    public void runTests() {
        super.runTests();
    }
}
