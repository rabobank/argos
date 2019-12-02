@ignore
Feature: sign link

  Background:
    * url karate.properties['server.baseurl']
    * def linkToBeSigned = __arg

  Scenario: sign the layout should return 200
    Given path '/integration-test/signLinkMetaBlock'
    And request linkToBeSigned
    And header Content-Type = 'application/json'
    When method POST
    Then status 200