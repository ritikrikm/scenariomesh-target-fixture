@regression @accounts
Feature: account_search

  Scenario Outline: account_search <id>
    Given an account workflow "<id>"
    When the workflow is submitted
    Then workflow "<id>" completes

    Examples:
      | id |
      | case-00001 |
      | case-00002 |
