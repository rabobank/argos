Feature: Layout

  Background:
    * url karate.properties['server.baseurl']
    * call read('reset.feature')
    * def supplyChain = call read('create-supplychain.feature') { name: 'name'}
    * def layoutPath = '/api/supplychain/'+ supplyChain.response.id + '/layout'
    * call read('create-key.feature')

  Scenario: store layout with valid specifications should return a 200
    * call read('create-validlayout.feature') {supplyChainId:#(supplyChain.response.id)}

  Scenario: store link with invalid specifications should return a 400 error
    Given path layoutPath
    And request read('../testmessages/invalid-layout.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 400
    And match response contains read('../testmessages/invalid-layout-response.json')

  Scenario: find layout with valid supplychainid should return a 200
    * def layoutResponse = call read('create-validlayout.feature') {supplyChainId:#(supplyChain.response.id)}
    Given path layoutPath
    When method GET
    Then status 200
    * def layoutId = layoutResponse.response.id
    * def response = read('../testmessages/valid-layout-response.json')
    And match response[*] contains response

  Scenario: update a layout should return a 200
    * def layoutResponse = call read('create-validlayout.feature') {supplyChainId:#(supplyChain.response.id)}
    * def layoutId = layoutResponse.response.id
    * def requestBody = call read('sign-layout.feature') read('../testmessages/valid-update-layout.json')
    Given path layoutPath + '/' + layoutId
    And request requestBody
    And header Content-Type = 'application/json'
    When method PUT
    Then status 200
    * def layoutId = layoutResponse.response.id
    * def expectedResponse = read('../testmessages/valid-update-layout.json')
    And match response contains expectedResponse