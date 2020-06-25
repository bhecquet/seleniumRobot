@core
Feature: Core

	Scenario Outline: core_ <text>
		
		When write <text>
		
		Examples:
		| text |
		| tata |
		| titi |
		
	
	Scenario Outline: core_unique_name
		
		When write <text>
		
		Examples:
		| text |
		| tata |
		| titi |
		
	@new
	Scenario: core_3
		When write tutu
		
	@new2
	Scenario: core_4
		When write tuta
		
	@EXCLUDE_FROM_SQUASH_TA
	@new4
	Scenario: core_5
		When write tatu
		
	@new4
	@new5
	Scenario: core_6
		When search
		
	Scenario: error_scenario
		When write_error error 
		
	@EXCLUDE_FROM_SQUASH_TA
	Scenario: my beautiful scenario ?? ok ??
		When write tatu