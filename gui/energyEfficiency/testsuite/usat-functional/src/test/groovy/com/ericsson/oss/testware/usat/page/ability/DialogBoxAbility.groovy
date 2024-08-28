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

package com.ericsson.oss.testware.usat.page.ability

import org.openqa.selenium.support.FindBy

import com.ericsson.oss.testware.usat.page.fragment.DialogBox

trait DialogBoxAbility {

    @FindBy(css = ".ebDialogBox")
    DialogBox dialogBox

    String getDialogBoxText() {
        return dialogBox.secondaryText
    }

    String getDialogBoxHeader() {
        return dialogBox.dialogBoxHeader
    }

    void clickConfirmDeactivateSubscription() {
        dialogBox.clickDeactivateButton()
    }

    boolean isDialogBoxDisplayed() {
        return dialogBox.dialogBoxHolderDisplayed
    }

    void clickContinueActivatingButton() {
        dialogBox.clickConfirmButton()
    }

    void waitForDialogBox() {
        dialogBox.waitForDialogBox()
    }

    void clickDialogBoxEditSubscriptionButton() {
        dialogBox.clickEditSubscriptionButton()
    }

    void clickDialogBoxCancelButton() {
        dialogBox.clickCancelButton()
    }

    String getItemDescription() {
        return dialogBox.secondaryText
    }

    void closeDialog() {
        dialogBox.clickCloseButton()
    }

    void confirmDialogBox() {
        dialogBox.clickConfirmButton()
    }

}
