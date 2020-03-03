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
Feature: insert test key pairs

  Background:
    * url karate.properties['server.baseurl']
    * def parentLabelId = __arg.parentLabelId;

  Scenario: store key pair 1
    * call read('classpath:feature/account/create-non-personal-account-with-key.feature') {accountName: 'npa1', parentLabelId: #(parentLabelId), keyFile: 'keypair1'}

  Scenario: store key pair 2
    * call read('classpath:feature/account/create-non-personal-account-with-key.feature') {accountName: 'npa2', parentLabelId: #(parentLabelId), keyFile: 'keypair2'}

  Scenario: store key pair 3
    * call read('classpath:feature/account/create-non-personal-account-with-key.feature') {accountName: 'npa3', parentLabelId: #(parentLabelId), keyFile: 'keypair3'}

  Scenario: store key pair 4
    * call read('classpath:feature/account/create-non-personal-account-with-key.feature') {accountName: 'npa4', parentLabelId: #(parentLabelId), keyFile: 'keypair4'}

  Scenario: store key pair 5
    * call read('classpath:feature/account/create-non-personal-account-with-key.feature') {accountName: 'npa5', parentLabelId: #(parentLabelId), keyFile: 'keypair5'}