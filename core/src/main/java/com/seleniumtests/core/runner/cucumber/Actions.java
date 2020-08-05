package com.seleniumtests.core.runner.cucumber;

import java.io.IOException;
import java.util.regex.Pattern;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.CheckBoxElement;
import com.seleniumtests.uipage.htmlelements.Element;
import com.seleniumtests.uipage.htmlelements.GenericPictureElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.Table;
import com.seleniumtests.util.helper.WaitHelper;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.java.fr.Alors;
import cucumber.api.java.fr.Lorsque;
import cucumber.api.java.fr.Soit;
import net.bytebuddy.utility.RandomString;

public class Actions extends Fixture {

	@Given("^Open page '(.+)'")
	@Soit("^Ouvrir la page '(.+)'")
	public void openPage(String url) throws IOException {
		new PageObject(null, getValue(url));
	}
	
	@When("^Write into '(\\w+?)' with (.*)")
	@Lorsque("^Saisir le champ '(\\w+?)' avec (.*)")
	public void sendKeysToField(String fieldName, String value) {
		Element element = getElement(fieldName);
		element.sendKeys(getValue(value));
	}
	
	@When("^Write (\\d+) random characters into '(\\w+?)'")
	@Lorsque("^Saisir (\\d+) caractères aléatoires dans le champ '(\\w+?)'")
	public void sendRandomKeysToField(Integer charNumber, String fieldName) {
		Element element = getElement(fieldName);
		element.sendKeys(RandomString.make(charNumber));
	}
	
