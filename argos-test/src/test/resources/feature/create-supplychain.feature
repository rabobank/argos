@ignore
Feature: using __arg

  Background:
    * url karate.properties['server.baseurl']

  Scenario: create a supplychain
    Given path '/api/supplychain'
    And request __arg
    And header Content-Type = 'application/json'
    When method POST
    Then status 201