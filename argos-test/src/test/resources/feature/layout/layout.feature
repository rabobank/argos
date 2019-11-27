Feature: Layout

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { name: 'name'}
    * def layoutPath = '/api/supplychain/'+ supplyChain.response.id + '/layout'
    * call read('classpath:feature/key/create-key.feature')
    * def validLayout = 'classpath:testmessages/layout/valid-layout.json'

  Scenario: store layout with valid specifications should return a 200
    * call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout)}

  Scenario: store link with invalid specifications should return a 400 error
    Given path layoutPath
    And request read('classpath:testmessages/layout/invalid-layout.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/layout/invalid-layout-response.json')

  Scenario: find layout with valid supplychainid should return a 200
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout)}
    Given path layoutPath
    When method GET
    Then status 200
    * def layoutId = layoutResponse.response.id
    * def response = read('classpath:testmessages/layout/valid-layout-response.json')
    And match response[*] contains response

  Scenario: update a layout should return a 200
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout)}
    * def layoutId = layoutResponse.response.id
    * def requestBody = call read('sign-layout.feature') read('classpath:testmessages/layout/valid-update-layout.json')
    Given path layoutPath + '/' + layoutId
    And request requestBody.response
    And header Content-Type = 'application/json'
    When method PUT
    Then status 200
    * def layoutId = layoutResponse.response.id
    * def expectedResponse = read('classpath:testmessages/layout/valid-update-layout.json')
    And match response contains expectedResponse