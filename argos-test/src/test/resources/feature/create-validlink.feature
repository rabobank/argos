@ignore
Feature: create a valid link

  Background:
    * url karate.properties['server.baseurl']
    * def linkPath = '/api/supplychain/'+ __arg.supplyChainId + '/link'
    * call read('create-key.feature')

  Scenario: store link with valid specifications should return a 204
    * def linkToBeSigned = read('../testmessages/valid-link.json')
    * def signedLink = call read('sign-link.feature') linkToBeSigned
    Given path linkPath
    And request signedLink.response
    And header Content-Type = 'application/json'
    When method POST
    Then status 204