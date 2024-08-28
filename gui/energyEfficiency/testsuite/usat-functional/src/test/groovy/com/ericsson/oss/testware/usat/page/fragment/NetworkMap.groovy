package src.test.groovy.com.ericsson.oss.testware.usat.page.fragment

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import com.ericsson.oss.testware.usat.page.fragment.PageFragment

class NetworkMap implements PageFragment{
    @FindBy(css = ".eaEnergyEfficiency-wNetworkMap")
    WebElement map

    @FindBy(css = ".elMapping-Map-renderer-overlay-pane")
    WebElement pane

    List<WebElement> getNetworkElementsOnMap() {
        return map.findElements(By.cssSelector(".eaEnergyEfficiency-wNetworkMap-NE-icon"))
    }

    List<WebElement> getLinksOnMap() {
        return pane.findElements(By.cssSelector(".elMapping-Map-renderer-interactive"))
    }
}
