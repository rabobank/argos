#
# Copyright (C) 2019 Rabobank Nederland
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

  Scenario: happy flow all rules
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'happy-flow'}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-happy-flow
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'match-rule-happy-flow'}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-happy-flow-with-prefix
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'match-rule-happy-flow-with-prefix'}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-no-destination-artifact
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'match-rule-no-destination-artifact'}
    And match resp.response == {"runIsValid":false}

  Scenario: happy flow match-rule-no-source-artifact
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'match-rule-no-source-artifact'}
    And match resp.response == {"runIsValid":false}

  Scenario: build-steps-incomplete-run
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'build-steps-incomplete-run'}
    And match resp.response == {"runIsValid":false}

  Scenario: commands-incorrect
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'commands-incorrect'}
    And match resp.response == {"runIsValid":false}

  Scenario: delete-rule-no-deletion
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'delete-rule-no-deletion'}
    And match resp.response == {"runIsValid":false}

  Scenario: create-rule-no-creation
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'create-rule-no-creation'}
    And match resp.response == {"runIsValid":false}

  Scenario: modify-rule-not-modified
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'modify-rule-not-modified'}
    And match resp.response == {"runIsValid":false}

  Scenario: require-rule-no-required-product-material
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'require-rule-no-required-product-material'}
    And match resp.response == {"runIsValid":false}

  Scenario: disallow-rule-non-empty
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'disallow-rule-non-empty'}
    And match resp.response == {"runIsValid":false}

  Scenario: allow-rule-no-match
    * def resp = call read('classpath:feature/verification/verification-template.feature') { testDir: 'allow-rule-no-match'}
    And match resp.response == {"runIsValid":false}



