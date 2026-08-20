Feature: ScenarioMesh isolated worker fixture
  A deterministic Selenium workload used to verify scenario discovery, execution, and isolation.

  Scenario Outline: Execute isolated fixture scenario <id>
    Given fixture scenario "<id>" opens an isolated page
    When the scenario writes its unique id
    Then only id "<id>" is visible

    Examples:
      | id             |
      | scenario-00001 |
      | scenario-00002 |
      | scenario-00003 |
      | scenario-00004 |
