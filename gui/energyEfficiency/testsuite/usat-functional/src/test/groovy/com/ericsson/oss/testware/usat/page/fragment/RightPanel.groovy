package src.test.groovy.com.ericsson.oss.testware.usat.page.fragment

import com.ericsson.oss.testware.usat.page.fragment.PageFragment
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

class RightPanel implements PageFragment{

    @FindBy(css = ".elLayouts-MultiSlidingPanels-rightWrapper")
    WebElement panel

    @FindBy(css = ".elLayouts-MultiSlidingPanels-title-right")
    WebElement rightPanelHeader

    //Details Panel
    @FindBy(css = ".eaEnergyEfficiency-Details-infoMessageHeader")
    WebElement rightPanelInfoMessage

    @FindBy(css = ".eaEnergyEfficiency-Details-Content-block-first")
    WebElement firstBlock

    @FindBy(css = ".eaEnergyEfficiency-Details-Content-block-first-header")
    WebElement firstBlockHeader

    @FindBy(css = ".eaEnergyEfficiency-Details-Content-block-second")
    WebElement secondBlock

    //Saving Panel
    @FindBy(css = ".eaEnergyEfficiency-rSavings-infoMessageHeader")
    WebElement rightPanelInfoMessageSaving

    @FindBy(css = ".eaEnergyEfficiency-rSavings-gauge-chart-content")
    WebElement gaugeChart

    @FindBy(css = ".eaEnergyEfficiency-rSavings-gauge-chart-header")
    WebElement gaugeChartHeader

    @FindBy(css = ".eaEnergyEfficiency-rSavings-gauge-chart-header-daily")
    WebElement gaugeChartDaily

    String getRightPanelTextHeader() {
        return rightPanelHeader.getText()
    }

    String getRightPanelNoSelectionInfoMessage(Boolean isSaving = false) {
        if(isSaving) {
            return rightPanelInfoMessageSaving.getText()
        }
        return rightPanelInfoMessage.getText()
    }

    //Details Panel
    String getDetailsFirstBlockTextHeader() {
        return firstBlockHeader.getText()
    }

    //Saving Panel
    String getGaugeChartTextHeader() {
        return gaugeChartHeader.getText()
    }

}
