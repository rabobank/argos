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

Feature: Layout

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def tokenResponse = callonce read('classpath:feature/authenticate.feature')
    * configure headers = call read('classpath:headers.js') { token: #(tokenResponse.response.token)}
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * call read('classpath:feature/account/insert-test-key-pairs.feature') {parentLabelId: #(supplyChain.response.parentLabelId)}
    * def layoutPath = '/api/supplychain/'+ supplyChain.response.id + '/layout'
    * def validLayout = 'classpath:testmessages/layout/valid-layout.json'

  Scenario: store layout with valid specifications should return a 200
    * call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:2}

  Scenario: store link with invalid specifications should return a 400 error
    Given path layoutPath
    And request read('classpath:testmessages/layout/invalid-layout.json')
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/layout/invalid-layout-response.json')

  Scenario: store link without authorization should return a 401 error
    * configure headers = null
    Given path layoutPath
    And header Content-Type = 'application/json'
    And request read(validLayout)
    When method POST
    Then status 401

  Scenario: find layout with valid supplychainid should return a 200
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:2}
    Given path layoutPath
    When method GET
    Then status 200
    * def layoutId = layoutResponse.response.id
    * def response = read('classpath:testmessages/layout/valid-layout-response.json')
    And match response[*] contains response

  Scenario: find layout without authorization should return a 401 error
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:2}
    * configure headers = null
    Given path layoutPath
    And header Content-Type = 'application/json'
    When method GET
    Then status 401

  Scenario: update a layout should return a 200
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:2}
    * def layoutId = layoutResponse.response.id
    * def layoutToBeSigned = read('classpath:testmessages/layout/valid-update-layout.json')
    * def requestBody = call read('sign-layout.feature') {json:#(layoutToBeSigned),keyNumber:3}
    Given path layoutPath + '/' + layoutId
    And request requestBody.response
    When method PUT
    Then status 200
    * def layoutId = layoutResponse.response.id
    * def expectedResponse = read('classpath:testmessages/layout/valid-update-layout-response.json')
    And match response contains expectedResponse

  Scenario: update a layout without authorization should return a 401 error
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:2}
    * def layoutId = layoutResponse.response.id
    * def layoutToBeSigned = read('classpath:testmessages/layout/valid-update-layout.json')
    * def requestBody = call read('sign-layout.feature') {json:#(layoutToBeSigned),keyNumber:3}
    * configure headers = null
    Given path layoutPath + '/' + layoutId
    And request requestBody.response
    And header Content-Type = 'application/json'
    When method PUT
    Then status 401
