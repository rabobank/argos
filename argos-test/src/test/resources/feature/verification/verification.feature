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
    * call read('classpath:feature/reset.feature')
    * def token = karate.properties['bearer.token']
    * configure headers = call read('classpath:headers.js') { token: #(token)}
    * def defaultVerificationRequest = {expectedProducts: [{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def defaultSteps = [{link:'build-step-link.json', signingKey:2},{link:'test-step-link.json', signingKey:3}]

  Scenario: happy flow all rules
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest) ,testDir: 'happy-flow',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}
  
  Scenario: products to verify wrong hash
    * def verificationRequest = {expectedProducts: [{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '0123456789012345678901234567890012345678901234567890123456789012'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(verificationRequest) ,testDir: 'happy-flow',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}
    
  Scenario: expected expected end products not matches
    * def verificationRequest = {expectedProducts: [{uri: 'argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(verificationRequest) ,testDir: 'happy-flow',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: multi segment happy flow all rules
    * def steps = [{link:'segment-1-build-step-link.json', signingKey:2},{link:'segment-1-test-step-link.json', signingKey:2},{link:'segment-2-build-step-link.json', signingKey:3},{link:'segment-2-test-step-link.json',signingKey:3}]
    * def verificationRequest = {expectedProducts: [{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(verificationRequest) ,testDir: 'multi-segment-happy-flow',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: multi segment happy flow with three segment hop
    * def steps = [{link:'segment-1-build-step-link.json', signingKey:2},{link:'segment-1-test-step-link.json', signingKey:2},{link:'segment-2-build-step-link.json', signingKey:3},{link:'segment-2-test-step-link.json',signingKey:3},{link:'segment-3-build-step-link.json', signingKey:2},{link:'segment-3-test-step-link.json', signingKey:2}]
    * def verificationRequest = {expectedProducts: [ {uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(verificationRequest) ,testDir: 'multi-segment-happy-flow-with-three-segment-hop',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: multi segment with multiple verification context
    * def steps = [{link:'segment-1-build-step-link.json', signingKey:2},{link:'segment-1-test-step-link.json', signingKey:2},{link:'segment-2-build-step-link.json', signingKey:3},{link:'segment-2-build-step-link-invalid.json', signingKey:3},{link:'segment-2-test-step-link.json',signingKey:3},{link:'segment-2-test-step-link-invalid.json',signingKey:3},{link:'segment-3-build-step-link.json', signingKey:2},{link:'segment-3-test-step-link.json', signingKey:2}]
    * def verificationRequest = {expectedProducts: [ {uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(verificationRequest) ,testDir: 'multi-segment-with-multiple-verification-context',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-happy-flow
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest) ,testDir: 'match-rule-happy-flow',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-happy-flow-with-prefix
    * def resp = call read('classpath:feature/verification/verification-template.feature') {verificationRequest:#(defaultVerificationRequest) ,testDir: 'match-rule-happy-flow-with-prefix',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-no-destination-artifact
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'match-rule-no-destination-artifact',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: happy flow match-rule-no-source-artifact
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'match-rule-no-source-artifact',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: build-steps-incomplete-run
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'build-steps-incomplete-run',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: commands-incorrect
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'commands-incorrect',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: delete-rule-no-deletion
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { verificationRequest:#(defaultVerificationRequest),testDir: 'delete-rule-no-deletion',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: create-rule-no-creation
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { verificationRequest:#(defaultVerificationRequest),testDir: 'create-rule-no-creation',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: modify-rule-not-modified
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { verificationRequest:#(defaultVerificationRequest),testDir: 'modify-rule-not-modified',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: require-rule-no-required-product-material
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'require-rule-no-required-product-material',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: disallow-rule-non-empty
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'disallow-rule-non-empty',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: allow-rule-no-match
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'allow-rule-no-match',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: multiple-run-id-happy-flow
    * def steps = [{link:'runid1-build-step-link.json', signingKey:2},{link:'runid1-test-step-link.json', signingKey:3},{link:'runid2-build-step-link.json', signingKey:2},{link:'runid2-test-step-link.json',signingKey:3}]
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'multiple-run-id-happy-flow',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: multiple-link-files-per-step-one-invalid
    * def steps = [{link:'build-step-link1.json', signingKey:2},{link:'build-step-link2.json', signingKey:2},{link:'test-step-link1.json', signingKey:2},{link:'test-step-link2.json',signingKey:2}]
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'multiple-link-files-per-step-one-invalid',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: multiple-verification-contexts-happy-flow
    * def steps = [{link:'build-step-link-valid.json', signingKey:2},{link:'build-step-link-invalid.json', signingKey:3},{link:'test-step-link-invalid.json', signingKey:2},{link:'test-step-link-valid.json',signingKey:3}]
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'multiple-verification-contexts',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}
   

  Scenario: verification without authorization should return a 401 error
    * url karate.properties['server.baseurl']
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * def supplyChainPath = '/api/supplychain/'+ supplyChain.response.id
    * configure headers = null
    Given path supplyChainPath + '/verification'
    And request defaultVerificationRequest
    When method POST
    Then status 401
