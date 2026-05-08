Feature: Verify login functionality of the application

  Background:
    Given The user navigates to the application "https://automationpracticehub.com/"

  @C09
  Scenario Outline: Login to the application and verify successful login with valid credentials
    When The user enters username "<username>" and password "<password>"
    And The user clicks on the login button
    Then The user should be redirected to the dashboard

    Examples:
      | username          | password               |
      | sagesyntaxacademy | BuildingExcellence@111 |
  @C10
  Scenario Outline: Login to the application and verify successful login with valid credentials
    When The user enters username "<username>" and password "<password>"
    And The user clicks on the login button
    Then The user should be redirected to the dashboard
    And The user will click on Product menu
    And The user will click on AddtoCart button
    And The user will check for the success message

    Examples:
      | username          | password               |
      | sagesyntaxacademy | BuildingExcellence@111 |