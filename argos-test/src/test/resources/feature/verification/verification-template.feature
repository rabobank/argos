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

@Ignore
Feature: Verification template

  Background:
    * url karate.properties['server.baseurl']
    * def verificationRequest = __arg.verificationRequest
    * def testFilesDir = __arg.testDir
    * def steps = __arg.steps
    * def layoutSigningKey = __arg.layoutSigningKey
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * call read('classpath:feature/account/insert-test-key-pairs.feature') {parentLabelId: #(supplyChain.response.parentLabelId)}
    * def layoutPath = '/api/supplychain/'+ supplyChain.response.id + '/layout'
    * def supplyChainPath = '/api/supplychain/'+ supplyChain.response.id
    * def supplyChainId = supplyChain.response.id
    * def layoutAuthorizedAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Layout authorized person',email: 'local.permissions@extra.go'}
    * call read('classpath:feature/account/set-local-permissions.feature') { accountId: #(layoutAuthorizedAccount.response.id),labelId: #(supplyChain.response.parentLabelId), permissions: ["READ","LAYOUT_ADD"]}

  Scenario: run template
    Given print 'testFilesDir : ', testFilesDir
    * configure headers = call read('classpath:headers.js') { token: #(layoutAuthorizedAccount.response.token)}
    * def layout = 'classpath:testmessages/verification/'+testFilesDir+'/layout.json'
    * def layoutCreated = call read('classpath:feature/layout/create-layout.feature') {supplyChainId:#(supplyChainId), json:#(layout), keyNumber:#(layoutSigningKey)}
    # this creates an array of stepLinksJson messages
    * def stepLinksJsonMapper = function(jsonlink, i){ return  {supplyChainId:supplyChainId, json:'classpath:testmessages/verification/'+testFilesDir+'/'+jsonlink.link, keyNumber:jsonlink.signingKey}}
    * def stepLinksJson = karate.map(steps, stepLinksJsonMapper)
    # when a call to a feature presented with an array of messages it will cal the feature template iteratively
    * call read('classpath:feature/link/create-link.feature') stepLinksJson
    Given path supplyChainPath + '/verification'
    And request  verificationRequest
    When method POST
    Then status 200