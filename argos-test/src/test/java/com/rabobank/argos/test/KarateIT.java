package com.rabobank.argos.test;

import cucumber.api.CucumberOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CucumberOptions(features = "classpath:feature", tags = "~@ignore")
public class KarateIT  extends BaseKarate {
    @Test
    @DisplayName("integration tests")
    public void runTests() {
        super.runTests();
    }
}
