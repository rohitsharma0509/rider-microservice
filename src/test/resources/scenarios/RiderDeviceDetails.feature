@functional
Feature: Rider Device Details Controller Test
  Optional description of the feature
  @ignore
  Scenario: POST API Call with Rider Device information
    Given Set End Point to create Rider Device Information
    When Send a POST HTTP request with profile id and rider device information
    Then I receive valid HTTP Response Code 201 and Rider Device information with id
  @ignore
  Scenario: GET API Call Of Rider Device Details By Rider Profile Id
    Given Set GET End point for Rider Device by rider profile id
    When Send a GET HTTP request with rider profile id to fetch rider device information
    Then I receive valid HTTP GET Response Code 200 and Rider Device information with id