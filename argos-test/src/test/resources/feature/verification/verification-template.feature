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

@Ignore
Feature: Verification template

  Background:
    * url karate.properties['server.baseurl']
    * def verificationRequest = __arg.verificationRequest
    * def testFilesDir = __arg.testDir
    * def steps = __arg.steps
    * call read('classpath:feature/reset.feature')
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { name: 'name'}
    * def layoutPath = '/api/supplychain/'+ supplyChain.response.id + '/layout'
    * call read('classpath:feature/key/create-key.feature')
    * def supplyChainPath = '/api/supplychain/'+ supplyChain.response.id
    * def supplyChainId = supplyChain.response.id

  Scenario: run template
    Given print 'testFilesDir : ', testFilesDir
    * def layout = 'classpath:testmessages/verification/'+testFilesDir+'/layout.json'
    * def layoutCreated = call read('classpath:feature/layout/create-layout.feature') {supplyChainId:#(supplyChainId), json:#(layout)}
    # this creates an array of stepLinksJson messages
    * def stepLinksJsonMapper = function(jsonlink, i){ return  {supplyChainId:supplyChainId, json:'classpath:testmessages/verification/'+testFilesDir+'/'+jsonlink,layoutToBeUpdated:layoutCreated.response,stepIndex:i}}
    * def stepLinksJson = karate.map(steps, stepLinksJsonMapper)
    # when a call to a feature presented with an array of messages it will cal the feature template iteratively
    * call read('classpath:feature/link/create-link-with-valid-layout-update.feature') stepLinksJson
    Given path supplyChainPath + '/verification'
    And request  verificationRequest
    And header Content-Type = 'application/json'
    When method POST
    Then status 200