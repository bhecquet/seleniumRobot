package com.seleniumtests.core.runner.cucumber;

import java.io.IOException;

import org.testng.Assert;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.util.helper.WaitHelper;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.java.fr.Alors;
import cucumber.api.java.fr.Lorsque;
import cucumber.api.java.fr.Soit;

public class Actions extends Fixture {

	@Given("^Open page '(.+)'")
	@Soit("^Ouvrir la page '(.+)'")
	public void openPage(String url) throws IOException {
		new PageObject(null, getValue(url));
	}
	
	@Given("^Snapshot")
	@Soit("^Capture d'ecran")
	public void snapshot(String url) throws IOException {
		if (currentPage.get() == null) {
			throw new ScenarioException("Aucune page n'a encore été ouverte");
		} else {
			currentPage.get().capturePageSnapshot();
		}
	}
	
	@When("^Write into '(\\w+?)' with (.*)")
	@Lorsque("^Saisir le champ '(\\w+?)' avec (.*)")
	public void sendKeysToField(String fieldName, String value) {
		currentPage.get().sendKeysToField(getElement(fieldName), getValue(value));
	}
	
	@When("^Write (\\d+) random characters into '(\\w+?)'")
	@Lorsque("^Saisir (\\d+) caractères aléatoires dans le champ '(\\w+?)'")
	public void sendRandomKeysToField(Integer charNumber, String fieldName) {
		currentPage.get().sendRandomKeysToField(charNumber, getElement(fieldName));
	}
	
	@When("^Clear field '(\\w+?)'")
	@Lorsque("^Vider le champ '(\\w+?)'")
	public void clear(String fieldName) {
		currentPage.get().clear(getElement(fieldName));
	}

	@When("^Select in list '(\\w+?)' option (.*)")
    @Lorsque("^Sélectionner dans la liste '(\\w+?)' l'option : (.*)")
    public void selectOption(String fieldName, String value) {
		currentPage.get().selectOption(getElement(fieldName), getValue(value));
    }

	@When("^Click on '(\\w+?)'")
	@Lorsque("^Cliquer sur '(\\w+?)'")
	public void click(String fieldName) {
		currentPage.get().click(getElement(fieldName));
	}

	@When("^Double click on '(\\w+?)'")
	@Lorsque("^Double cliquer sur '(\\w+?)'")
	public void doubleClick(String fieldName) {
		currentPage.get().doubleClick(getElement(fieldName));
	}

	@When("^Wait (\\d+) ms")
	@Lorsque("^Attendre (\\d+) ms")
	public void wait(Integer waitMs) {
		WaitHelper.waitForMilliSeconds(waitMs);
	}

	@When("^Click on cell ([0-9]+)x([0-9]+) of table '(\\w+?)'")
    @Lorsque("^Cliquer sur la cellule ([0-9]+)x([0-9]+) du tableau '(\\w+?)'")
    public void clickTableCell(Integer row, Integer column, String fieldName) {
		currentPage.get().clickTableCell(row, column, getElement(fieldName));
    }
	
	@When("^Accept alert")
	@Lorsque("^Valider l'alerte")
	public void acceptAlert() {
		currentPage.get().acceptAlert();
	}
	
	@When("^Cancel alert")
	@Lorsque("^Annuler l'alerte")
	public void cancelAlert() {
		currentPage.get().cancelAlert();
	}
	
	@When("^Wait field '(\\w+?)' to be present")
	@Lorsque("^Attendre que le champ '(\\w+?)' soit présent")
	public void waitForPresent(String fieldName) {
		currentPage.get().waitForPresent(getElement(fieldName));
	}
	
	@When("^Wait field '(\\w+?)' to be visible")
	@Lorsque("^Attendre que le champ '(\\w+?)' soit visible")
	public void waitForVisible(String fieldName) {
		currentPage.get().waitForVisible(getElement(fieldName));
	}
	
	@When("^Wait field '(\\w+?)' not to be present")
	@Lorsque("^Attendre que le champ '(\\w+?)' ne soit pas présent")
	public void waitForNotPresent(String fieldName) {
		currentPage.get().waitForNotPresent(getElement(fieldName));
	}

	@When("^Wait field '(\\w+?)' to be invisible")
	@Lorsque("^Attendre que le champ '(\\w+?)' soit invisible")
	public void waitForInvisible(String fieldName) {
		currentPage.get().waitForInvisible(getElement(fieldName));
	}
	
	@When("^Wait field value '(\\w+?)' to be (.*)")
	@Lorsque("^Attendre que la valeur du champ '(\\w+?)' soit (.*)")
	public void waitForValue(String fieldName, String value) {
		currentPage.get().waitForValue(getElement(fieldName), getValue(value));
	}
	
