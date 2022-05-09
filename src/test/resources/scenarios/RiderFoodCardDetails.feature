@functional
Feature: Rider Food Card Controller Test

  Scenario: Create Rider Food Card Details
    Given Set POST Rider Food Card service api endpoint
    When Send a POST HTTP request for Food Card Details
    Then I receive valid HTTP Response Code 201 for Food Card Details

  Scenario: Update Rider Food Card Details
    Given Set PUT Rider Food Card service api endpoint
    When Send a PUT HTTP request for Food Card Details
    Then I receive valid PUT HTTP Response Code 200 for Food Card Details

  Scenario: GET API Call Of  Rider Food Card Details By Profile ID
    Given Set GET Rider  Food Card Details By profile id service api endpoint
    When Send a GET HTTP request with to get Food Card Details by profile Id
    Then I receive valid HTTP Response Code 200 and Rider Food Card Details by profile id