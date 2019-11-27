@ignore
Feature: create a valid layout

  Background:
    * url karate.properties['server.baseurl']
    * def linkPath = '/api/supplychain/'+ __arg.supplyChainId + '/layout'

  Scenario: store layout with valid specifications should return a 201
    * def signedLayout = call read('classpath:feature/layout/sign-layout.feature') read(__arg.json)
    Given path linkPath
    And request signedLayout.response
    And header Content-Type = 'application/json'
    When method POST
    Then status 201