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


Feature: Verification template

  Background:
    * def testFilesDir = __arg.testDir
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { name: 'name'}
    * def layoutPath = '/api/supplychain/'+ supplyChain.response.id + '/layout'
    * call read('classpath:feature/key/create-key.feature')
    * def supplyChainPath = '/api/supplychain/'+ supplyChain.response.id
    * def supplyChainId = supplyChain.response.id

  Scenario: run template

    Given print 'testFilesDir : ', testFilesDir
    * def layout = 'classpath:testmessages/verification/'+testFilesDir+'/layout.json'
    * call read('classpath:feature/layout/create-layout.feature') {supplyChainId:#(supplyChainId), json:#(layout)}
    * def buildStepLink = 'classpath:testmessages/verification/'+testFilesDir+'/build-step-link.json'
    * call read('classpath:feature/link/create-link.feature') {supplyChainId:#(supplyChainId), json:#(buildStepLink)}
    * def testStepLink = 'classpath:testmessages/verification/'+testFilesDir+'/test-step-link.json'
    * call read('classpath:feature/link/create-link.feature') {supplyChainId:#(supplyChainId), json:#(testStepLink)}
    Given path supplyChainPath + '/verification'
    And request {"expectedProducts": [{"uri": "target/argos-test-0.0.1-SNAPSHOT.jar","hash": "49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162"}]}
    And header Content-Type = 'application/json'
    When method POST
    Then status 200