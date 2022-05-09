@functional
Feature: Rider Background Verification Controller Test

  Scenario: Create Rider Background Verification Details
    Given Set POST Rider Background Verification service api endpoint
    When Send a POST HTTP request for Background Verification Details
    Then I receive valid HTTP Response Code 201 for Background Verification Details

  Scenario: Update Rider Background Verification Details
    Given Set PUT Rider Background Verification service api endpoint
    When Send a PUT HTTP request for Background Verification Details
    Then I receive valid PUT HTTP Response Code 200 for Background Verification Details

  Scenario: GET API Call Of  Rider Background Verification Details By Profile ID
    Given Set GET Rider  Background Verification Details By profile id service api endpoint
    When Send a GET HTTP request with to get Background Verification Details by profile Id
    Then I receive valid HTTP Response Code 200 and Rider Background Verification Details by profile id