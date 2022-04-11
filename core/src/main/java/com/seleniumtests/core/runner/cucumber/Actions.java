package com.seleniumtests.core.runner.cucumber;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.util.helper.WaitHelper;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Lorsque;
import io.cucumber.java.fr.Soit;

public class Actions extends Fixture {

	@Given("^Open page '(.+)'$")
	@Soit("^Ouvrir la page '(.+)'$")
	public void openPage(String url) {
		new PageObject(null, getValue(url));
	}
	
	@Given("^Snapshot$")
	@Soit("^Capture d'ecran$")
	public void snapshot(String url) {
		if (currentPage.get() == null) {
			throw new ScenarioException("Aucune page n'a encore été ouverte");
		} else {
			currentPage.get().capturePageSnapshot();
		}
	}
	
	@When("^Write into '([\\w.]+?)' with (.*)$")
	@Lorsque("^Saisir le champ '([\\w.]+?)' avec (.*)$")
	public void sendKeysToField(String fieldName, String value) {
		String element = getElement(fieldName);
		currentPage.get().sendKeysToField(element, getValue(value));
	}
	
	@When("^Write password into '([\\w.]+?)' with (.*)$")
	@Lorsque("^Saisir le mot de passe dans le champ '([\\w.]+?)' avec (.*)$")
	public void sendPasswordToField(String fieldName, String password) {
		String element = getElement(fieldName);
		currentPage.get().sendKeysToField(element, getValue(password));
	}
	
	@When("^Write (\\d+) random characters into '([\\w.]+?)'$")
	@Lorsque("^Saisir (\\d+) caractères aléatoires dans le champ '([\\w.]+?)'$")
	public void sendRandomKeysToField(Integer charNumber, String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().sendRandomKeysToField(charNumber, element);
	}
	
	@When("^Clear field '([\\w.]+?)'$")
	@Lorsque("^Vider le champ '([\\w.]+?)'$")
	public void clear(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().clear(element);
	}

	@When("^Select in list '([\\w.]+?)' option (.*)$")
    @Lorsque("^Sélectionner dans la liste '([\\w.]+?)' l'option : (.*)$")
    public void selectOption(String fieldName, String value) {
		String element = getElement(fieldName);
		currentPage.get().selectOption(element, getValue(value));
    }

	@When("^Click on '([\\w.]+?)'$")
	@Lorsque("^Cliquer sur '([\\w.]+?)'$")
	public void click(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().click(element);
	}

	@When("^Double click on '([\\w.]+?)'$")
	@Lorsque("^Double cliquer sur '([\\w.]+?)'$")
	public void doubleClick(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().doubleClick(element);
	}

	@When("^Wait (\\d+) ms$")
	@Lorsque("^Attendre (\\d+) ms$")
	public void wait(Integer waitMs) {
		WaitHelper.waitForMilliSeconds(waitMs);
	}

	@When("^Click on cell ([0-9]+)x([0-9]+) of table '([\\w.]+?)'$")
    @Lorsque("^Cliquer sur la cellule ([0-9]+)x([0-9]+) du tableau '([\\w.]+?)'$")
    public void clickTableCell(Integer row, Integer column, String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().clickTableCell(row, column, element);
    }
	
	@When("^Accept alert$")
	@Lorsque("^Valider l'alerte$")
	public void acceptAlert() {
		currentPage.get().acceptAlert();
	}
	
	@When("^Cancel alert$")
	@Lorsque("^Annuler l'alerte$")
	public void cancelAlert() {
		currentPage.get().cancelAlert();
	}
	
	@When("^Wait field '([\\w.]+?)' to be present$")
	@Lorsque("^Attendre que le champ '([\\w.]+?)' soit présent$")
	public void waitForPresent(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().waitForPresent(element);
	}
	
	@When("^Wait field '([\\w.]+?)' to be visible$")
	@Lorsque("^Attendre que le champ '([\\w.]+?)' soit visible$")
	public void waitForVisible(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().waitForVisible(element);
	}
	
	@When("^Wait field '([\\w.]+?)' not to be present$")
	@Lorsque("^Attendre que le champ '([\\w.]+?)' ne soit pas présent$")
	public void waitForNotPresent(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().waitForNotPresent(element);
	}

	@When("^Wait field '([\\w.]+?)' to be invisible$")
	@Lorsque("^Attendre que le champ '([\\w.]+?)' soit invisible$")
	public void waitForInvisible(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().waitForInvisible(element);
	}
	
	@When("^Wait field value '([\\w.]+?)' to be (.*)$")
	@Lorsque("^Attendre que la valeur du champ '([\\w.]+?)' soit (.*)$")
	public void waitForValue(String fieldName, String value) {
		String element = getElement(fieldName);
		currentPage.get().waitForValue(element, getValue(value));
	}
	
