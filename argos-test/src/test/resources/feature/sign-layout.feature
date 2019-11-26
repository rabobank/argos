@ignore
Feature: sign layout

  Background:
    * url karate.properties['server.baseurl']
    * def layoutToBeSigned = __arg

  Scenario: sign the layout should return 200
    Given path '/integration-test/signLayoutMetaBlock'
    And request layoutToBeSigned
    And header Content-Type = 'application/json'
    When method POST
    Then status 200