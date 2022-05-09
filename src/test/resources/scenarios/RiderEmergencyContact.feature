@functional
Feature: Rider Profile Controller Test
  Optional description of the feature

  Scenario: Create Rider Emergency Contact Details
    Given Set POST Rider Emergency Contact service api endpoint
    When Send a POST Emergency Contact request
    Then I receive valid HTTP Status Code for emergency contact 200

  #Scenario: Create Rider Emergency Contact Details
    #Given Set POST Rider Emergency Contact service api endpoint
    #When Send a POST Emergency Contact request
    #Then I receive valid HTTP Status Code 200

  Scenario: GET API Call Of Rider Emergency Contact By id
    Given Set GET Rider Emergency Contact by id service api endpoint
    When Send a GET HTTP request with Emergency Contact id
    Then I receive valid HTTP Response Code 200 and Rider Emergency Contact by Id