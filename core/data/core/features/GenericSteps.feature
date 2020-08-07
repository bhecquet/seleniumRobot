Feature: generic fixtures

  Scenario: scenario1
    Given Open page '{{ url }}'
		When Write into 'textElement' with hello
		
  Scenario: scenario2
    Given Open page '{{ url }}'
		When Write into 'DriverTestPage.textElement' with hello
		When Click on 'link'
		And Switch to new window
		And Write password into 'DriverSubTestPage.textElement' with Byebye

