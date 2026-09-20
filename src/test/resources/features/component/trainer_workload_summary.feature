Feature: Trainer workload summary queries

  Background:
    Given a unique trainer username

  Scenario: The full summary reflects all years and months in sorted order
    Given an ADD workload event of 60 minutes on "2024-06-15" is published for the trainer
    And an ADD workload event of 45 minutes on "2024-03-10" is published for the trainer
    And an ADD workload event of 30 minutes on "2023-12-01" is published for the trainer
    And the trainer's workload for "2023-12-01" eventually shows 30 minutes
    When I request the full workload summary for the trainer
    Then the response status should be 200
    And the summary years should be in order 2023, 2024
    And the 2024 summary months should be in order 3, 6

  Scenario: The summary reflects the trainer's active status from the latest event
    Given an ADD workload event of 60 minutes is published for the trainer with active status false
    And the trainer's workload for that month eventually shows 60 minutes
    When I request the full workload summary for the trainer
    Then the response status should be 200
    And the summary trainer status should be false

  Scenario: A month with no recorded training returns zero, not a 404
    Given an ADD workload event of 60 minutes is published for the trainer
    And the trainer's workload for that month eventually shows 60 minutes
    When I request the monthly workload for the trainer for year 2024 and month 1
    Then the response status should be 200
    And the monthly training duration should be 0

  Scenario: Requesting the monthly workload for an unknown trainer returns 404
    When I request the monthly workload for "Ghost.Trainer" for year 2024 and month 6
    Then the response status should be 404
