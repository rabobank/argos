Feature: Keypair
  Background:
    * url karate.properties['server.baseurl']
    * call read('reset.feature')

  Scenario: store key with valid key should return a 204
    Given path '/api/key'
    And request  {"keyId":"1849b73bf08d11cec1aca990d2682566a3ccca3bc216d92f684dea34796772c3","publicKey":"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC/Ldm84IhBvssdweZOZSPcx87J0Xy63g0JhlOYlr66aKmbXz5YD+J+b4NlIIbvaa5sEg4FS0+gkOPgexqCzgRUqHK5coLchpuLFggmDiL4ShqGIvqb/HPq7Aauk8Ss+0TaHfkJjd2kEBPRgWLII1gytjKkqlRGD/LxRtsppnleQwIDAQAB","encryptedPrivateKey":null}
    And header Content-Type = 'application/json'
    When method POST
    Then status 204

  Scenario: store key with invalid key should return a 400 error
    Given path '/api/key'
    And request {"keyId": "invalidkeyid","publicKey": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC/Ldm84IhBvssdweZOZSPcx87J0Xy63g0JhlOYlr66aKmbXz5YD+J+b4NlIIbvaa5sEg4FS0+gkOPgexqCzgRUqHK5coLchpuLFggmDiL4ShqGIvqb/HPq7Aauk8Ss+0TaHfkJjd2kEBPRgWLII1gytjKkqlRGD/LxRtsppnleQwIDAQAB","encryptedPrivateKey": null}
    And header Content-Type = 'application/json'
    When method POST
    Then status 400
    And match response contains {"message":"keyId:must match \"^[0-9a-f]*$\", keyId:size must be between 64 and 64"}
