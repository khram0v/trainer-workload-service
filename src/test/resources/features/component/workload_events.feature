Feature: Workload event consumption

  Background:
    Given a unique trainer username

  Scenario: A valid ADD event increases the trainer's monthly workload
    When an ADD workload event of 60 minutes is published for the trainer
    Then the trainer's workload for that month eventually shows 60 minutes

  Scenario: A second ADD event for the same trainer and month accumulates
    Given an ADD workload event of 60 minutes is published for the trainer
    When an ADD workload event of 30 minutes is published for the trainer
    Then the trainer's workload for that month eventually shows 90 minutes

  Scenario: A DELETE event reduces the trainer's monthly workload
    Given an ADD workload event of 90 minutes is published for the trainer
    When a DELETE workload event of 30 minutes is published for the trainer
    Then the trainer's workload for that month eventually shows 60 minutes

  Scenario: A DELETE event exceeding the accumulated total clamps at zero
    Given an ADD workload event of 20 minutes is published for the trainer
    When a DELETE workload event of 60 minutes is published for the trainer
    Then the trainer's workload for that month eventually shows 0 minutes

  Scenario: A malformed event is dead-lettered and never applied
    Given a malformed workload event is published for the trainer
    When an ADD workload event of 15 minutes is published for the trainer
    Then the trainer's workload for that month eventually shows 15 minutes

  Scenario: An event with a blank trainer username is dead-lettered
    Given a workload event with a blank trainer username is published
    When an ADD workload event of 15 minutes is published for the trainer
    Then the trainer's workload for that month eventually shows 15 minutes

  Scenario: An event with a non-positive duration is dead-lettered and never applied
    Given a workload event with a non-positive duration is published for the trainer
    When an ADD workload event of 15 minutes is published for the trainer
    Then the trainer's workload for that month eventually shows 15 minutes
