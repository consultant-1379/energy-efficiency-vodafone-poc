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

import com.ericsson.oss.testware.usat.page.ability.WaitAbility
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

class MainTable implements WaitAbility, PageFragment {

    @FindBy(css = ".elTablelib-Table-body")
    WebElement tableBody

    @FindBy(css = ".ebTableRow.ebTableRow_highlighted")
    WebElement highlightedRow

    @FindBy(css = ".elTablelib-Table-body > tr:nth-child(1) > td:nth-child(2)")
    WebElement firstTableRow

    @FindBy(css = ".elTablelib-Table-body > tr:nth-child(2) > td:nth-child(1)")
    WebElement secondTableRow

    @Override
    void waitForLoad() {
        waitVisible(tableBody)
    }

    void clickFirstRow() {
        click(secondTableRow)
    }

    int getNumberOfRowsOnPage() {
        List<WebElement> tableRows = tableBody.findElements(By.cssSelector(".ebTableRow"))
        return tableRows.size()
    }

    List<String> getColumnData(final int columnIndex) {
        List<String> columnText = new ArrayList<String>()
        List<WebElement> columnData = tableBody.findElements(By.cssSelector("tr>td:nth-child(" + columnIndex + ")"))
        for (WebElement i : columnData) {
            columnText.add(i.getText())
        }
        return columnText
    }

    String getCellData(final int columnIndex, final int rowIndex) {
        List<String> columnData = getColumnData(columnIndex)
        return columnData.get(rowIndex)
    }
}

