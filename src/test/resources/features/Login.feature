Feature: Verify login functionality of the application

  Background:
    Given The user navigates to the application "https://automationpracticehub.com/"

  @C46
  Scenario Outline: Login to the application and verify successful login with valid credentials
    When The user enters username "<username>" and password "<password>"
    And The user clicks on the login button
    Then The user should be redirected to the dashboard

    Examples:
      | username          | password               |
      | sagesyntaxacademy | BuildingExcellence@111 |

  @C52
  Scenario Outline: Login to the application and the user will add multiple product in the cart
    When The user enters username "<username>" and password "<password>"
    And The user clicks on the login button
    Then The user should be redirected to the dashboard
    And The user will click on Product menu
    And The user will click on AddtoCart button for product1 "<product1>" and for product2 "<product2>" and for product3 "<product3>"
    And The user will check for the success message

    Examples:
      | username          | password               | product1 | product2 | product3 |
      | sagesyntaxacademy | BuildingExcellence@111 | iPhone   | Camera   | Purse    |