	@When("^Wait for cell ([0-9]+)x([0-9]+) of table '([\\w.]+?)' to be (\\w+)$")
    @Lorsque("^Attendre que la cellule ([0-9]+)x([0-9]+) du tableau '([\\w.]+?)' contienne (\\w+)$")
    public void waitTableCellValue(Integer row, Integer column, String fieldName, String value) {
		String element = getElement(fieldName);
		currentPage.get().waitTableCellValue(row, column, element, getValue(value));
    }

	@When("^Switch to new window$")
	@Lorsque("^Basculer sur la nouvelle fenêtre$")
	public void switchToNewWindow() {
    	currentPage.get().switchToNewWindow();
    }

	@When("^Switch to main window$")
	@Lorsque("^Basculer sur la fenêtre principale$")
    public void switchToMainWindow() {
    	currentPage.get().switchToMainWindow();
    }

	@When("^Switch to window (\\d+)$")
	@Lorsque("^Basculer sur la fenêtre (\\d+)$")
    public void switchToWindow(int index) {
    	currentPage.get().switchToWindow(index);
    }
	
	@Then("^Assert field '([\\w.]+?)' to be invisible$")
	@Alors("^Vérifier que le champ '([\\w.]+?)' soit invisible$")
	public void assertForInvisible(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().assertForInvisible(element);
	}
	
	@Then("^Assert field '([\\w.]+?)' to be visible$")
	@Alors("^Vérifier que le champ '([\\w.]+?)' soit visible$")
	public void assertForVisible(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().assertForVisible(element);
	}
	
	@Then("^Assert field '([\\w.]+?)' to be disabled$")
	@Alors("^Vérifier que le champ '([\\w.]+?)' soit inactif$")
	public void assertForDisabled(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().assertForDisabled(element);
	}
	
	@Then("^Assert field '([\\w.]+?)' to be enabled$")
	@Alors("^Vérifier que le champ '([\\w.]+?)' soit actif$")
	public void assertForEnabled(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().assertForEnabled(element);
	}

	@Then("^Assert field value '([\\w.]+?)' to be (.*)$")
	@Alors("^Vérifier que la valeur du champ '([\\w.]+?)' soit (.*)$")
	public void assertForValue(String fieldName, String value) {
		String element = getElement(fieldName);
		currentPage.get().assertForValue(element, getValue(value));
	}
	
	@Then("^Assert field value '([\\w.]+?)' to be empty$")
	@Alors("^Vérifier que la valeur du champ '([\\w.]+?)' soit vide$")
	public void assertForEmptyValue(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().assertForEmptyValue(element);
	}
	
	@Then("^Assert field value '([\\w.]+?)' not to be empty$")
	@Alors("^Vérifier que la valeur du champ '([\\w.]+?)' soit non vide$")
	public void assertForNonEmptyValue(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().assertForNonEmptyValue(element);
	}
	
	@Then("^Assert field value '([\\w.]+?)' matches regex (.*)$")
	@Alors("^Vérifier que la valeur du champ '([\\w.]+?)' corresponde à la regex (.*)$")
	public void assertForMatchingValue(String fieldName, String regex) {
		String element = getElement(fieldName);
		currentPage.get().assertForMatchingValue(element, getValue(regex));
	}

	@Then("^Assert selected option in list '([\\w.]+?)' is (.*)$")
	@Alors("^Vérifier que l'option sélectionnée dans la liste '([\\w.]+?)' est (.*)$")
    public void assertSelectedOption(String fieldName, String value) {
		String element = getElement(fieldName);
		currentPage.get().assertSelectedOption(element, getValue(value));
    }
	
	@Then("^Assert checkbox '([\\w.]+?)' is checked$")
	@Alors("^Vérifier que la case '([\\w.]+?)' est cochée$")
	public void assertChecked(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().assertChecked(element);
	}
	
	@Then("^Assert checkbox '([\\w.]+?)' is not checked$")
	@Alors("^Vérifier que la case '([\\w.]+?)' n'est pas cochée$")
	public void assertNotChecked(String fieldName) {
		String element = getElement(fieldName);
		currentPage.get().assertNotChecked(element);
	}
	
	@Then("^Assert for cell ([0-9]+)x([0-9]+) of table '([\\w.]+?)' to be (\\w+)$")
	@Alors("^Vérifier que la cellule ([0-9]+)x([0-9]+) du tableau '([\\w.]+?)' contient (\\w+)$")
    public void assertTableCellValue(Integer row, Integer column, String fieldName, String value) {
		String element = getElement(fieldName);
		currentPage.get().assertTableCellValue(row, column, element, getValue(value));
    }

	@Then("^Assert text present in page (\\w+)$")
	@Alors("^Vérifier la présence du texte (\\w+)$")
    public void assertTextPresentInPage(String text) {
    	currentPage.get().assertTextPresentInPage(text);
    }
	
	@Then("^Assert text not present in page (\\w+)$")
	@Alors("^Vérifier l'absence du texte (\\w+)$")
	public void assertTextNotPresentInPage(String text) {
		currentPage.get().assertTextNotPresentInPage(text);
	}	
	
}
