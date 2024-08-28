package com.ericsson.oss.testware.usat.page.fragment

import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy

class BreadCrumbs implements PageFragment {

    @FindBy(xpath = "//a[@class='ebBreadcrumbs-link' and text() = 'Energy Efficiency']")
    WebElement eeLink

    void clickEeLink(){
        click(eeLink)
    }

}