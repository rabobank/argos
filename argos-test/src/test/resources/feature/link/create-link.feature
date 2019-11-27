@ignore
Feature: create a valid link

  Background:
    * url karate.properties['server.baseurl']
    * def linkPath = '/api/supplychain/'+ __arg.supplyChainId + '/link'

  Scenario: store link with valid specifications should return a 204
    * def signedLink = call read('classpath:feature/link/sign-link.feature') read(__arg.json)
    Given path linkPath
    And request signedLink.response
    And header Content-Type = 'application/json'
    When method POST
    Then status 204