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
Feature: create a valid link

  Background:
    * url karate.properties['server.baseurl']
    * def linkPath = '/api/supplychain/'+ __arg.supplyChainId + '/link'
    * def linkToBeSigned = read(__arg.json)
    * def keyNumber = __arg.keyNumber

  Scenario: store link with valid specifications should return a 204
    * def signedLink = call read('classpath:feature/link/sign-link.feature') {json:#(linkToBeSigned),keyNumber:#(keyNumber)}
    * def keyPair = read('classpath:testmessages/key/keypair'+keyNumber+'.json')
    * configure headers = call read('classpath:headers.js') { username: #(keyPair.keyId),password:test}
    Given path linkPath
    And request signedLink.response
    When method POST
    Then status 204