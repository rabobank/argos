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

Feature: Hierarchy

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def root1 = call read('classpath:feature/label/create-label.feature') { name: 'root1'}
    * def root2 = call read('classpath:feature/label/create-label.feature') { name: 'root2'}
    * def root3 = call read('classpath:feature/label/create-label.feature') { name: 'root3'}
    * def root1Children = [{ name: 'childaroot1',parentLabelId:#(root1.response.id)},{ name: 'childbroot1',parentLabelId:'#(root1.response.id)'}]
    * def root2Children = [{ name: 'childaroot2',parentLabelId:#(root2.response.id)},{ name: 'childbroot2',parentLabelId:'#(root2.response.id)'}]
    * def root3Children = [{ name: 'childaroot3',parentLabelId:#(root3.response.id)},{ name: 'childbroot3',parentLabelId:'#(root3.response.id)'}]
    * call read('classpath:feature/label/create-label.feature') root1Children
    * call read('classpath:feature/label/create-label.feature') root2Children
    * call read('classpath:feature/label/create-label.feature') root3Children

  Scenario: get root nodes with HierarchyMode all should return full trees
    Given path '/api/hierarchy'
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-all.json')
    And match response == expectedResponse

  Scenario: get root nodes with HierarchyMode none should return root entries only
    Given path '/api/hierarchy'
    And param HierarchyMode = 'NONE'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-none.json')
    And match response == expectedResponse

  Scenario: get root nodes with HierarchyMode maxdepth should return maxdepth descendant entries only
    * def root1ChildWithExceedingMaxdepth = call read('classpath:feature/label/create-label.feature')  { name: 'child3root1',parentLabelId:#(root1.response.id)}
    * call read('classpath:feature/label/create-label.feature') { name: 'subchild1child3root1',parentLabelId:#(root1ChildWithExceedingMaxdepth.response.id)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = 1
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-maxdepth.json')
    And match response == expectedResponse

  Scenario: get root nodes with HierarchyMode maxdepth and non positive maxdepth should return validation error
    Given path '/api/hierarchy'
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = -1
    When method GET
    Then status 400
    And match response == {message:'getRootNodes.maxDepth:must be greater than or equal to 1'}

  Scenario: get root nodes with HierarchyMode maxdepth and no maxdepth should return maxdepth 1 descendant entries only
    * def root1ChildWithExceedingMaxdepth = call read('classpath:feature/label/create-label.feature')  { name: 'child3root1',parentLabelId:#(root1.response.id)}
    * call read('classpath:feature/label/create-label.feature') { name: 'subchild1child3root1',parentLabelId:#(root1ChildWithExceedingMaxdepth.response.id)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'MAX_DEPTH'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-maxdepth.json')
    And match response == expectedResponse

  Scenario: get sub tree with HierarchyMode all should return full tree
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-all.json')
    And match response == expectedResponse

  Scenario: get sub tree with HierarchyMode none should return only root
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'NONE'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-none.json')
    And match response == expectedResponse

  Scenario: get sub tree with HierarchyMode max depth 1 should return only direct descendants
    * def root1ChildWithExceedingMaxdepth = call read('classpath:feature/label/create-label.feature')  { name: 'child3root1',parentLabelId:#(root1.response.id)}
    * call read('classpath:feature/label/create-label.feature') { name: 'subchild1child3root1',parentLabelId:#(root1ChildWithExceedingMaxdepth.response.id)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = 1
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-maxdepth.json')
    And match response == expectedResponse

  Scenario: get sub tree with HierarchyMode max depth -1 should return a validation error
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = -1
    When method GET
    Then status 400
    And match response == {message:'getSubTree.maxDepth:must be greater than or equal to 1'}

  Scenario: get sub tree with HierarchyMode maxdepth and no maxdepth should return maxdepth 1 descendant entries only
    * def root1ChildWithExceedingMaxdepth = call read('classpath:feature/label/create-label.feature')  { name: 'child3root1',parentLabelId:#(root1.response.id)}
    * call read('classpath:feature/label/create-label.feature') { name: 'subchild1child3root1',parentLabelId:#(root1ChildWithExceedingMaxdepth.response.id)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'MAX_DEPTH'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-maxdepth.json')
    And match response == expectedResponse