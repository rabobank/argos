Feature: create supply chain for jenkins it

  Background:
    * url karate.properties['server.baseurl']
    * call read('reset.feature')
    * def result = call read('create-supplychain.feature') { name: 'argos-test-app'}