	@When("^Clear field '(\\w+?)'")
	@Lorsque("^Vider le champ '(\\w+?)'")
	public void clear(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).clear();
		}
	}

	@When("^Select in list '(\\w+?)' option (.*)")
    @Lorsque("^Sélectionner dans la liste '(\\w+?)' l'option : (.*)")
    public void selectOption(String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			new Select((HtmlElement)element).selectByVisibleText(getValue(value));
		} else {
			throw new ScenarioException(String.format("Element %s is not an HtmlElement subclass", fieldName));
		}
    }

	@When("^Click on '(\\w+?)'")
	@Lorsque("^Cliquer sur '(\\w+?)'")
	public void click(String fieldName) {
		Element element = getElement(fieldName);
		element.click();
	}

	@When("^Double click on '(\\w+?)'")
	@Lorsque("^Double cliquer sur '(\\w+?)'")
	public void doubleClick(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).doubleClickAction();
		} else {
			((GenericPictureElement)element).doubleClick();
		}
	}

	@When("^Wait (\\d+) ms")
	@Lorsque("^Attendre (\\d+) ms")
	public void wait(Integer waitMs) {
		WaitHelper.waitForMilliSeconds(waitMs);
	}

	@When("^Click on cell ([0-9]+)x([0-9]+) of table '(\\w+?)'")
    @Lorsque("^Cliquer sur la cellule ([0-9]+)x([0-9]+) du tableau '(\\w+?)'")
    public void clickTableCell(Integer row, Integer column, String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof Table) {
			((Table)element).getCell(row, column).click();
		} else {
			throw new ScenarioException(String.format("Element %s is not an Table element", fieldName));
		}
    }
	
	@When("^Wait field '(\\w+?)' to be present")
	@Lorsque("^Attendre que le champ '(\\w+?)' soit présent")
	public void waitForPresent(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).waitForPresent();
		} else {
			boolean present = ((GenericPictureElement)element).isElementPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout() * 1000);
			if (!present) {
				throw new TimeoutException(String.format("Element %s is not present", fieldName));
			}
		}
	}
	
	@When("^Wait field '(\\w+?)' to be visible")
	@Lorsque("^Attendre que le champ '(\\w+?)' soit visible")
	public void waitForVisible(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).waitForVisibility();
		} else {
			boolean present = ((GenericPictureElement)element).isElementPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout() * 1000);
			if (!present) {
				throw new TimeoutException(String.format("Element %s is not present", fieldName));
			}
		}
	}
	
	@When("^Wait field '(\\w+?)' not to be present")
	@Lorsque("^Attendre que le champ '(\\w+?)' ne soit pas présent")
	public void waitForNotPresent(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).waitForNotPresent();
		} else {
			boolean present = ((GenericPictureElement)element).isElementPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout() * 1000);
			if (present) {
				throw new TimeoutException(String.format("Element %s is present", fieldName));
			}
		}
	}

	@When("^Wait field '(\\w+?)' to be invisible")
	@Lorsque("^Attendre que le champ '(\\w+?)' soit invisible")
	public void waitForInvisible(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).waitForInvisibility();
		} else {
			boolean present = ((GenericPictureElement)element).isElementPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout() * 1000);
			if (present) {
				throw new TimeoutException(String.format("Element %s is present", fieldName));
			}
		}
	}
	
	@When("^Wait field value '(\\w+?)' to be (.*)")
	@Lorsque("^Attendre que la valeur du champ '(\\w+?)' soit (.*)")
	public void waitForValue(String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).waitFor(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout(), 
					ExpectedConditions.or(
							ExpectedConditions.attributeToBe((HtmlElement)element, "value", getValue(value)),
							ExpectedConditions.textToBePresentInElement((HtmlElement)element, getValue(value))
							));
		} else {
			throw new ScenarioException(String.format("Element %s is not an HtmlElement subclass", fieldName));
		}
	}
	

	@When("^Wait for cell ([0-9]+)x([0-9]+) of table '(\\w+?)' to be (\\w+)")
    @Lorsque("^Attendre que la cellule ([0-9]+)x([0-9]+) du tableau '(\\w+?)' contienne (\\w+)")
    public void waitTableCellValue(Integer row, Integer column, String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof Table) {
			((HtmlElement) element).waitFor(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout(), 
					ExpectedConditions.textToBePresentInElement(((Table)element).getCell(row, column), getValue(value)));
			;
		} else {
			throw new ScenarioException(String.format("Element %s is not an Table element", fieldName));
		}
    }
	

	@Then("^Assert field '(\\w+?)' to be invisible")
	@Alors("^Vérifier que le champ '(\\w+?)' soit invisible")
	public void assertForInvisible(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertFalse(((HtmlElement) element).isDisplayed());
		} else {
			Assert.assertFalse(((GenericPictureElement)element).isElementPresent());
		}
	}
	
	@Then("^Assert field '(\\w+?)' to be visible")
	@Alors("^Vérifier que le champ '(\\w+?)' soit visible")
	public void assertForVisible(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(((HtmlElement) element).isDisplayed());
		} else {
			Assert.assertTrue(((GenericPictureElement)element).isElementPresent());
		}
	}
	
	@Then("^Assert field '(\\w+?)' to be disabled")
	@Alors("^Vérifier que le champ '(\\w+?)' soit inactif")
	public void assertForDisabled(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertFalse(((HtmlElement) element).isEnabled());
		} else {
			throw new ScenarioException(String.format("Element %s is not an HtmlElement subclass", fieldName));
		}
	}
	
	@Then("^Assert field '(\\w+?)' to be enabled")
	@Alors("^Vérifier que le champ '(\\w+?)' soit actif")
	public void assertForEnabled(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(((HtmlElement) element).isEnabled());
		} else {
			throw new ScenarioException(String.format("Element %s is not an HtmlElement subclass", fieldName));
		}
	}

	@Then("^Assert field value '(\\w+?)' to be (.*)")
	@Alors("^Vérifier que la valeur du champ '(\\w+?)' soit (.*)")
	public void assertForValue(String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(((HtmlElement) element).getText().equals(getValue(value)) || ((HtmlElement) element).getValue().equals(getValue(value)));
		} else {
			throw new ScenarioException(String.format("Element %s is not an HtmlElement subclass", fieldName));
		}
	}
	
	@Then("^Assert field value '(\\w+?)' to be empty")
	@Alors("^Vérifier que la valeur du champ '(\\w+?)' soit vide")
	public void assertForEmptyValue(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(((HtmlElement) element).getValue().isEmpty());
		} else {
			throw new ScenarioException(String.format("Element %s is not an HtmlElement subclass", fieldName));
		}
	}
	
	@Then("^Assert field value '(\\w+?)' not to be empty")
	@Alors("^Vérifier que la valeur du champ '(\\w+?)' soit non vide")
	public void assertForNonEmptyValue(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertFalse(((HtmlElement) element).getValue().isEmpty());
		} else {
			throw new ScenarioException(String.format("Element %s is not an HtmlElement subclass", fieldName));
		}
	}
	
	@Then("^Assert field value '(\\w+?)' matches regex (.*)")
	@Alors("^Vérifier que la valeur du champ '(\\w+?)' corresponde à la regex (.*)")
	public void assertForMatchingValue(String fieldName, String regex) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(Pattern.matches(getValue(regex), ((HtmlElement) element).getText()) 
					|| Pattern.matches(getValue(regex), ((HtmlElement) element).getValue()));
		} else {
			throw new ScenarioException(String.format("Element %s is not an HtmlElement subclass", fieldName));
		}
	}

	@Then("^Assert selected option in list '(\\w+?)' is (.*)")
	@Alors("^Vérifier que l'option sélectionnée dans la liste '(\\w+?)' est (.*)")
    public void assertSelectedOption(String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertEquals(new Select((HtmlElement)element).getFirstSelectedOption().getText(), getValue(value));
		} else {
			throw new ScenarioException(String.format("Element %s is not an HtmlElement subclass", fieldName));
		}
    }
	
	@Then("^Assert checkbox '(\\w+?)' is checked")
	@Alors("^Vérifier que la case '(\\w+?)' est cochée")
	public void assertChecked(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof CheckBoxElement) {
			Assert.assertTrue(((CheckBoxElement)element).isSelected());
		} else {
			throw new ScenarioException(String.format("Element %s is not an CheckBoxElement", fieldName));
		}
	}
	
	@Then("^Assert checkbox '(\\w+?)' is not checked")
	@Alors("^Vérifier que la case '(\\w+?)' n'est pas cochée")
	public void assertNotChecked(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof CheckBoxElement) {
			Assert.assertFalse(((CheckBoxElement)element).isSelected());
		} else {
			throw new ScenarioException(String.format("Element %s is not an CheckBoxElement", fieldName));
		}
	}
	
	@Then("^Assert for cell ([0-9]+)x([0-9]+) of table '(\\w+?)' to be (\\w+)")
	@Alors("^Vérifier que la cellule ([0-9]+)x([0-9]+) du tableau '(\\w+?)' contient (\\w+)")
    public void assertTableCellValue(Integer row, Integer column, String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof Table) {
			Assert.assertEquals(((Table)element).getCell(row, column).getText(), getValue(value));
		} else {
			throw new ScenarioException(String.format("Element %s is not an Table element", fieldName));
		}
    }
	
	
}
