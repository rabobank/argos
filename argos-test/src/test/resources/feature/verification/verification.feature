Feature: Verification

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { name: 'name'}
    * def layoutPath = '/api/supplychain/'+ supplyChain.response.id + '/layout'
    * call read('classpath:feature/key/create-key.feature')
    * def supplyChainPath = '/api/supplychain/'+ supplyChain.response.id
    * def supplyChainId = supplyChain.response.id

  Scenario: happy flow
    * def layout = 'classpath:testmessages/verification/layout.json'
    * call read('classpath:feature/layout/create-layout.feature') {supplyChainId:#(supplyChainId), json:#(layout)}
    * def buildStepLink = 'classpath:testmessages/verification/build-step-link.json'
    * call read('classpath:feature/link/create-link.feature') {supplyChainId:#(supplyChainId), json:#(buildStepLink)}
    * def testStepLink = 'classpath:testmessages/verification/test-step-link.json'
    * call read('classpath:feature/link/create-link.feature') {supplyChainId:#(supplyChainId), json:#(testStepLink)}
    Given path supplyChainPath + '/verification'
    And request {"expectedProducts": [{"uri": "target/argos-test-0.0.1-SNAPSHOT.jar","hash": "49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162"}]}
    And header Content-Type = 'application/json'
    When method POST
    Then status 200
    And match response == {"runIsValid":true}

