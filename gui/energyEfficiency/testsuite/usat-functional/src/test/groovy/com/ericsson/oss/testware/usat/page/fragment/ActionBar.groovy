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

package com.ericsson.oss.testware.usat.page.fragment

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement

class ActionBar implements PageFragment {

//    static String DUPLICATE_SUBSCRIPTION = 'Duplicate'
//    static String EDIT_SUBSCRIPTION = 'Edit Subscription'
//    static String REMOVE = 'Remove'
//
//    @FindBy(css = ".ebDropdown")
//    WebElement createSubscriptionDropdown
//
//    @FindBy(xpath = "//a[@class='elLayouts-ActionBarItem' and text() = 'View Subscription Logs']")
//    WebElement viewSubscriptionLogsLink
//
//    @FindBy(xpath = "//a[@class='elLayouts-ActionBarItem' and text() = 'View PM Node Processes for Subscription']")
//    WebElement viewPmNodeProcessesForSubscriptionLink
//
//    @FindBy(xpath = "//a[@class='elLayouts-ActionBarItem' and text() = 'View PM Node Processes for Node']")
//    WebElement viewPmNodeProcessesForNodeLink
//
//    @FindBy(css = ".ebBtn.elLayouts-ActionBarButton")
//    WebElement cancelButton
//
//    @FindBy(css = ".ebIcon.ebIcon_info.elLayouts-ActionBarButton-icon")
//    WebElement viewDescription
//
//    @FindBy(xpath = "//button[@class= 'ebBtn  elLayouts-ActionBarButton' and text() = 'Save and Activate']")
//    WebElement saveAndActivateButton
//
//    @FindBy(xpath = "//button[@class= 'ebBtn  elLayouts-ActionBarButton' and text() = 'View Subscription']")
//    WebElement viewSubscriptionButton
//
//    void clickViewDescription() {
//        click(viewDescription)
//    }
//
//    void clickViewSubscription() {
//        click(viewSubscriptionButton)
//    }
//
//    void clickViewPmNodeProcessesForSubscriptionLink() {
//        click(viewPmNodeProcessesForSubscriptionLink)
//    }
//
//    void clickViewPmNodeProcessesForNodeLink() {
//        waitVisible(viewPmNodeProcessesForNodeLink)
//        click(viewPmNodeProcessesForNodeLink)
//    }
//
//    boolean isViewSubscriptionLogsLinkVisible() {
//        return falseOnException { viewSubscriptionLogsLink.isDisplayed() }
//    }
//
//    boolean isViewPmNodeProcessesLinkVisible() {
//        return falseOnException { viewPmNodeProcessesForSubscriptionLink.isDisplayed() }
//    }
//
    boolean isButtonDisplayed(final String buttonText) {
        final String buttonSelector = getActionBarButtonSelector(buttonText)
        try {
            WebElement actionBarButton = root.findElement(By.xpath(buttonSelector))
            return actionBarButton.isDisplayed()
        } catch (NoSuchElementException e) {
            return false
        }
    }

    boolean isButtonPanelDisplayed(final String buttonText) {
        final String buttonSelector = getPanelActionBarButtonSelector(buttonText)
        try {
            WebElement actionBarButton = root.findElement(By.xpath(buttonSelector))
            return actionBarButton.isDisplayed()
        } catch (NoSuchElementException e) {
            return false
        }
    }

    void clickActionBarButton(final String buttonText) {
        waitVisible(root)
        if (buttonText.equalsIgnoreCase("Cancel")) {
            cancelButton.click()
        } else {
            final String buttonSelector = getActionBarButtonSelector(buttonText)
            root.findElement(By.xpath(buttonSelector)).click()
        }
    }

    void clickActionBarButtonPanel(final String buttonText) {
        waitVisible(root)
        if (buttonText.equalsIgnoreCase("Cancel")) {
            cancelButton.click()
        } else {
            final String buttonSelector = getPanelActionBarButtonSelector(buttonText)
            root.findElement(By.xpath(buttonSelector)).click()
        }
    }

    private String getActionBarButtonSelector(final String buttonText) {
        return "//span[@class='elLayouts-ActionBarButton-text' and text()='" + buttonText + "']"
    }

    private String getPanelActionBarButtonSelector(final String buttonText) {
        return "//span[@class='elLayouts-PanelActionBar-button-label' and text()='" + buttonText + "']"
    }


}
