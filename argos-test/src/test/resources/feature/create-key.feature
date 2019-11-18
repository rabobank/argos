@ignore
Feature: create a valid public key

  Background:
    * url karate.properties['server.baseurl']

  Scenario: create a supplychain
    Given path '/api/key'
    And request read('../testmessages/valid-key.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 204