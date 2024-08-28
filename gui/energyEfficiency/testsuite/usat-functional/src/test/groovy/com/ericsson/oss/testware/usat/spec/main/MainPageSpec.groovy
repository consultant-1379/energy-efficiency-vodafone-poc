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

package com.ericsson.oss.testware.usat.spec.main

import com.ericsson.oss.testware.usat.page.object.MainPage
import org.jboss.arquillian.graphene.page.Page
import com.ericsson.oss.testware.usat.spec.BaseSpecification
import org.jboss.arquillian.spock.ArquillianSputnik
import org.junit.runner.RunWith
import spock.lang.Stepwise
import src.test.groovy.com.ericsson.oss.testware.usat.page.fragment.LeftPanel
import src.test.groovy.com.ericsson.oss.testware.usat.page.fragment.RightPanel
import src.test.groovy.com.ericsson.oss.testware.usat.page.fragment.NetworkMap

import usat.page.fragment.BondingTab
import usat.page.fragment.LinksTab
import usat.page.fragment.MainLineChart
import usat.page.fragment.NetworkElementsTab

import static com.ericsson.oss.testware.usat.page.constants.TestConstants.DETAILS_PANEL
import static com.ericsson.oss.testware.usat.page.constants.TestConstants.NETWORK_TOPOLOGY_PANEL
import static com.ericsson.oss.testware.usat.page.constants.TestConstants.REFRESH
import static com.ericsson.oss.testware.usat.page.constants.TestConstants.SAVINGS
import static com.ericsson.oss.testware.usat.page.constants.TestConstants.NO_SELECTION
import static com.ericsson.oss.testware.usat.page.constants.TestConstants.NOT_MONITORED
import static com.ericsson.oss.testware.usat.page.constants.TestConstants.NETWORK

@Stepwise
@RunWith(ArquillianSputnik)
class MainPageSpec extends BaseSpecification {

    //@Shared
    @Page
    MainPage mainPage

    @Page
    NetworkMap networkMap

    @Page
    RightPanel rightPanel

    @Page
    LeftPanel leftPanel

    @Page
    LinksTab linksTab

    @Page
    NetworkElementsTab networkElementsTab

    @Page
    BondingTab bondingTab

    @Page
    MainLineChart mainLineChart

    //TODO Se la faccio aprire da qui non funziona!
    def setSpec() {
        //open(mainPage)

    }

    //TODO devo fare qualcosa??!
    def cleanupSpec() {
    }

    def 'It should check the button and the elements present on map'() {
        System.out.println("*** Test 1: It should check the button and the elements present on map")
        when: 'main page is open'
            open(mainPage)
        then:'following buttons are present on main page'
            browser.currentUrl.endsWith("#energyefficiency")

            mainPage.isButtonDisplayed(SAVINGS) == true
            mainPage.isButtonDisplayed(REFRESH) == true

            mainPage.isButtonPanelDisplayed(DETAILS_PANEL) == true
            mainPage.isButtonPanelDisplayed(NETWORK_TOPOLOGY_PANEL) == true
        and: 'checking left panel'
            leftPanel.panel != null
            leftPanel.getLeftPanelTextHeader() == NETWORK_TOPOLOGY_PANEL
            leftPanel.getTreeTextHeader() == NETWORK
        and: 'checking network map'
            networkMap.map != null
            networkMap.getNetworkElementsOnMap().size() == 6
            networkMap.linksOnMap.size() == 7
        and: 'checking tabs'
            networkElementsTab.tab != null
            linksTab.tab != null
            bondingTab.tab != null
            notThrown(NoSuchElementException)

        System.out.println("*** End of Test 1.")
    }

    def 'It should check if details panel works as expected' () {
        System.out.println("*** Test 2: It should check if details panel works as expected")
        when: 'click on detail button'
            mainPage.clickActionBarButtonPanel(DETAILS_PANEL)
        then:
            rightPanel.panel != null
            notThrown(NoSuchElementException)
        and:
            rightPanel.getRightPanelTextHeader() == DETAILS_PANEL
            rightPanel.getRightPanelNoSelectionInfoMessage() == NO_SELECTION

        System.out.println("*** End of Test 2.")
    }

