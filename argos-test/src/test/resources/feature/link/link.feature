Feature: Link

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { name: 'name'}
    * def linkPath = '/api/supplychain/'+ supplyChain.response.id + '/link'
    * def validLink = 'classpath:testmessages/link/valid-link.json'

  Scenario: store link with valid specifications should return a 204
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLink)}

  Scenario: store link with invalid specifications should return a 400 error
    Given path linkPath
    And request read('classpath:testmessages/link/invalid-link.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/link/invalid-link-response.json')

  Scenario: find link with valid supplychainid should return a 200
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLink)}
    Given path linkPath
    When method GET
    Then status 200
    And match response[*] contains read('classpath:testmessages/link/valid-link-response.json')

  Scenario: find link with valid supplychainid and optionalHash should return a 200
    * call read('create-link.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLink)}
    Given path linkPath
    And param optionalHash = '74a88c1cb96211a8f648af3509a1207b2d4a15c0202cfaa10abad8cc26300c63'
    When method GET
    Then status 200
    And match response[*] contains read('classpath:testmessages/link/valid-link-response.json')