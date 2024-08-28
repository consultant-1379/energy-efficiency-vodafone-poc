package usat.page.fragment

import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

class MainLineChart {

    @FindBy(css = ".eaEnergyEfficiency-rMain-lineChart_show")
    WebElement lineChart

    @FindBy(css = ".eaEnergyEfficiency-rMain-lineChart-header")
    WebElement lineChartHeader


    String getLineChartTextHeader() {
        return lineChartHeader.getText()
    }
}
