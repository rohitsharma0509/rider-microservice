@functional
Feature: As ops team member, I should be able to use search functionality in operations portal
  Optional description of the feature

	Scenario: Verify if search functionality is returning correct values when name of the rider is entered in search box
		Given I successfully log into the portal
		And I click on hamburger menu
		When I see Rider Management, Pricing Management, Training Management, Logout options
		And I click on Rider Management
		And I enter the name of the rider 'UNAUTH' in the search box
		Then I should see the data returned to have same name 'UNAUTHORIZED'	
		

		Scenario: Verify if search functionality is returning correct values when name of the rider is entered in search box
		Given I successfully log into the portal
		And I click on hamburger menu
		When I see Rider Management, Pricing Management, Training Management, Logout options
		And I click on Rider Management
		And I enter the mobile number of the rider '99' in the search box
		Then I should see the data returned to have same mobile number '99'