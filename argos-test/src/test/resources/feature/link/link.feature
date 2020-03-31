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

Feature: Link

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'name', parentLabelId: #(defaultTestData.defaultRootLabel.id)}
    * def linkPath = '/api/supplychain/'+ supplyChain.response.id + '/link'
    * def validLink = 'classpath:testmessages/link/valid-link.json'

  Scenario: store link with valid specifications should return a 204
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLink), keyNumber:1}

  Scenario: NPA can store a link with valid specifications and should return a 204
    * def childLabelResult = call read('classpath:feature/label/create-label.feature') {name: child_label, parentLabelId: #(supplyChain.response.parentLabelId)}
    * def otherSupplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'other', parentLabelId: #(childLabelResult.response.id)}
    * call read('create-link.feature') {supplyChainId:#(otherSupplyChain.response.id), json:#(validLink), keyNumber:1}

  Scenario: user with local permission LINK_ADD can store a link
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["LINK_ADD"]}
    * def otherSupplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: other-supply-chain, parentLabelId: #(info.labelId)}
    * def layoutToSign = read(validLink)
    * def signedLink = call read('classpath:feature/link/sign-link.feature') {json:#(layoutToSign),keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    Given path '/api/supplychain/'+ otherSupplyChain.response.id + '/link'
    And request signedLink.response
    When method POST
    Then status 204

  Scenario: user without local permission LINK_ADD cannot store a link
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["READ"]}
    * def otherSupplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: other-supply-chain, parentLabelId: #(info.labelId)}
    * def layoutToSign = read(validLink)
    * def signedLink = call read('classpath:feature/link/sign-link.feature') {json:#(layoutToSign),keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    Given path '/api/supplychain/'+ otherSupplyChain.response.id + '/link'
    And request signedLink.response
    When method POST
    Then status 403

  Scenario: NPA in other root label cannot store a link
    * def otherRootLabel = call read('classpath:feature/label/create-label.feature') { name: 'other_root_label'}
    * def otherSupplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: other-supply-chain, parentLabelId: #(otherRootLabel.response.id)}
    * def layoutToSign = read(validLink)
    * def signedLink = call read('classpath:feature/link/sign-link.feature') {json:#(layoutToSign),keyNumber:1}
    * def keyPair = defaultTestData.nonPersonalAccount['default-npa1']
    * configure headers = call read('classpath:headers.js') { username: #(keyPair.keyId), password: #(keyPair.hashedKeyPassphrase)}
    Given path '/api/supplychain/'+ otherSupplyChain.response.id + '/link'
    And request signedLink.response
    When method POST
    Then status 403

  Scenario: store link with invalid specifications should return a 400 error
    Given path linkPath
    And request read('classpath:testmessages/link/invalid-link.json')
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/link/invalid-link-response.json')

  Scenario: store link without authorization should return a 401 error
    * configure headers = null
    Given path linkPath
    And request read(validLink)
    And header Content-Type = 'application/json'
    When method POST
    Then status 401

  Scenario: find link with valid supplychainid should return a 200
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLink), keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    Given path linkPath
    When method GET
    Then status 200
    And match response[*] contains read('classpath:testmessages/link/valid-link-response.json')

  Scenario: find link without authorization should return a 401 error
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLink), keyNumber:1}
    * configure headers = null
    Given path linkPath
    And header Content-Type = 'application/json'
    When method GET
    Then status 401

  Scenario: find link with valid supplychainid and optionalHash should return a 200
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLink), keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    Given path linkPath
    And param optionalHash = '74a88c1cb96211a8f648af3509a1207b2d4a15c0202cfaa10abad8cc26300c63'
    When method GET
    Then status 200
    And match response[*] contains read('classpath:testmessages/link/valid-link-response.json')

  Scenario: user with READ local permission can find link with valid supplychainid and optionalHash should return a 200
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["READ"]}
    * call read('classpath:feature/account/set-local-permissions.feature') {accountId: #(info.accountId), labelId: #(supplyChain.response.parentLabelId), permissions: '["READ"]'}
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLink), keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    Given path linkPath
    And param optionalHash = '74a88c1cb96211a8f648af3509a1207b2d4a15c0202cfaa10abad8cc26300c63'
    When method GET
    Then status 200
    And match response[*] contains read('classpath:testmessages/link/valid-link-response.json')

  Scenario: user without READ local permission cannot find link with valid supplychainid and optionalHash should return a 403
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["LINK_ADD"]}
    * call read('classpath:feature/account/set-local-permissions.feature') {accountId: #(info.accountId), labelId: #(supplyChain.response.parentLabelId), permissions: '["LINK_ADD"]'}
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLink), keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    Given path linkPath
    And param optionalHash = '74a88c1cb96211a8f648af3509a1207b2d4a15c0202cfaa10abad8cc26300c63'
    When method GET
    Then status 403
