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

@ignore
Feature: create a valid layout

  Background:
    * url karate.properties['server.baseurl']
    * def layoutPath = '/api/supplychain/'+ __arg.supplyChainId + '/layout'
    * def layoutToBeSigned = read(__arg.json)
    * def keyNumber = __arg.keyNumber
    * def keyPair = defaultTestDate.personalAccounts['default-pa'+keyNumber]

  Scenario: store layout with valid specifications should return a 201
    * def signedLayout = call read('classpath:feature/layout/sign-layout.feature') {json:#(layoutToBeSigned),keyNumber:#(keyNumber)}
    * configure headers = call read('classpath:headers.js') { token: #(keyPair.token)}
    Given path layoutPath
    And request signedLayout.response
    When method POST
    Then status 201