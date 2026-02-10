Feature: User management

  Scenario: Create and fetch user
    Given the database is clean
    When I create a user with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    Then the response status should be 200
    And the response should include user handle "adal"
    And the user "adal" has role "basic" with action "user:write"
    And I request a token for username "adal" with password "password123"
    When I fetch the user "adal"
    Then the response status should be 200
    And the response should include user email "ada@lovelace.com"
    When I list users
    Then the response should include 1 users

  Scenario: Update and delete user
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    And the user "adal" has role "basic" with action "user:write"
    And I request a token for username "adal" with password "password123"
    When I update user "adal" with:
      | firstName | lastName | email             | handle | username | password    |
      | Ada       | Lovelace | ada2@lovelace.com | ada    | ada2     | newpassword |
    Then the response status should be 200
    And the response should include user handle "ada"
    When I delete user "ada"
    Then the response status should be 204
    When I list users
    Then the response should include 0 users

  Scenario: Reject duplicate handle on create
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    When I create a user with:
      | firstName | lastName | email              | handle | username | password    |
      | Grace     | Hopper   | grace@hopper.com   | adal   | ghopper  | password456 |
    Then the response status should be 409

  Scenario: Reject duplicate email on create
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    When I create a user with:
      | firstName | lastName | email            | handle   | username | password    |
      | Grace     | Hopper   | ada@lovelace.com | ghopper  | ghopper  | password456 |
    Then the response status should be 409

  Scenario: Reject duplicate username on create
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    When I create a user with:
      | firstName | lastName | email            | handle   | username | password    |
      | Grace     | Hopper   | grace@hopper.com | ghopper  | adal     | password456 |
    Then the response status should be 409

  Scenario: Reject duplicate handle on update
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    And a user exists with:
      | firstName | lastName | email            | handle   | username | password    |
      | Grace     | Hopper   | grace@hopper.com | ghopper  | ghopper  | password456 |
    And the user "adal" has role "basic" with action "user:write"
    And I request a token for username "adal" with password "password123"
    When I update user "ghopper" with:
      | firstName | lastName | email            | handle | username | password    |
      | Grace     | Hopper   | grace@hopper.com | adal   | ghopper  | newpassword |
    Then the response status should be 409

  Scenario: Reject duplicate email on update
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    And a user exists with:
      | firstName | lastName | email            | handle   | username | password    |
      | Grace     | Hopper   | grace@hopper.com | ghopper  | ghopper  | password456 |
    And the user "adal" has role "basic" with action "user:write"
    And I request a token for username "adal" with password "password123"
    When I update user "ghopper" with:
      | firstName | lastName | email            | handle   | username | password    |
      | Grace     | Hopper   | ada@lovelace.com | ghopper  | ghopper  | newpassword |
    Then the response status should be 409

  Scenario: Reject duplicate username on update
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    And a user exists with:
      | firstName | lastName | email            | handle   | username | password    |
      | Grace     | Hopper   | grace@hopper.com | ghopper  | ghopper  | password456 |
    And the user "adal" has role "basic" with action "user:write"
    And I request a token for username "adal" with password "password123"
    When I update user "ghopper" with:
      | firstName | lastName | email            | handle   | username | password    |
      | Grace     | Hopper   | grace@hopper.com | ghopper  | adal     | newpassword |
    Then the response status should be 409

  Scenario: Reject missing username on create
    Given the database is clean
    When I create a user with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   |          | password123 |
    Then the response status should be 400
