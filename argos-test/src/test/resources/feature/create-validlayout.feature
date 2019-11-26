@ignore
Feature: create a valid link

  Background:
    * url karate.properties['server.baseurl']
    * def linkPath = '/api/supplychain/'+ __arg.supplyChainId + '/layout'

  Scenario: store link with valid specifications should return a 204
    * def layoutToBeSigned = read('../testmessages/valid-layout.json')
    * def signedLayout = call read('sign-layout.feature') layoutToBeSigned
    Given path linkPath
    And request signedLayout.response
    And header Content-Type = 'application/json'
    When method POST
    Then status 201