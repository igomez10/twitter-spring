Feature: Tweet management

  Scenario: Create and manage a tweet
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    When I create a tweet with content "Hello world" for user "adal"
    Then the response status should be 200
    And the response should include tweet content "Hello world"
    When I fetch the tweet
    Then the response status should be 200
    And the response should include tweet content "Hello world"
    When I update the tweet with content "Updated tweet"
    Then the response status should be 200
    And the response should include tweet content "Updated tweet"
    When I delete the tweet
    Then the response status should be 204
    When I list tweets
    Then the response should include 0 tweets

  Scenario: Fetch deleted tweet returns not found
    Given the database is clean
    And a user exists with:
      | firstName | lastName | email            | handle | username | password    |
      | Ada       | Lovelace | ada@lovelace.com | adal   | adal     | password123 |
    When I create a tweet with content "Hello world" for user "adal"
    And I delete the tweet
    And I fetch the tweet
    Then the response status should be 404
