@functional
Feature: Rider Driving License Controller Test
  Optional description of the feature

  Scenario: Create Rider Driving License Details
    Given Set POST Rider Driving License Details service api endpoint
    When Send a POST HTTP request
    Then I receive valid HTTP Response Code 201

  Scenario: Update Rider Driving License Details
    Given Set PUT Rider Driving License Details service api endpoint
    When Send a PUT HTTP request
    Then I receive valid Updated Details and HTTP Response Code 200

  Scenario: GET API Call Of Rider Driving License By id
    Given Set GET Rider Driving License Details by id service api endpoint
    When Send a GET HTTP request with driving license id
    Then I receive valid HTTP Response Code 200 and Rider Retails by Id

  Scenario: GET API Call Of Rider Driving License By Profile ID
    Given Set GET Rider Driving License Details By profile id service api endpoint
    When Send a GET HTTP request with rider profile id
    Then I receive valid HTTP Response Code 200 and Rider Driving License Details by profile id