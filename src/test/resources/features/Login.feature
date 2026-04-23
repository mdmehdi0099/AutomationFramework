
Feature: Verify login functionality of the application
  @C09
  Scenario Outline: To login to the application and verify successful login
    Given The user navigates to "<url>"
    When The user enters username "<username>" and password "<password>"
    And The user clicks on the login button
    Then The user should be redirected to the dashboard

    Examples:
      | url                                | username          | password               |
      | https://automationpracticehub.com/ | sagesyntaxacademy | BuildingExcellence@111 |

