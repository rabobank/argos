Feature: Link

  Background:
    * url karate.properties['server.baseurl']
    * call read('reset.feature')
    * def result = call read('create-supplychain.feature') { name: 'name'}
    * def linkPath = '/api/supplychain/'+ result.response.id + '/layout'

  Scenario: store layout with valid specifications should return a 201
    * def linkresult = call read('create-validlink.feature') {id:#(result.response.id)}

  Scenario: store link with invalid specifications should return a 400 error
    Given path linkPath
    And request read('../testmessages/invalid-layout.json')
    And header Content-Type = 'application/json'
    When method POST
    Then status 400
    And match response contains read('../testmessages/invalid-layout-response.json')

  Scenario: find layout with valid supplychainid should return a 200
    * call read('create-validlayout.feature') {id:#(result.response.id)}
    Given path linkPath
    When method GET
    Then status 200
    * def layoutId = result.response.id
    * def response = read('../testmessages/valid-layout-response.json')
    And match response[*] contains response

  Scenario: update a layout should return a 200
    * def layoutResponse = call read('create-validlayout.feature') {id:#(result.response.id)}
    * def layoutId = layoutResponse.response.id
    * def requestBody = read('../testmessages/valid-update-layout.json')
    Given path linkPath + '/' + layoutId
    And request requestBody
    And header Content-Type = 'application/json'
    When method PUT
    Then status 200
    * def layoutId = layoutResponse.response.id
    * def expectedResponse = read('../testmessages/valid-update-layout.json')
    And match response contains expectedResponse