	@When("^Wait for cell ([0-9]+)x([0-9]+) of table '(\\w+?)' to be (\\w+)")
    @Lorsque("^Attendre que la cellule ([0-9]+)x([0-9]+) du tableau '(\\w+?)' contienne (\\w+)")
    public void waitTableCellValue(Integer row, Integer column, String fieldName, String value) {
		currentPage.get().waitTableCellValue(row, column, getElement(fieldName), getValue(value));
    }

	@When("^Switch to new window")
	@Lorsque("^Basculer sur la nouvelle fenêtre")
	public void switchToNewWindow() {
    	currentPage.get().switchToNewWindow();
    }

	@When("^Switch to main window")
	@Lorsque("^Basculer sur la fenêtre principale")
    public void switchToMainWindow() {
    	currentPage.get().switchToMainWindow();
    }

	@When("^Switch to window (\\d+)")
	@Lorsque("^Basculer sur la fenêtre (\\d+)")
    public void switchToWindow(int index) {
    	currentPage.get().switchToWindow(index);
    }
	
	@Then("^Assert field '(\\w+?)' to be invisible")
	@Alors("^Vérifier que le champ '(\\w+?)' soit invisible")
	public void assertForInvisible(String fieldName) {
		currentPage.get().assertForInvisible(getElement(fieldName));
	}
	
	@Then("^Assert field '(\\w+?)' to be visible")
	@Alors("^Vérifier que le champ '(\\w+?)' soit visible")
	public void assertForVisible(String fieldName) {
		currentPage.get().assertForVisible(getElement(fieldName));
	}
	
	@Then("^Assert field '(\\w+?)' to be disabled")
	@Alors("^Vérifier que le champ '(\\w+?)' soit inactif")
	public void assertForDisabled(String fieldName) {
		currentPage.get().assertForDisabled(getElement(fieldName));
	}
	
	@Then("^Assert field '(\\w+?)' to be enabled")
	@Alors("^Vérifier que le champ '(\\w+?)' soit actif")
	public void assertForEnabled(String fieldName) {
		currentPage.get().assertForEnabled(getElement(fieldName));
	}

	@Then("^Assert field value '(\\w+?)' to be (.*)")
	@Alors("^Vérifier que la valeur du champ '(\\w+?)' soit (.*)")
	public void assertForValue(String fieldName, String value) {
		currentPage.get().assertForValue(getElement(fieldName), getValue(value));
	}
	
	@Then("^Assert field value '(\\w+?)' to be empty")
	@Alors("^Vérifier que la valeur du champ '(\\w+?)' soit vide")
	public void assertForEmptyValue(String fieldName) {
		currentPage.get().assertForEmptyValue(getElement(fieldName));
	}
	
	@Then("^Assert field value '(\\w+?)' not to be empty")
	@Alors("^Vérifier que la valeur du champ '(\\w+?)' soit non vide")
	public void assertForNonEmptyValue(String fieldName) {
		currentPage.get().assertForNonEmptyValue(getElement(fieldName));
	}
	
	@Then("^Assert field value '(\\w+?)' matches regex (.*)")
	@Alors("^Vérifier que la valeur du champ '(\\w+?)' corresponde à la regex (.*)")
	public void assertForMatchingValue(String fieldName, String regex) {
		currentPage.get().assertForMatchingValue(getElement(fieldName), getValue(regex));
	}

	@Then("^Assert selected option in list '(\\w+?)' is (.*)")
	@Alors("^Vérifier que l'option sélectionnée dans la liste '(\\w+?)' est (.*)")
    public void assertSelectedOption(String fieldName, String value) {
		currentPage.get().assertSelectedOption(getElement(fieldName), getValue(value));
    }
	
	@Then("^Assert checkbox '(\\w+?)' is checked")
	@Alors("^Vérifier que la case '(\\w+?)' est cochée")
	public void assertChecked(String fieldName) {
		currentPage.get().assertChecked(getElement(fieldName));
	}
	
	@Then("^Assert checkbox '(\\w+?)' is not checked")
	@Alors("^Vérifier que la case '(\\w+?)' n'est pas cochée")
	public void assertNotChecked(String fieldName) {
		currentPage.get().assertNotChecked(getElement(fieldName));
	}
	
	@Then("^Assert for cell ([0-9]+)x([0-9]+) of table '(\\w+?)' to be (\\w+)")
	@Alors("^Vérifier que la cellule ([0-9]+)x([0-9]+) du tableau '(\\w+?)' contient (\\w+)")
    public void assertTableCellValue(Integer row, Integer column, String fieldName, String value) {
		currentPage.get().assertTableCellValue(row, column, getElement(fieldName), getValue(value));
    }

	@Then("^Assert text present in page (\\w+)")
	@Alors("^Vérifier la présence du texte (\\w+)")
    public void assertTextPresentInPage(String text) {
    	currentPage.get().assertTextPresentInPage(text);
    }
	
	@Then("^Assert text not present in page (\\w+)")
	@Alors("^Vérifier l'absence du texte (\\w+)")
	public void assertTextNotPresentInPage(String text) {
		currentPage.get().assertTextNotPresentInPage(text);
	}	
	
}
