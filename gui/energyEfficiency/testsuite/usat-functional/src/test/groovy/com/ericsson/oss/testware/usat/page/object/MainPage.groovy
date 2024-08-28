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

package com.ericsson.oss.testware.usat.page.object

import com.ericsson.oss.testware.usat.page.fragment.ActionBar
import com.ericsson.oss.testware.usat.page.fragment.BreadCrumbs
import com.ericsson.oss.testware.usat.page.fragment.DialogBox
import com.ericsson.oss.testware.usat.page.fragment.MainTable
import org.jboss.arquillian.graphene.page.Location
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

@Location("/#energyefficiency")
class MainPage implements PageObject {

    @FindBy(css = ".elLayouts-QuickActionBarWrapper")
    ActionBar actionBar

    @FindBy(css = ".elTablelib-Table")
    MainTable table

    @FindBy(css = ".ebBreadcrumbs")
    BreadCrumbs breadCrumbs

    @FindBy(css = ".ebDialogBox")
    DialogBox dialogBox

    @FindBy(css = ".elLayouts-TopSection-title")
    WebElement pageTitle

    @FindBy(xpath = "//div[contains(@class, 'ebLoader-Holder')]")
    WebElement loadingDots

    @Override
    void waitForLoad() {
        table.waitForLoad()
    }

    String getDialogBoxHeader() {
        return dialogBox.getDialogBoxHeader()
    }

    String getItemDescription() {
        return dialogBox.getSecondaryText()
    }

    void closeDialog() {
        dialogBox.clickCloseButton()
    }

    void waitForDialogBox() {
        dialogBox.waitForDialogBox()
    }

    boolean isDialogBoxDisplayed() {
        return dialogBox.isDialogBoxHolderDisplayed()
    }

    void confirmDialogBox() {
        dialogBox.clickConfirmButton()
    }

    void clickEeBreadCrumb() {
        breadCrumbs.clickEeLink()
    }

    String getPageTitle() {
        return pageTitle.text
    }

    private void refreshPage(final WebDriver browser) {
        browser.navigate().refresh()
        waitForLoad()
    }
}