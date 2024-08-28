/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package usat.page.fragment

import com.ericsson.oss.testware.usat.page.ability.ClickAbility
import com.ericsson.oss.testware.usat.page.fragment.PageFragment
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

trait Tab implements ClickAbility, PageFragment {

    @FindBy(css = ".ebTabs-tabItem_selected_true")
    WebElement activeTab

    @FindBy(css = ".ebTableRow_highlighted")
    WebElement highlightedElement

    //@FindBy(css = ".elTablelib-Table-body")
    //@FindBy(className = "elTablelib-Table-body")
    @FindBy(xpath = "//tbody")
    WebElement table

    abstract String getTabName()

    boolean isActive() {
        return getActiveTabName() == getTabName()
    }

//    @Override
    void click() {
        System.out.println("root = " + root)
//        click(root.findElement(By.xpath(getTabSelector())))
    }

    String getTabSelector() {
        return "//div[starts-with(@class, 'ebTabs-tabItem') and contains(text(), '${getTabName()}')]"
    }

    String getActiveTabName() {
        return waitPresent(activeTab).getText()
    }

    String getNameOfHighlightedElement() {
        waitPresent(highlightedElement)
        return highlightedElement.findElement(By.xpath("//tr/td")).getText()
    }

    int getNumberOfVisibleRowsOnPage() {
        waitPresent(table)
        waitVisible(table)

        System.out.println("table = \n" + table.getText())

        List<WebElement> tableRows = table.findElements(By.xpath("//tbody/tr"))
        //List<WebElement> tableRows = table.findElements(By.className("ebTableRow"))
        return tableRows.size()
    }
}
