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

import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

import java.util.concurrent.TimeUnit

import static org.jboss.arquillian.graphene.Graphene.waitGui

class DialogBox implements PageFragment {

    @FindBy(css = ".ebDialog-holder")
    WebElement dialogBoxHolder

    @FindBy(css = ".ebDialogBox-primaryText")
    WebElement dialogBoxHeader

    @FindBy(css = ".ebDialogBox-secondaryText")
    WebElement secondaryText

    @FindBy(css = ".ebBtn-caption")
    WebElement closeButton

    @FindBy(css = ".ebDialogBox-actionBlock .ebBtn_color_darkBlue")
    WebElement deactivateButton

    @FindBy(xpath = "//span[@class = 'ebBtn-caption' and text() = 'Cancel']")
    WebElement cancelButton

    String getDialogBoxHeader() {
        return dialogBoxHeader.getText()
    }

    String getSecondaryText() {
        return secondaryText.getText()
    }

    void clickCloseButton() {
        click(closeButton)
    }

    void clickDeactivateButton() {
        click(deactivateButton)
    }

    void clickConfirmButton() {
        click(deactivateButton)
    }

    boolean isDialogBoxHolderDisplayed() {
        waitGui().withTimeout(1, TimeUnit.SECONDS)
        return falseOnException { dialogBoxHeader.isDisplayed() }
    }

    void waitForDialogBox() {
        waitVisible(closeButton)
    }

    void clickCancelButton() {
        click(cancelButton)
    }
}
