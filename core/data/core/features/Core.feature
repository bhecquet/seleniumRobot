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
		
	Scenario Outline: a very long scenàrio outline name which should not have been created but is there but we should not strip it only display a message saying its much too long
		
		When write <text>
		
		Examples:
		| text |
		| tata |
		| titi |
		
	@new
	Scenario: core_3
		When write tutu
		
	# test new annotations
	Scenario: core_7
		When write2 tutu
		
	@new2
	Scenario: core_4
		When write tuta

	@new4
	Scenario: core_5
		When write tatu
		
	@new4
	@new5
	Scenario: core_6
		When search
		
	Scenario: error_scenario
		When write_error error 
		
	Scenario: my beautiful scenario ?? ok ??
		When write tatu