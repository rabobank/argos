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
    * def token = karate.properties['bearer.token']
    * configure headers = call read('classpath:headers.js') { token: #(token)}
    * def rootLabel = call read('classpath:feature/label/create-label.feature') { name: 'root1'}

  Scenario: store a non personal account with valid name should return a 201
    * def result = call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    * match result.response == { name: 'npa 1', id: '#uuid', parentLabelId: '#uuid' }

  Scenario: store a non personal account without authorization should return a 401 error
    * configure headers = null
    Given path '/api/nonpersonalaccount'
    And request { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    When method POST
    Then status 401

  Scenario: store a non personal account with a non existing parent label id should return a 400
    Given path '/api/nonpersonalaccount'
    And request { name: 'label', parentLabelId: '940935f6-22bc-4d65-8c5b-a0599dedb510'}
    When method POST
    Then status 400
    And match response.message == 'parent label id not found : 940935f6-22bc-4d65-8c5b-a0599dedb510'

  Scenario: store two non personal accounts with the same name should return a 400
    * call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    Given path '/api/nonpersonalaccount'
    And request { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
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

  Scenario: create a non personal account key should return a 200
    * def createResult = call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/npa-keypair1.json')
    * def result = call read('create-non-personal-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * match result.response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: create a non personal account key should without authorization should return a 401 error
    * def createResult = call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/npa-keypair1.json')
    * configure headers = null
    Given path '/api/nonpersonalaccount/'+accountId+'/key'
    And request keyPair
    When method POST
    Then status 401

  Scenario: get a active non personal account key should return a 200
    * def createResult = call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/npa-keypair1.json')
    * call read('create-non-personal-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/nonpersonalaccount/'+accountId+'/key'
    Given path restPath
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get a active non personal account key without authorization should return a 401 error
    * def createResult = call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/npa-keypair1.json')
    * call read('create-non-personal-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/nonpersonalaccount/'+accountId+'/key'
    * configure headers = null
    Given path restPath
    When method GET
    Then status 401

  Scenario: get active key of authenticated npa should return a 200
    * def keypairResponse = call read('classpath:feature/account/create-non-personal-account-with-key.feature') {accountName: 'npa1', parentLabelId: #(rootLabel.response.id), keyFile: 'keypair1'}
    * def keyPair = keypairResponse.response
    * configure headers =  call read('classpath:headers.js') { username: #(keyPair.keyId),password:test}
    Given path '/api/nonpersonalaccount/me/activekey'
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get active key of authenticated npa with invalid credentials should return a 401
    * def keypairResponse = call read('classpath:feature/account/create-non-personal-account-with-key.feature') {accountName: 'npa1', parentLabelId: #(rootLabel.response.id), keyFile: 'keypair1'}
    * def keyPair = keypairResponse.response
    * configure headers =  call read('classpath:headers.js') { username: fake,password:fake}
    Given path '/api/nonpersonalaccount/me/activekey'
    When method GET
    Then status 401

  Scenario: get an active non personal account key after update should return a 200
    * def createResult = call read('create-non-personal-account.feature') { name: 'npa 1', parentLabelId: #(rootLabel.response.id)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/npa-keypair1.json')
    * call read('create-non-personal-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPathKey = '/api/nonpersonalaccount/'+accountId+'/key'
    * def restPathUpdate = '/api/nonpersonalaccount/'+ accountId
    Given path restPathUpdate
    And request { name: 'npa 2', parentLabelId: #(rootLabel.response.id)}
    When method PUT
    Then status 200
    Given path restPathKey
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