    def 'It should check if saving panel works as expected' () {
        System.out.println("*** Test 3: It should check if saving panel works as expected")
        when: 'click on saving button'
            mainPage.clickActionBarButton(SAVINGS)
        then:
            rightPanel.panel != null
            notThrown(NoSuchElementException)
        and:
            rightPanel.getRightPanelTextHeader() == SAVINGS
            rightPanel.getRightPanelNoSelectionInfoMessage(true) == NO_SELECTION

        System.out.println("*** End of Test 3.")
    }


    //Links Tab tests
    def 'It should check if selecting one link the lineChart is shown' () {
        System.out.println("*** Test 4: It should check if selecting one link the lineChart is shown")
        when: 'select the first row of Links tab'
            linksTab.selectLinksTab()
        and:
            linksTab.selectFirstLink()
        then:
            mainLineChart.lineChart != null
            notThrown(NoSuchElementException)
        and:
            String linksSelectedName = linksTab.getNameOfHighlightedElement()
            String lcHeader = mainLineChart.getLineChartTextHeader()
            lcHeader.contains(linksSelectedName)
        and:
            linksTab.getNumberOfVisibleRowsOnPage()  == 7 //WARNING: allRows == 14

        System.out.println("*** End of Test 4.")
    }

    def 'It should check if selecting one link and clicking on Details button details are shown as expected' () {
        System.out.println("*** Test 5: It should check if selecting one link and clicking on Details button details are shown as expected")
        when: 'select the first row of Links tab'
            linksTab.selectLinksTab()
        //and:
            //linksTab.selectFirstLink()
        and:
            mainPage.clickActionBarButtonPanel(DETAILS_PANEL)
        then:
            rightPanel.getRightPanelTextHeader() == DETAILS_PANEL
            rightPanel.getRightPanelNoSelectionInfoMessage() != NO_SELECTION
        and:
            rightPanel.firstBlock != null
            rightPanel.secondBlock != null

            notThrown(NoSuchElementException)
        and:
            String linksSelectedName = linksTab.getNameOfHighlightedElement()
            String detailsFirstBlockHeader = rightPanel.getDetailsFirstBlockTextHeader()
            detailsFirstBlockHeader.contains(linksSelectedName)

        System.out.println("*** End of Test 5.")
    }

    def 'It should check if selecting one link and clicking on Saving button details are shown as expected' () {
        System.out.println("*** Test 6: It should check if selecting one link and clicking on Saving button details are shown as expected")
        when: 'select the first row of Links tab'
            linksTab.selectLinksTab()
        //and:
        //    linksTab.selectFirstLink()
        and:
            mainPage.clickActionBarButton(SAVINGS)
        then:
            rightPanel.getRightPanelTextHeader() == SAVINGS
            rightPanel.getRightPanelNoSelectionInfoMessage(true) != NO_SELECTION
        and:
            rightPanel.gaugeChart != null
            rightPanel.gaugeChartDaily != null

            notThrown(NoSuchElementException)
        and:
            String linksSelectedName = linksTab.getNameOfHighlightedElement()
            String gaugeChartHeader = rightPanel.getGaugeChartTextHeader()
            gaugeChartHeader.contains(linksSelectedName)

        System.out.println("*** End of Test 6.")
    }

    //Bonding Tab tests
    def 'It should check if selecting one bonding link the lineChart is shown' () {
        System.out.println("*** Test 7: It should check if selecting one bonding link the lineChart is shown")
        when: 'select the first row of Links tab'
            bondingTab.selectBondingTab()
        and:
            bondingTab.selectFirstLink()
        then:
            mainLineChart.lineChart != null
            notThrown(NoSuchElementException)
        and:
            String linksSelectedName = bondingTab.getNameOfHighlightedElement()
            String lcHeader = mainLineChart.getLineChartTextHeader()
            lcHeader.contains(linksSelectedName)
        and:
            bondingTab.getNumberOfVisibleRowsOnPage() == 2

        System.out.println("*** End of Test 7.")
    }

