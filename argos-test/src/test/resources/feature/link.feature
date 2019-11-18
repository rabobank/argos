Feature: Link

  Background:
    * url karate.properties['server.baseurl']
    * call read('reset.feature')
    * def result = call read('create-supplychain.feature') { name: 'name'}
    * def linkPath = '/api/supplychain/'+ result.response.id + '/link/'

  Scenario: store link with valid specifications should return a 204
    * def linkresult = call read('create-validlink.feature') {id:#(result.response.id)}

  Scenario: store link with invalid specifications should return a 400 error
    Given path linkPath
    And request read('../testmessages/invalid-link.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 400
    And match response contains read('../testmessages/invalid-link-response.json')

  Scenario: find link with valid supplychainid should return a 200
    * call read('create-validlink.feature') {id:#(result.response.id)}
    Given path linkPath
    And param supplyChainId = result.response.id
    When method GET
    Then status 200
    And match response[*] contains read('../testmessages/valid-link-response.json')

  Scenario: find link with valid supplychainid and optionalHash should return a 200
    * call read('create-validlink.feature') {id:#(result.response.id)}
    Given path linkPath
    And param supplyChainId = result.response.id
    And param optionalHash = '74a88c1cb96211a8f648af3509a1207b2d4a15c0202cfaa10abad8cc26300c63'
    When method GET
    Then status 200
    And match response[*] contains read('../testmessages/valid-link-response.json')