@functional
Feature: Rider Details Controller Test
  Optional description of the feature

  Scenario: GET API Call Of Rider Details By id
    Given Set GET Rider Details by id service api endpoint
    When Send a GET HTTP request with profile id for rider details
    Then I receive valid HTTP Response Code 200 and Rider Details by Id

  Scenario: GET API Call Of Rider Details By phoneNumber
    Given Set GET Rider Details by phoneNumber service api endpoint
    When Send a GET HTTP request with rider phoneNumber for rider details
    Then I receive valid HTTP Response Code 200 and Rider Details by phoneNumber

  Scenario: GET API Call Of Rider Details with Documents By id
    Given Set GET Rider Details with Documents by id service api endpoint
    When Send a GET HTTP request with rider id for rider details with documents
    Then I receive valid HTTP Response Code 200 and Rider Details with documents by id