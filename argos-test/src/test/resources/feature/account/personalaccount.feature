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

Feature: Personal Account

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def token = karate.properties['bearer.token']
    * def defaultUsertoken = defaultTestData.personalAccounts['default-pa1'].token
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}

  Scenario: get Personal Account profile should return 200
    * def expectedResponse = read('classpath:testmessages/personal-account/admin-account-response.json')
    Given path '/api/personalaccount/me'
    When method GET
    Then status 200
    Then match response == expectedResponse

  Scenario: createKey should return 204
    Given path '/api/personalaccount/me/key'
    And request read('classpath:testmessages/key/personal-keypair.json')
    When method POST
    Then status 204

  Scenario: createKey with invalid key should return 400
    Given path '/api/personalaccount/me/key'
    And request {"keyId": "invalidkeyid","publicKey": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC/Ldm84IhBvssdweZOZSPcx87J0Xy63g0JhlOYlr66aKmbXz5YD+J+b4NlIIbvaa5sEg4FS0+gkOPgexqCzgRUqHK5coLchpuLFggmDiL4ShqGIvqb/HPq7Aauk8Ss+0TaHfkJjd2kEBPRgWLII1gytjKkqlRGD/LxRtsppnleQwIDAQAB","encryptedPrivateKey": null}
    When method POST
    Then status 400

  Scenario: getKey should return 200
    * def keyPair = read('classpath:testmessages/key/personal-keypair.json')
    Given path '/api/personalaccount/me/key'
    And request keyPair
    When method POST
    Then status 204
    Given path '/api/personalaccount/me/key'
    When method GET
    Then status 200
    Then match response == keyPair


  Scenario: get account by id should return a 200
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * def expectedResponse = read('classpath:testmessages/personal-account/get-account-by-id-response.json')
    Given path '/api/personalaccount/'+extraAccount.response.id
    When method GET
    Then status 200
    Then match response == expectedResponse

  Scenario: get account by id without ASSIGN_ROLE should return a 403
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * def expectedResponse = read('classpath:testmessages/personal-account/get-account-by-id-response.json')
    * configure headers = call read('classpath:headers.js') { token: #(defaultUsertoken)}
    Given path '/api/personalaccount/'+extraAccount.response.id
    When method GET
    Then status 403
    And match response == {"message":"Access denied"}

  Scenario: update roles should return 200
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    Given path '/api/personalaccount/'+extraAccount.response.id+'/role'
    And request ["administrator"]
    When method PUT
    Then status 200
    Then match response == {"id":"#(extraAccount.response.id)","name":"Extra Person", "roles": [{"id": "#uuid", "name":"administrator", "permissions" : ["READ","LOCAL_PERMISSION_EDIT","TREE_EDIT","VERIFY","ASSIGN_ROLE"] }]}
    Given path '/api/personalaccount/'+extraAccount.response.id
    When method GET
    Then status 200
    Then match response == {"id":"#(extraAccount.response.id)","name":"Extra Person", "roles": [{"id": "#uuid", "name":"administrator", "permissions" : ["READ","LOCAL_PERMISSION_EDIT","TREE_EDIT","VERIFY","ASSIGN_ROLE"] }]}

  Scenario: user without ASSIGN_ROLE permission can not assign a role to a user
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    Given path '/api/personalaccount/'+extraAccount.response.id+'/role'
    And request ["administrator"]
    When method PUT
    Then status 403
    And match response == {"message":"Access denied"}

  Scenario: search personal account by role name should return 200
    * configure headers = call read('classpath:headers.js') { token: #(defaultUsertoken)}
    Given path '/api/personalaccount'
    And param roleName = 'administrator'
    When method GET
    Then status 200
    And match response == [{"id":"#uuid","name":"Luke Skywalker"}]

  Scenario: search all personal account 200
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * def expectedResponse = read('classpath:testmessages/personal-account/account-search-all-response.json')
    * configure headers = call read('classpath:headers.js') { token: #(defaultUsertoken)}
    Given path '/api/personalaccount'
    When method GET
    Then status 200
    And match response == expectedResponse

  Scenario: search personal account without PERSONAL_ACCOUNT_READ should return a 403
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    Given path '/api/personalaccount'
    When method GET
    Then status 403
    And match response == {"message":"Access denied"}

  Scenario: search personal account by name should return 200
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(defaultUsertoken)}
    Given path '/api/personalaccount'
    And param name = 'per'
    When method GET
    Then status 200
    And match response == [{"id":"#uuid","name":"Extra Person"}]

  Scenario: search by local permission label id personal account 200
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'search@extra.go'}
    * def label = call read('classpath:feature/label/create-label.feature') { name: 'label1'}
    Given path '/api/personalaccount'
    And param localPermissionsLabelId = label.response.id
    When method GET
    Then status 200
    And match response == []

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    And request ["VERIFY","READ"]
    When method PUT
    Then status 200

    Given path '/api/personalaccount'
    And param localPermissionsLabelId = label.response.id
    When method GET
    Then status 200
    And match response == [{"id":"#uuid","name":"Extra Person"}]

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    And request []
    When method PUT
    Then status 204

    Given path '/api/personalaccount'
    And param localPermissionsLabelId = label.response.id
    When method GET
    Then status 200
    And match response == []

  Scenario: manage local permissions
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'local.permissions@extra.go'}
    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission'
    When method GET
    Then status 200
    And match response == []
    * def label = call read('classpath:feature/label/create-label.feature') { name: 'label1'}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    When method GET
    Then status 200
    And match response == {"labelId": "#(label.response.id)", "permissions": []}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    And request ["READ"]
    When method PUT
    Then status 200
    And match response == {"labelId": "#(label.response.id)", "permissions": ["READ"]}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    And request ["VERIFY","READ"]
    When method PUT
    Then status 200
    And match response == {"labelId": "#(label.response.id)", "permissions": ["VERIFY","READ"]}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    When method GET
    Then status 200
    And match response == {"labelId": "#(label.response.id)", "permissions": ["VERIFY","READ"]}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission'
    When method GET
    Then status 200
    And match response == [{"labelId": "#(label.response.id)", "permissions": ["VERIFY","READ"]}]

  Scenario: a user with local permission LOCAL_PERMISSION_EDIT can manage local permissions
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email:'local.permissions@extra.go'}
    * def rootLabel = call read('classpath:feature/label/create-label.feature') { name: 'root_label'}
    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+rootLabel.response.id
    And request ["LOCAL_PERMISSION_EDIT"]
    When method PUT
    Then status 200

    * def childLabel = call read('classpath:feature/label/create-label.feature') { name: 'child_label', parentLabelId: #(rootLabel.response.id)}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    * def anotherAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Another Person', email: 'another@extra.go'}

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission/'+childLabel.response.id
    And request ["READ"]
    When method PUT
    Then status 200

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission/'+childLabel.response.id
    When method GET
    Then status 200
    And match response == {"labelId": "#(childLabel.response.id)", "permissions": ["READ"]}

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission'
    When method GET
    Then status 403
    And match response == {"message":"Access denied"}

  Scenario: a user without local permission LOCAL_PERMISSION_EDIT can not manage local permissions
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'local.permissions@extra.go'}
    * def rootLabel = call read('classpath:feature/label/create-label.feature') { name: 'root_label'}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+rootLabel.response.id
    And request ["READ"]
    When method PUT
    Then status 200

    * def childLabel = call read('classpath:feature/label/create-label.feature') { name: 'child_label', parentLabelId: #(rootLabel.response.id)}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    * def anotherAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Another Person', email: 'another@extra.go'}

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission/'+childLabel.response.id
    And request ["READ"]
    When method PUT
    Then status 403
    And match response == {"message":"Access denied"}

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission/'+childLabel.response.id
    When method GET
    Then status 403
    And match response == {"message":"Access denied"}

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission'
    When method GET
    Then status 403
    And match response == {"message":"Access denied"}