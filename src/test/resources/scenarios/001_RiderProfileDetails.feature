@functional
Feature: Rider Profile Controller Test
  Optional description of the feature

  Scenario: Create Rider Profile Details
    Given Set POST Rider Profile service api endpoint
    When Send a POST request with rider profile
    Then I receive valid HTTP Status Code 201

  Scenario: Update Rider Status
    Given Set PUT Update Rider Status
    When Send a PUT HTTP request with riderId and status
    Then I receive valid HTTP Response Code 200 and rider status details

  Scenario: Update Rider Profile Details
    Given Set PUT Rider Profile service api endpoint
    When Send a PUT request
    Then I receive valid Updated Details and HTTP Status Code 200

  Scenario: GET API Call Of Rider Profile By id
    Given Set GET Rider Profile by id service api endpoint
    When Send a GET HTTP request with profile id
    Then I receive valid HTTP Response Code 200 and Rider Profile by Id

  Scenario: Set Status for Rider
    Given Set GET Rider Status by id service api endpoint
    When Send a GET HTTP request with status
    Then I receive valid HTTP Response Code 200 for status

  Scenario: Fetch Riders on the basis of availability status and zone
    Given Set GET End Point for Riders Ids on the basis of availability status and zone
    When Send a GET HTTP request with status and zone
    Then I receive valid HTTP Response Code 200 for status and response list

  Scenario: Update Rider National Id status
      Given Set PUT Update Rider National Id status
      When Send a PUT HTTP request with riderId and nationalId status
      Then I receive valid HTTP Response Code 200 and isUpdated flag

  Scenario: Update Rider Profile Photo status
        Given Set PUT Update Rider profile photo status
        When Send a PUT HTTP request with riderId and profile photo status
        Then I receive valid HTTP Response Code 200 and isStatusUpdated flag