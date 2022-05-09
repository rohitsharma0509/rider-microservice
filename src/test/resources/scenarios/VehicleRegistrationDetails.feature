@functional
Feature: Rider Vehicle Registration Controller Test
  Optional description of the feature

  Scenario: Create Rider Vehicle Registration Details
    Given Set POST Rider Vehicle Registration Details service api endpoint
    When Send a POST HTTP request for Vehicle Registration Details
    Then I receive valid HTTP Response Code 201 for Vehicle Registration Details

  Scenario: Update Rider Vehicle Registration Details
    Given Set PUT Rider Vehicle Registration Details service api endpoint
    When Send a PUT HTTP request for Vehicle Registration Details
    Then I receive valid PUT HTTP Response Code 200 for Vehicle Registration Details

  Scenario: GET API Call Of  Rider Vehicle Registration Details By Profile ID
    Given Set GET Rider  Vehicle Registration Details By profile id service api endpoint
    When Send a GET HTTP request with to get Vehicle Registration Details by profile Id
    Then I receive valid HTTP Response Code 200 and Rider Vehicle Registration Details by profile id