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

  Scenario: multi segment happy flow with three segment hop
    * def steps = [{link:'segment-1-build-step-link.json', signingKey:2},{link:'segment-1-test-step-link.json', signingKey:2},{link:'segment-2-build-step-link.json', signingKey:3},{link:'segment-2-test-step-link.json',signingKey:3},{link:'segment-3-build-step-link.json', signingKey:2},{link:'segment-3-test-step-link.json', signingKey:2}]
    * def verificationRequest = {expectedProducts: [ {uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(verificationRequest) ,testDir: 'multi-segment-happy-flow-with-three-segment-hop',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}
