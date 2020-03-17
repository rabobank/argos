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

Feature: Permissions

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def token = karate.properties['bearer.token']
    * configure headers = call read('classpath:headers.js') { token: #(token)}

  Scenario: all roles requested from server will return 200
    Given path '/api/permissions/global/role'
    And method GET
    Then status 200
    And match response == [{"id":"#uuid","name":"administrator","permissions":["READ","LOCAL_PERMISSION_EDIT","TREE_EDIT","VERIFY","ASSIGN_ROLE"]},{"id": "#uuid","name": "user","permissions": ["PERSONAL_ACCOUNT_READ"]}]

  Scenario: all local permissions requested from server will return 200
    Given path '/api/permissions'
    And method GET
    Then status 200
    And match response == ["READ","TREE_EDIT","LOCAL_PERMISSION_EDIT","ASSIGN_ROLE","LINK_ADD","LAYOUT_ADD","VERIFY","PERSONAL_ACCOUNT_READ"]
