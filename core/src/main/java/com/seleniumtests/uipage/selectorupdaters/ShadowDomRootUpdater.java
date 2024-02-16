package com.seleniumtests.uipage.selectorupdaters;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.ByC;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

public class ShadowDomRootUpdater implements SelectorUpdater {

    /**
     * Some selectors are not allowed as direct children of a Shadow root (tagName, xPath, name)
     * For xPath, we can't do anything as it's impossible to convert any xPath to its cssSelector equivalent
     * For name and tagName, we provide the equivalent cssSelector
     *
     * According to https://github.com/SeleniumHQ/selenium/issues/10025, By.tagName is not supported when searching an shadow root by tag name
     * @param element
     */
    @Override
    public void update(HtmlElement element) {
        By by = element.getBy();
        HtmlElement parent = element.getParent();

        // direct child of a shadow root cannot be searched with any selector
        if (parent != null && parent.getBy() instanceof ByC.Shadow) {
            by = rewriteUnsupportedSelector(by);
            element.setBy(by);

        // some selectors are not allowed inside by list (https://github.com/SeleniumHQ/selenium/issues/10025)
        } else if (element.getBy() instanceof ByC.Shadow) {
            List<By> newBies = new ArrayList<>();
            for (By shadowBy: ((ByC.Shadow)element.getBy()).getBies()) {
                newBies.add(rewriteUnsupportedSelector(shadowBy));
            }
            ((ByC.Shadow)element.getBy()).setBies(newBies.toArray(new By[]{}));
        }
    }

    private static By rewriteUnsupportedSelector(By by) {
        if (by instanceof By.ByXPath || by instanceof ByC.ByForcedXPath || (by instanceof ByC && !(by instanceof ByC.ByHasCssSelector))) {
            throw new ScenarioException(String.format("%s is not supported as a direct child of a shadow DOM as it uses XPath. Try to add an intermediate selector (e.g: By.tagName) before", by.getClass()));
        } else if (by instanceof By.ByTagName) {
            by = By.cssSelector(by.toString().split(":")[1].trim());
        } else if (by instanceof By.ByName) {
            by = By.cssSelector(String.format("[name=%s]", by.toString().split(":")[1].trim()));
        } else if (by instanceof ByC.ByHasCssSelector) {
            ((ByC.ByHasCssSelector) by).setUseCssSelector(true);
        }
        return by;
    }
}
