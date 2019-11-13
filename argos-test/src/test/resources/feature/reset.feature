@ignore
Feature: reset

  Background:
    * url karate.properties['server.baseurl']

  Scenario:
    Given path '/integration-test/reset-db'
    And  request {}
    When method post
    Then status 200

