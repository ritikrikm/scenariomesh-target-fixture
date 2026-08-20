@regression @payments
Feature: payment_review

  Scenario Outline: payment_review <id>
    Given an account workflow "<id>"
    When the workflow is submitted
    Then workflow "<id>" completes

    Examples:
      | id |
      | case-00003 |
      | case-00004 |
