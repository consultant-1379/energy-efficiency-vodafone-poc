define([
    'jscore/core',
    'chartlib/charts/Gauge',
    './SavingsView',
    'i18n!energyefficiency/dictionary.json',
    '../../datastore/datastore',
    '../../common'
], function (core, Gauge, View, dictionary, datastore, FC) {
    'use strict';

    return core.Region.extend({

        View: View,

        init: function (options) {
            this.options = options || {};
        },

        onStart: function () {
            if (this._updateSavingChartsEvtId) {
                this.getEventBus().unsubscribe(this._updateSavingChartsEvtId);
            }
            this.destroyDailyGaugeChart();
            this.destroyGaugeChart();
            this.initGaugeParams();

            this.createInstantGaugeChart();
            this.createDailyGaugeChart();

            this.showChart = this.options.showchart;
            this.isRowSelected = this.options.rowSelect;
            this.view.showGaugeCharts(this.showChart);

            if (this.showChart) {
                this.view.hideInfoMessage();
            } else {
                this.showNoSelectionInfo();
            }

            if (this.options.id) {
                this.selectedId = this.options.id;
                this.setGaugeChartHeader(this.options.id);

                if (this.options.savingPerctangeValues) {
                    this.updateSavingCharts(this.options.savingPerctangeValues);
                }
            }

            // When we click on tab of an interested table (link/bonding) and we have selected before a row, we have to read
            // datastore.getWhichShowDetails() in order to understand if we have to show charts or not.
            this._updateHeaderTitleGuagesEvtId = this.getEventBus().subscribe('rowTabSelectForDetails', this.setHeaderTitleAndUpdateGuageChart.bind(this));

            // When we select an interested row (link/bonding) or is time interval to call the rest, so we have to update Savings Charts.
            this._updateSavingChartsEvtId = this.getEventBus().subscribe('charts:update:savings', this.updateSavingCharts.bind(this));


            // this._windowResizeEvtId = core.Window.addEventHandler('resize', this.adaptDimension.bind(this)); //TODO
        },

        setSelectedId: function (id) {
            this.selectedId = id;
        },

        setSelectedTab: function (tab) {
            this.selectedTab = tab;
        },

        onStop: function () {
            this.getEventBus().unsubscribe(this._updateSavingChartsEvtId);
            this.getEventBus().unsubscribe(this._updateHeaderTitleGuagesEvtId);
            // core.Window.removeEventHandler(this._windowResizeEvtId);
        },

        initGaugeParams: function () {
            var areaLineColor = [
                    '#c9df8a',
                    '#77ab59',
                    '#36802d',
                    '#234d20'
                ],
                areasFromValue = [
                    0,
                    20,
                    40,
                    60
                ],
                areasFill = [
                    '#ddd',
                    '#ccc',
                    '#bbb',
                    '#aaa',
                    '#999'
                ];

            this.areas = [
                {fromValue: areasFromValue[0], fill: areasFill[0], lineFill: areaLineColor[0]},
                {fromValue: areasFromValue[1], fill: areasFill[1], lineFill: areaLineColor[1]},
                {fromValue: areasFromValue[2], fill: areasFill[2], lineFill: areaLineColor[2]},
                {fromValue: areasFromValue[3], fill: areasFill[3], lineFill: areaLineColor[3]}
            ];
        },

        setGaugeChartHeader: function (id) {
            this.view.setGaugeChartHeader(dictionary.get("savingInstantChartHeader").replace('$1', "\"" + id + "\""));
            this.view.setDailyGaugeChartHeader(dictionary.get("savingDailyChartHeader").replace('$1', "\"" + id + "\""));
        },

        setShowSavingsChartValue: function (showChart) {
            this.showChart = showChart;
        },

        /**
         * Show/Hide charts/info-messages, change info-messages if none selected or the selected row it's not monitored.
         * @method setHeaderTitleAndUpdateGuageChart
         * @param {type} params
         *
         * params = {
         *     whichTab: whichTab,
         *     showCharts: this.showCharts,
         *     id: this.selectedId,
         *     isRowSelected: this.isRowSelected,
         *     isRightPanelOpen: this.isRightPanelOpen,
         *     actionValueRightPanel: this.actionValueRightPanel
         * }
         */
        setHeaderTitleAndUpdateGuageChart: function (params) {
            this.isRowSelected = params.isRowSelected;
            if (params.whichTab) {
                this.setSelectedTab(params.whichTab);
            }
            if (params.id) {
                this.setSelectedId(params.id);
            }
            this.showChart = params.showCharts;
            if (params.showCharts && params.isRowSelected && this.selectedId &&
                (this.selectedTab && (this.selectedTab === FC.bondingLabel || this.selectedTab === FC.linksLabel || this.selectedTab === FC.lagsLabel)) &&
                params.isRightPanelOpen && params.actionValueRightPanel === FC.savingsLabel) {
                this.showChartGauge();
            } else {
                this.showNoSelectionInfo();
            }
        },

        /**
         * Attach to DOM the info message about no selection Links/Lags an current tab table dane and Details Panel open.
         *
         * @private
         * @method showNoSelectionInfo
         */
        showNoSelectionInfo: function () {
            this.view.showGaugeCharts(false);
            this.view.showInfoMessage();
            var header = this.isRowSelected ? dictionary.get('noValidSelectionHeader') : dictionary.get('noSelectionInfoHeader');
            this.view.setInfoMessageHeader(header);
            this.view.setInfoMessageParagraph(dictionary.get('noSelectionInfoParagraph'));
        },

        showChartGauge: function () {
            this.view.showGaugeCharts(true);
            this.view.hideInfoMessage();
            this.setGaugeChartHeader(this.selectedId);
        },

        destroyGaugeChart: function () {
            if (this.gaugeChart) {
                this.gaugeChart.destroy();
            }
        },

        destroyDailyGaugeChart: function () {
            if (this.dailyGaugeChart) {
                this.dailyGaugeChart.destroy();
            }
        },

        createInstantGaugeChart: function () {
            this.gaugeChart = new Gauge({
                element: this.view.getGaugeChart(),
                chart: {
                    labels: {
                        value: {
                            format: function (v) {
                                return v.toFixed(2) + '%';
                            }
                        },
                        title: dictionary.get("savings"),
                        min: '0%',
                        max: '100%'
                    },
                    min: 0,
                    areas: this.areas
                },
                data: {
                    value: 0
                }
            });
        },

        createDailyGaugeChart: function () {
            this.dailyGaugeChart = new Gauge({
                element: this.view.getDailyGaugeChart(),
                chart: {
                    labels: {
                        value: {
                            format: function (v) {
                                return v.toFixed(2) + '%';
                            }
                        },
                        title: dictionary.get("dailySavings"),
                        min: '0%',
                        max: '100%'
                    },
                    min: 0,
                    areas: this.areas
                },
                data: {
                    value: 0
                }
            });
        },

        updateSavingCharts: function (savingPerctangeValues) {
            if (this.showChart && savingPerctangeValues) {
                this.historicalSavingPeriod = this.getHistoricalSavingPeriod(savingPerctangeValues.historicalSavingPeriod);

                if (this.gaugeChart && this.showChart) {
                    this.gaugeChart.update({
                        value: savingPerctangeValues.currentSavingPercentage ? savingPerctangeValues.currentSavingPercentage : 0
                    });
                }
                if (this.dailyGaugeChart && this.showChart) {
                    this.dailyGaugeChart.update({
                        value: savingPerctangeValues.historicalSavingPercentage ? savingPerctangeValues.historicalSavingPercentage : 0
                    });
                }
            }
        },

        getHistoricalSavingPeriod: function (historicalSavingPeriod) {
            switch (historicalSavingPeriod) {
                case 1:
                    return 24;
                default:
                    return 24;
            }
        }

        //NOT used for now
        // ,
        // adaptDimension: function () {
        //     var size = {
        //         width: core.Window.getProperty('innerWidth'),
        //         height: core.Window.getProperty('innerHeight')
        //     };
        //     this.getElement().setStyle({
        //         height: size.height - 200,
        //         width: size.width - 500
        //     });
        // }

    });

});