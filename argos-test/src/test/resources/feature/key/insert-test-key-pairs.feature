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

@ignore
Feature: insert test key pairs

  Background:
    * url karate.properties['server.baseurl']

  Scenario: store key pair 1
    Given path '/api/key'
    And request read('classpath:testmessages/key/keypair1.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 204

  Scenario: store key pair 2
    Given path '/api/key'
    And request read('classpath:testmessages/key/keypair2.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 204

  Scenario: store key pair 3
    Given path '/api/key'
    And request read('classpath:testmessages/key/keypair3.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 204

  Scenario: store key pair 4
    Given path '/api/key'
    And request read('classpath:testmessages/key/keypair4.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 204

  Scenario: store key pair 5
    Given path '/api/key'
    And request read('classpath:testmessages/key/keypair5.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 204