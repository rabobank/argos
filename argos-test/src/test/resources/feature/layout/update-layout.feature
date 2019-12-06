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
Feature: update a valid layout

  Background:
    * url karate.properties['server.baseurl']
    * def layoutPath = '/api/supplychain/'+ __arg.supplyChainId + '/layout/' + __arg.id
    * json layoutJson = { layout:#(__arg.json.layout)}

  Scenario: update layout with valid specifications should return a 201
    * def signedLayout = call read('classpath:feature/layout/sign-layout.feature') layoutJson
    * print signedLayout.response
    Given path layoutPath
    And request signedLayout.response
    And header Content-Type = 'application/json'
    When method PUT
    Then status 200
    Given path layoutPath
    When method GET
    Then status 200

