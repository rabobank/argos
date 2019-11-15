@ignore
Feature: create a valid link

  Background:
    * url karate.properties['server.baseurl']
    * def linkPath = '/api/supplychain/'+ __arg.id + '/link/'

  Scenario: store link with valid specifications should return a 204
    Given path linkPath
    And request read('../testmessages/valid-link.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 204