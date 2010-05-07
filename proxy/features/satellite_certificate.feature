Feature: Load entitlement data from a Satellite Certificate
    As an administrator
    I can populate entitlement data from a Satellite Certificate

    Scenario: List products from a Satellite Certificate
        Given I am user "Spacewalk Public Cert" with password "testuserpass"
        And I have imported a Satellite Certificate
        Then I should have at least 5 products available

    Scenario: Upload a duplicate Satellite Certificate as the same user
        Given I am user "Spacewalk Public Cert" with password "testuserpass"
        And I have imported a Satellite Certificate
        Then importing the Satelite Certificate again should cause a bad request