    def 'It should check if selecting one bonding link and clicking on Details button details are shown as expected' () {
        System.out.println("*** Test 8: It should check if selecting one bonding link and clicking on Details button details are shown as expected")
        when: 'select the first row of Links tab'
            bondingTab.selectBondingTab()
        //and:
        //linksTab.selectFirstLink()
        and:
            mainPage.clickActionBarButtonPanel(DETAILS_PANEL)
        then:
            rightPanel.getRightPanelTextHeader() == DETAILS_PANEL
            rightPanel.getRightPanelNoSelectionInfoMessage() != NO_SELECTION
        and:
            rightPanel.firstBlock != null
            rightPanel.secondBlock != null

            notThrown(NoSuchElementException)
        and:
            String linksSelectedName = linksTab.getNameOfHighlightedElement()
            String detailsFirstBlockHeader = rightPanel.getDetailsFirstBlockTextHeader()
            detailsFirstBlockHeader.contains(linksSelectedName)

        System.out.println("*** End of Test 8.")
    }

    def 'It should check if selecting one bonding link and clicking on Saving button details are shown as expected' () {
        System.out.println("*** Test 9: It should check if selecting one bonding link and clicking on Saving button details are shown as expected")
        when: 'select the first row of Links tab'
            bondingTab.selectBondingTab()
        //and:
        //    linksTab.selectFirstLink()
        and:
            mainPage.clickActionBarButton(SAVINGS)
        then:
            rightPanel.getRightPanelTextHeader() == SAVINGS
            rightPanel.getRightPanelNoSelectionInfoMessage(true) != NO_SELECTION
        and:
            rightPanel.gaugeChart != null
            rightPanel.gaugeChartDaily != null

            notThrown(NoSuchElementException)
        and:
            String linksSelectedName = linksTab.getNameOfHighlightedElement()
            String gaugeChartHeader = rightPanel.getGaugeChartTextHeader()
            gaugeChartHeader.contains(linksSelectedName)

        System.out.println("*** End of Test 9.")
    }

    //Network Elements Tab test
    def 'It should check if selecting one node the lineChart is not shown' () {
        System.out.println("*** Test 10: It should check if selecting one node the lineChart is not shown")
        when: 'select the first row of Links tab'
            networkElementsTab.selectNetworkElementsTab()
        and:
            networkElementsTab.selectFirstNode()
        and:
            mainLineChart.getProperty()
        then:
            thrown(NullPointerException)
        and:
            networkElementsTab.getNumberOfVisibleRowsOnPage() == 6

        System.out.println("*** End of Test 10.")
    }

    def 'It should check if details panel is empty' () {
        System.out.println("*** Test 11: It should check if details panel is empty")
        when: 'click on detail button'
        mainPage.clickActionBarButtonPanel(DETAILS_PANEL)
        then:
        rightPanel.panel != null
        notThrown(NoSuchElementException)
        and:
        rightPanel.getRightPanelTextHeader() == DETAILS_PANEL
        rightPanel.getRightPanelNoSelectionInfoMessage() == NOT_MONITORED

        System.out.println("*** End of Test 11.")
    }

    def 'It should check if saving panel is empty' () {
        System.out.println("*** Test 12: It should check if saving panel is empty")
        when: 'click on saving button'
        mainPage.clickActionBarButton(SAVINGS)
        then:
        rightPanel.panel != null
        notThrown(NoSuchElementException)
        and:
        rightPanel.getRightPanelTextHeader() == SAVINGS
        rightPanel.getRightPanelNoSelectionInfoMessage(true) == NOT_MONITORED

        System.out.println("*** End of Test 12.")
    }
}