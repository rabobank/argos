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

Feature: SupplyChain

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')

  Scenario: store supplychain with valid name should return a 201
    Given path '/api/supplychain'
    * def result = call read('create-supplychain.feature') { name: 'name'}
    * def locationHeader = result.responseHeaders['Location'][0]
    * match result.response == { name: 'name', id: '#uuid' }
    * match locationHeader contains 'api/supplychain/'

  Scenario: store supplychain with non unique name should return a 400
    * call read('create-supplychain.feature') { name: 'name'}
    Given path '/api/supplychain'
    And request  {"name":"name"}
    And header Content-Type = 'application/json'
    When method POST
    Then status 400
    And match response == {"message":"supply chain name must be unique"}

  Scenario: get supplychain with valid id should return a 200
    * def result = call read('create-supplychain.feature') { name: 'name'}
    * def restPath = '/api/supplychain/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match result.response == { name: 'name', id: '#uuid' }

  Scenario: get supplychain with invalid id should return a 404
    Given path '/api/supplychain/invalidid'
    When method GET
    Then status 404
    And match response == {"message":"supply chain not found : invalidid"}

  Scenario: query supplychain with name  should return a 200
    * def result = call read('create-supplychain.feature') { name: 'name'}
    Given path '/api/supplychain/'
    And param name = 'name'
    When method GET
    Then status 200
    And match response[*] contains { name: 'name', id: '#uuid' }

  Scenario: query supplychain with no name  should return a 200
    * def result = call read('create-supplychain.feature') { name: 'name'}
    Given path '/api/supplychain/'
    When method GET
    Then status 200
    And match response[*] contains { name: 'name', id: '#uuid' }