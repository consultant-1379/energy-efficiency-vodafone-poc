package src.test.groovy.com.ericsson.oss.testware.usat.page.fragment

import com.ericsson.oss.testware.usat.page.fragment.PageFragment
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

class LeftPanel implements PageFragment{

    @FindBy(css = ".elLayouts-MultiSlidingPanels-LeftWrapper")
    WebElement panel

    @FindBy(css = ".elLayouts-MultiSlidingPanels-title-left")
    WebElement leftPanelHeader

    @FindBy(xpath = "//div[@class = 'ebTreeItem-label' and text() = 'Network']")
    WebElement treeHeader

    String getLeftPanelTextHeader() {
        return leftPanelHeader.getText()
    }

    String getTreeTextHeader() {
        return treeHeader.getText()
    }
}
