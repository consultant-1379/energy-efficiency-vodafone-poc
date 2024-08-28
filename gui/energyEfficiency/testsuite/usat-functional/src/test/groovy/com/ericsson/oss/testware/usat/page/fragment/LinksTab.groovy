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

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

class LinksTab implements Tab {

    @FindBy(xpath = "//div[starts-with(@class, 'ebTabs-tabItem') and contains(text(), 'Links')]")
    WebElement tab

    @FindBy(css = ".elTablelib-Table-body")
    WebElement linksTable

    @FindBy(css = ".elTablelib-Table-body > tr:nth-child(1) > td:nth-child(2)")
    WebElement firstTableRow

    void selectFirstLink() {
        waitPresent(firstTableRow)
        firstTableRow.click()
    }

    void selectLinksTab() {
        waitPresent(tab)
        tab.click()

    }

    String getTab() {
        waitPresent(tab)
        return getTabSelector()
    }

    @Override
    String getTabName() {
        return "Links"
    }
}
