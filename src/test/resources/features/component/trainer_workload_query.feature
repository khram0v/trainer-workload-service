Feature: Trainer workload query API

  As a trusted internal caller, I want to query a trainer's workload summary.

  Scenario: Requesting workload for an unknown trainer with a valid service token
    Given a valid service token
    When I request the workload summary for trainer "Ghost"
    Then the response status should be 404

  Scenario: Requesting workload without any token is rejected
    Given no service token
    When I request the workload summary for trainer "Ghost"
    Then the response status should be 401

  Scenario: Requesting workload with a non-service token is rejected
    Given a non-service token
    When I request the workload summary for trainer "Ghost"
    Then the response status should be 401
