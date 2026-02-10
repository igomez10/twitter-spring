Feature: OAuth token issuance

  Scenario: Issue token with permitted actions
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    And the user "adal" has role "basic" with action "tweet:read"
    When I request a token for username "adal" with password "password123"
    Then the response status should be 200
    And the response should have an access token
    And the response should include action "tweet:read"

  Scenario: Reject invalid credentials
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    When I request a token for username "adal" with password "wrongpassword"
    Then the response status should be 401

  Scenario: Issue token without roles returns no actions
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    When I request a token for username "adal" with password "password123"
    Then the response status should be 200
    And the response should have an access token
    And the response should include 0 actions
