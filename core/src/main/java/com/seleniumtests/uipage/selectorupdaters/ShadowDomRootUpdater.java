package com.seleniumtests.uipage.selectorupdaters;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import org.openqa.selenium.By;

public class ShadowDomRootUpdater implements SelectorUpdater {

    /**
     * Some selectors are not allowed as direct children of a Shadow root (tagName, xPath, name)
     * For xPath, we can't do anything as it's impossible to convert any xPath to its cssSelector equivalent
     * For name and tagName, we provide the equivalent cssSelector
     * @param element
     */
    @Override
    public void update(HtmlElement element) {
        By by = element.getBy();
        HtmlElement parent = element.getParent();

        if (parent != null && parent.getBy() instanceof ByC.Shadow) {
            if (by instanceof By.ByXPath || by instanceof ByC.ByForcedXPath || (by instanceof ByC && !(by instanceof ByC.ByHasCssSelector))) {
                throw new ScenarioException(String.format("%s is not supported as a direct child of a shadow DOM as it uses XPath. Try to add an intermediate selector (e.g: By.tagName) before", by.getClass()));
            } else if (by instanceof By.ByTagName) {
                by = By.cssSelector(by.toString().split(":")[1].trim());
            } else if (by instanceof By.ByName) {
                by = By.cssSelector(String.format("[name=%s]", by.toString().split(":")[1].trim()));
            } else if (by instanceof ByC.ByHasCssSelector) {
                ((ByC.ByHasCssSelector)by).setUseCssSelector(true);
            }
            element.setBy(by);
        }
    }
}
