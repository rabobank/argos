@ignore
Feature: create a valid public key

  Background:
    * url karate.properties['server.baseurl']

  Scenario: store public key for links
    Given path '/api/key'
    And request read('classpath:testmessages/key/valid-key.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 204

  Scenario: store public key for layouts
    Given path '/api/key'
    And request read('classpath:testmessages/key/valid-layout-key.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 204