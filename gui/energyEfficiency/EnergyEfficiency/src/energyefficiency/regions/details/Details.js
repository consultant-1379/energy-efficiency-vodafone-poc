define([
    'jscore/core',
    'jscore/ext/net',
    './DetailsView',
    'i18n!energyefficiency/dictionary.json',
    'chartlib/charts/Column',
    'container/api',
    '../../widgets/radialChart/RadialChart',
    '../../datastore/datastore',
    '../../common'
], function (core, net, View, dictionary, Column, container, RadialChart, datastore, FC) {

    return core.Region.extend({

        View: View,

        init: function (options) {
            this.options = options || {};
            this.showCharts = false;
        },

        onStart: function () {
            this.createHistogramChart();
            this.createRadialChart();

            // When we click on tab of an interested table (link/bonding) and we have selected before a row, we have to read
            // datastore.getWhichShowDetails() in order to understand if we have to show charts or not.
            this._setHeaderTitleChartsEvent = this.getEventBus().subscribe('rowTabSelectForDetails', this.setHeaderTitleAndUpdateCharts.bind(this));
        },

        /**
         * Show/Hide charts/info-messages, change info-messages if none selected or the selected row it's not monitored.
         * @method setHeaderTitleAndUpdateCharts
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
        setHeaderTitleAndUpdateCharts: function (params, allDataForDetailsCharts) {
            this.params = params;
            var showDetailsGraph = false;
            this.showCharts = params.showCharts;
            if (params.whichTab) {
                this.setSelectedTab(params.whichTab);
            }
            if (params.id) {
                this.setSelectedId(params.id);
            }
            if (params.isRowSelected && this.selectedId && (this.selectedTab && (this.selectedTab === FC.bondingLabel ||
                this.selectedTab === FC.linksLabel || this.selectedTab === FC.lagsLabel))) {
                showDetailsGraph = true;
            }
            if (showDetailsGraph && this.showCharts && params.isRightPanelOpen && params.actionValueRightPanel === FC.detailsLabel) {
                this.showGraphs();
            } else {
                this.showNoSelectionInfo(params.isRowSelected);
            }
        },

        setShowDetailsChartsValue: function (showCharts) {
            this.showCharts = showCharts;
        },

        setSelectedId: function (id) {
            this.selectedId = id;
        },

        setSelectedTab: function (tab) {
            this.selectedTab = tab;
        },

        showGraphs: function () {
            var totalTimeSliceRadar = "24h"; //TODO - maybe to be changed in the future into a dynamic one.
            this.view.hideInfoMessage();
            this.showCharts = true;
            this.view.showCharts(true);
            this.view.setColumnChartHeader(dictionary.get("histogramChartHeader").replace('$1', "\"" + this.selectedId + "\""));
            this.view.setRadialChartHeader(dictionary.get("radarChartHeader").replace('$1', "\"" + this.selectedId + "\"")
                .replace('$2', totalTimeSliceRadar));
        },

        //// Not used for now (when tooltips are too long and you can't see them, you move them on the right)
        // adjustTooltips: function () {
        //     var tooltips = document.getElementsByClassName('tooltip');
        //     for (var i = 0; i < tooltips.length; i++) {
        //         tooltips[i].style.marginLeft = "6rem";
        //     }
        // },

        //// Not used for now - Context Menu (Right click on Mouse) on Histogram Chart.
        // showContextMenu: function (entry, e) {
        //     e.preventDefault();
        //     container.getEventBus().publish('contextmenu:show', e, [{
        //         header: entry.label + ' - ' + entry.value,
        //         items: [{
        //             name: 'Corrective Action',
        //             action: function () {
        //                 container.getEventBus().publish('Details:notify-operation', entry.label);
        //             }.bind(this)
        //         }]
        //     }]);
        // },

        onStop: function () {
            this.destroyColumnChart();
            this.destroyRadialChart();
            this.unSubscribeEvents();
        },

        unSubscribeEvents: function () {
            this.getEventBus().unsubscribe(this._setHeaderTitleChartsEvent);
        },

        /**
         * Attach to DOM the info message about no selection Links/Bonding an current tab table dane and Details Panel open.
         * @private
         * @method showNoSelectionInfo
         */
        showNoSelectionInfo: function (isRowSelected) {
            this.view.showCharts(false);
            this.view.showInfoMessage();
            var header = isRowSelected ? dictionary.get('noValidSelectionHeader') : dictionary.get('noSelectionInfoHeader');
            this.view.setInfoMessageHeader(header);
            this.view.setInfoMessageParagraph(dictionary.get('noSelectionInfoParagraph'));
            if (!isRowSelected) {
                this.selectedId = undefined;
            }
        },

        destroyColumnChart: function () {
            if (this.histogramChart) {
                this.histogramChart.destroy();
            }
        },

        destroyRadialChart: function () {
            if (this.alarmRadialChart) {
                this.alarmRadialChart.destroy();
            }
        },

        /**
         * Create the histogram chart.
         * @method createHistogramChart
         */
        createHistogramChart: function () {
            this.destroyColumnChart();
            this.histogramChart = new Column({
                element: this.view.getFirstContentDetails(),
                data: [
                    {
                        label: dictionary.get("main.labelReal"),
                        value: []
                    },
                    {
                        label: dictionary.get("main.labelPredicted"),
                        value: []
                    },
                    {
                        label: dictionary.get("main.labelThresholdMin"),
                        value: []
                    },
                    {
                        label: dictionary.get("main.labelThresholdMax"),
                        value: []
                    },
                    {
                        label: dictionary.get("main.labelConfiguredBandwidth"),
                        value: []
                    }
                ],
                theme: {
                    legend: {
                        text: { // Theme options for text in legend (this.histogramChart.options.theme.legend.text.fontSize;)
                            fontSize: '10px'
                        }
                    }
                },
                plotOptions: {
                    orientation: 'horizontal',
                    column: {
                        datalabels: true
                    }
                },
                legend: {
                    events: {
                        click: 'toggle'
                    },
                    tooltip: {
                        format: function (entry) {
                            return entry.label;
                        }
                    }
                },
                tooltip: true
            });
        },

        /**
         * Update the histogram chart.
         * @method updateHistogramChart
         */
        updateHistogramChart: function (params, allDataForDetailsCharts) {
            if (this.showCharts) {
                this.updateRadialChart(allDataForDetailsCharts);

                var columnData = this.getHistogramChartData(allDataForDetailsCharts);
                this.histogramChart.update([
                    {
                        label: dictionary.get("main.labelReal"),
                        value: columnData.real
                    },
                    {
                        label: dictionary.get("main.labelPredicted"),
                        value: columnData.predicted
                    },
                    {
                        label: dictionary.get("main.labelThresholdMin"),
                        value: columnData.thresholdMin
                    },
                    {
                        label: dictionary.get("main.labelThresholdMax"),
                        value: columnData.thresholdMax
                    },
                    {
                        label: dictionary.get("main.labelConfiguredBandwidth"),
                        value: columnData.configuredBandwidth
                    }
                ]);

                // To uncomment in case tooltips are too large, and cannot be seen
                // this.adjustTooltips();
                // this.histogramChart.addEventHandler('entry:contextmenu', this.showContextMenu);
            }
        },

        /**
         * Create the radial chart.
         * @method createRadialChart
         */
        createRadialChart: function () {
            this.destroyRadialChart();
            this.numberOfSamples = 24; // In the future could be a dynamic value, for now is only 24.
            this.radialChart = new RadialChart({
                numberOfSamples: this.numberOfSamples
            });
            this.radialChart.attachTo(this.view.getSecondContentDetails());
        },

        /**
         * Update the radial chart.
         * @method updateRadialChart
         */
        updateRadialChart: function (allDataForDetailsCharts) {
            this.radialChart.hideTooltip();
            //TODO - Change the control, split it
            if (allDataForDetailsCharts && (allDataForDetailsCharts.ifObservationList && allDataForDetailsCharts.ifObservationList.length > 0 ||
                allDataForDetailsCharts.ifPredictionList && allDataForDetailsCharts.ifPredictionList.length > 0)) {
                this.radialChart.updateStacks(this.getRadialChartData(allDataForDetailsCharts));
                this.radialChart.attachTo(this.view.getSecondContentDetails());
            }
            this.radialChart.redraw();
        },

        /**
         * Process and retrive the datas for the radial chart.
         * @method getRadialChartData
         * @param allDataForDetailsCharts
         * @returns {{realData: Array, predictedData: Array}}
         */
        getRadialChartData: function (allDataForDetailsCharts) {
            var realData = [];
            var predictedData = [];
            var currentReal = 0;
            var currentPredicted = 0;

            var key = "index";
            var orderedRealData = FC.sortArrayByKey(allDataForDetailsCharts.ifObservationList, key);
            var orderedPredictedData = FC.sortArrayByKey(allDataForDetailsCharts.ifPredictionList, key);

            for (var index = 0; index < this.numberOfSamples; index++) {
                currentReal = orderedRealData[index] && FC.isNumber(orderedRealData[index].ifTrafficBandwidth) ?
                    orderedRealData[index].ifTrafficBandwidth : 0;
                currentPredicted = orderedPredictedData[index] && FC.isNumber(orderedPredictedData[index].predictedValue) ?
                    orderedPredictedData[index].predictedValue : 0;

                realData.push(currentReal);
                predictedData.push(currentPredicted);
            }
            return {
                realData: realData,
                predictedData: predictedData
            };
        },

        /**
         * Process and retrive the datas for the histogram chart.
         * @method getHistogramChartData
         * @param allDataForDetailsCharts
         * @returns {{}}
         */
        getHistogramChartData: function (allDataForDetailsCharts) {
            var columnData = {};
            columnData.real = allDataForDetailsCharts.ifTrafficBandwidth.ifTrafficBandwidth;
            columnData.predicted = allDataForDetailsCharts.ifPredictedTrafficBandwidth.predictedValue;
            columnData.thresholdMin = allDataForDetailsCharts.ifPredictedTrafficBandwidth.predictedLowerThreshold;
            columnData.thresholdMax = allDataForDetailsCharts.ifPredictedTrafficBandwidth.predictedUpperThreshold;
            columnData.configuredBandwidth = allDataForDetailsCharts.ifCurrentCapacity;

            return columnData;
        }

    });
});