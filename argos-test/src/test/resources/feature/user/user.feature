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

Feature: User

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def result = call read('classpath:feature/authenticate.feature')
    * def headerAuthorization = 'Bearer ' + result.response.token

  Scenario: get user profile
    Given path '/api/user/me'
    And header Authorization = headerAuthorization
    When method GET
    Then status 200
    Then match response contains {"name":"Luke Skywalker","email":"luke@skywalker.imp"}

  Scenario: update profile
    Given path '/api/user/me'
    And header Authorization = headerAuthorization
    And request {"keyIds": ["keyId1","keyId2"]}
    When method PUT
    Then status 200
    Then match response contains {"name":"Luke Skywalker","email":"luke@skywalker.imp", "keyIds": ["keyId1","keyId2"]}