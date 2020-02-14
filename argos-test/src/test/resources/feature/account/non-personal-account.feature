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

Feature: Non Personal Account

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def rootLabel = call read('classpath:feature/label/create-label.feature') { name: 'root1'}

  Scenario: store a non personal account with valid name should return a 201
    * def result = call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    * match result.response == { name: 'npa 1', id: '#uuid', parentLabelId: '#uuid' }

  Scenario: store a non personal account with a non existing parent label id should return a 400
    Given path '/api/nonpersonalaccount'
    And request { name: 'label', parentLabelId: '940935f6-22bc-4d65-8c5b-a0599dedb510'}
    And header Content-Type = 'application/json'
    When method POST
    Then status 400
    And match response.message == 'parent label id not found : 940935f6-22bc-4d65-8c5b-a0599dedb510'

  Scenario: store two non personal accounts with the same name should return a 400
    * call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    Given path '/api/nonpersonalaccount'
    And request { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    And header Content-Type = 'application/json'
    When method POST
    Then status 400
    And match response.message contains 'non personal account with name: npa 1 and parentLabelId'

  Scenario: retrieve non personal account should return a 200
    * def result = call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    * def restPath = '/api/nonpersonalaccount/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'npa 1', id: '#(result.response.id)', parentLabelId: #(rootLabel.response.id)}

  Scenario: update a non personal account should return a 200
    * def createResult = call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    * def accountId = createResult.response.id
    * def restPath = '/api/nonpersonalaccount/'+accountId
    Given path restPath
    And request { name: 'npa 2', parentLabelId: #(rootLabel.response.id)}
    When method PUT
    Then status 200
    And match response == { name: 'npa 2', id: '#(accountId)', parentLabelId: #(rootLabel.response.id)}

  Scenario: create a non personal account key should return a 201
    * def createResult = call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/keypair1.json')
    * def result = call read('create-non-personal-account-key.feature') { accountId: #(accountId), key: #(keyPair)}
    * match result.response == keyPair

  Scenario: get a active non personal account key should return a 200
    * def createResult = call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/keypair1.json')
    * call read('create-non-personal-account-key.feature') { accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/nonpersonalaccount/'+accountId+'/key'
    Given path restPath
    When method GET
    Then status 200
    And match response == keyPair