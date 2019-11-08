Feature: Link
  Background:
    * url karate.properties['server.baseurl']

  Scenario: store link with invalid specifications should return a 400 error
    Given path '/api/link/supplychainid'
    And request {"signature":{"keyId":"keyId1","signature":"sig1"},"link":{"command":["command1","command2"],"materials":[{"uri":"materialsUri1","hash":"materialsHash1"},{"uri":"materialsUri2","hash":"materialsHash2"}],"stepName":"step name","products":[{"uri":"productsUri1","hash":"productsHash1"},{"uri":"productsUri12","hash":"productsHash2"}]}}
    And header Content-Type = 'application/json'
    When method POST
    Then status 400