#
# Copyright (C) 2019 - 2020 Rabobank Nederland
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

Feature: Verification

  Background:
    * def defaultVerificationRequest = {expectedProducts: [{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def defaultSteps = ['build-step-link.json','test-step-link.json']

  Scenario: happy flow all rules
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest) ,testDir: 'happy-flow',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-happy-flow
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest) ,testDir: 'match-rule-happy-flow',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-happy-flow-with-prefix
    * def resp = call read('classpath:feature/verification/verification-template.feature') {verificationRequest:#(defaultVerificationRequest) ,testDir: 'match-rule-happy-flow-with-prefix',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-no-destination-artifact
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'match-rule-no-destination-artifact',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":false}

  Scenario: happy flow match-rule-no-source-artifact
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'match-rule-no-source-artifact',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":false}

  Scenario: build-steps-incomplete-run
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'build-steps-incomplete-run',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":false}

  Scenario: commands-incorrect
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'commands-incorrect',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":false}

  Scenario: delete-rule-no-deletion
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { verificationRequest:#(defaultVerificationRequest),testDir: 'delete-rule-no-deletion',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":false}

  Scenario: create-rule-no-creation
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { verificationRequest:#(defaultVerificationRequest),testDir: 'create-rule-no-creation',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":false}

  Scenario: modify-rule-not-modified
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { verificationRequest:#(defaultVerificationRequest),testDir: 'modify-rule-not-modified',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":false}

  Scenario: require-rule-no-required-product-material
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'require-rule-no-required-product-material',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":false}

  Scenario: disallow-rule-non-empty
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'disallow-rule-non-empty',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":false}

  Scenario: allow-rule-no-match
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'allow-rule-no-match',steps:#(defaultSteps)}
    And match resp.response == {"runIsValid":false}




