@functional
Feature: Rider Preferred Zone Controller Test
  Optional description of the feature

  Scenario: Create Rider Preferred Zone Details
    Given  Set POST Rider Preferred Zone service api endpoint
    When   Send a POST request for Preferred Zone
    Then  I receive valid HTTP Status Code for Preferred Zone 200
