// Generated by Selenium IDE
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
public class MainPageTest {
  private WebDriver driver;
  private Map<String, Object> vars;
  JavascriptExecutor js;
  @Before
  public void setUp() {
    driver = new FirefoxDriver();
    js = (JavascriptExecutor) driver;
    vars = new HashMap<String, Object>();
  }
  @After
  public void tearDown() {
    driver.quit();
  }
  @Test
  public void mainPage() {
    driver.get("https://docs.python.org/3/library/operator.html");
    driver.manage().window().setSize(new Dimension(1150, 825));
    vars.put("user", "myUser");
    driver.findElement(By.linkText("Lib/operator.py")).click();
    vars.put("dateFin", driver.findElement(By.xpath("//td[8]/div/lds-datepicker/div/input")).getAttribute("value"));
    vars.put("dateAujourdhui", js.executeScript("return new Date().toLocaleDateString(\'fr-FR\');"));
    assertEquals(vars.get("dateAujourdhui").toString(), "vars.get("dateFin").toString()");
    assertThat(value, is("vars.get("dateDemain").toString()"));
    assertThat(driver.findElement(By.xpath("//div/input")).getText(), is("vars.get(\"stringVide\").toString()"));
  }
}
