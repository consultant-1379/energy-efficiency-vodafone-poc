define([
    'jscore/core',
    './MainView',
    '../../widgets/verticalSlider/VerticalSlider',
    '../../widgets/tabsWithMenuButton/TabsWithMenuButton',
    '../../widgets/dataTable/DataTable',
    '../../types/types',
    '../../widgets/networkMap/NetworkMap',
    '../../common',
    'jscore/ext/net',
    'i18n!energyefficiency/dictionary.json',
    '../../datastore/datastore',
    'chartlib/charts/MasterDetailLine'
], function (core, View, VerticalSlider, Tabs, DataTable, types, NetworkMap, FC, net, dictionary, datastore, MasterDetailLine) {

    var tabTypes = [];  // Names of the tables associated to the tabs
    var tabIndex = {};  // Tab index for each table
    var tabStores = {}; // datastore to be read/updated when accessing the relevant tab

    return core.Region.extend({

        View: View,

        VIEW_CHANGE_ZOOM_CHART_EVENT: 'display-range:change',
        UPDATE_SAVINGS_ZOOM_CHART_EVENT: 'charts:update:savings',

        /**
         * Initialize the layout of tables managed by hte right area, including map (on top) and info tables (on bottom).
         * @method onStart
         */
        onStart: function () {
            this.justCreated = {};
            this.table = {};
            this.current = undefined;
            this.showCharts = false;
            this.view.showLineChart(this.showCharts);
            this.view.setHeaderNetworksMap(dictionary.get('networkTopologyMap'));

            // TABS Tables - Begin
            tabTypes = [
                FC.nodesLabel,
                FC.linksLabel,
                FC.bondingLabel
            ];
            tabIndex = {
                'nodes': 0,
                'links': 1,
                'bonding': 2
            };
            tabStores = {
                'nodes': ['nodes'],
                'links': ['links'],
                'bonding': ['bonding']
            };
            // TABS Tables - End

            var heightTop = localStorage["eaEnergyEfficiency-mapHeight"] !== undefined ? localStorage["eaEnergyEfficiency-mapHeight"] : 500;

            // Initialize the slider separating the map from the data tables on the bottom
            this.slider = new VerticalSlider({
                minTopHeight: 150,
                minBottomHeight: 50,
                topHeight: heightTop,
                bottomHeight: 150,
                onTopChanged: this.onTopChanged.bind(this),
                onBottomChanged: this.onBottomChanged.bind(this)
            });
            this.slider.attachTo(this.view.getElement());

            this.slider.addEventHandler('topChanged', this.onTopChanged, this);
            this.slider.addEventHandler('bottomChanged', this.onBottomChanged, this);
            this.slider.getBottomDiv().setStyle('height', '100%');
            this.slider.getBottomDiv().setStyle('overflow-y', 'hidden');
            this.slider.getBottomDiv().setStyle('overflow-x', 'hidden');

            // Initialize the network map
            this.map = new NetworkMap({
                height: "100%"
            });
            this.mapOpen = true;
            this.showMap();

            // Initialize the tabs and define the associated side menu
            this.tabs = new Tabs({
                curTab: 0,
                menu: {
                    title: dictionary.get("main.tableActions"),
                    items: [
                        {
                            name: dictionary.get("main.columnSettingsTitle"),
                            icon: 'settings',
                            action: this.showTableSettings.bind(this)
                        },
                        // {
                        //     name: 'Search',
                        //     icon: 'search',
                        //     action: function () {
                        //         FC.searchDialog(dictionary.get("search"), dictionary.get("main.searchFor"), this.searchTable, this);
                        //     }.bind(this)
                        // },
                        {
                            name: dictionary.get("main.filterUpperCase"),
                            icon: dictionary.get("main.filterLowerCase"),
                            action: function () {
                                this.current.table.toggleFilterRow();
                            }.bind(this)
                        },
                        {
                            name: dictionary.get("main.refreshUpperCase"),
                            icon: dictionary.get("main.refreshLowerCase"),
                            action: function () {
                                if (tabStores[this.whichTab]) {
                                    this.current.table.toggleFilterRow(false);
                                    this.getEventBus().publish('wait-start', dictionary.get("main.refreshing") + this.whichTab, 100);
                                    /*----------------------------------------------------------------------------------
                                     + In case you don't want to loose selection (table/map) after refresh comment the
                                     + the code below and uncomment the commented one after.
                                     */
                                    this.current.table.table.unselectAllIds();
                                    this.getEventBus().publish('reloadDataStore', {
                                        names: tabStores[this.whichTab] || [],
                                        show: this.whichTab,
                                        id: 0
                                    });
                                    /*----------------------------------------------------------------------------------
                                     * In case you don't want to loose selection (table/map) after refresh uncomment the
                                     * the code below and comment the code before.
                                     */
                                    /*
                                     this.getEventBus().publish('reloadDataStore', {
                                     names: tabStores[this.whichTab] || [],
                                     show: this.whichTab,
                                     id: (types[this.whichTab].selectedId || 0)
                                     });
                                     if(this.firstSelected && this.firstSelected>=0) {
                                     this.getEventBus().publish('tabSelect', this.whichTab, [this.firstSelected], this.selectedTabsWithSelectedRows);
                                     }
                                     */
                                    //----------------------------------------------------------------------------------
                                }
                            }.bind(this)
                        }
                    ]
                },
                height: this.slider.getBottomHeight()
            });

            for (var i = 0; i < tabTypes.length; ++i) {
                var whichTab = tabTypes[i];
                if (types[whichTab]) {
                    this.table[whichTab] = {'whichTab': whichTab, 'div': new core.Element()};
                    this.table[whichTab].div.setStyle('height', '100%');
                    this.tabs.addTab(types[whichTab].title, this.table[whichTab].div);
                }
            }
            this.tabs.addEventHandler('tabselect', this.onTabSelected, this);
            this.tabs.attachTo(this.slider.getBottomDiv());

            this.getEventBus().subscribe('layouts:leftpanel:afterchange', function (shown, panel) {
                this.widthChange(shown, this.options.leftWidth, panel);
                if (this.lineChart && this.showCharts) {
                    this.lineChart.redraw();
                }
            }.bind(this));

            this.getEventBus().subscribe('layouts:rightpanel:afterchange', function (shown, panel) {
                this.isRightPanelOpen = shown;
                this.actionValueRightPanel = panel;
                this.widthChange(shown, this.options.rightWidth, panel);
                if (this.lineChart && this.showCharts) {
                    this.lineChart.redraw();
                }
            }.bind(this));

            this.getEventBus().subscribe('updateTabs', this.updateTabTitles.bind(this));
            this.getEventBus().subscribe('redrawMap', this.redrawMap.bind(this));
            this.getEventBus().subscribe('clearMap', this.clearMap.bind(this));
        },

        showCurrentAndExpectedGraphForSelectedId: function (ids) {
            this.allDataForCurrentDay = undefined;
            this.allDataForDetailsCharts = undefined;

            if (ids !== undefined && ids.length > 0) {
                this.updateRadialChart = true;
                if (!this.selectedId && this.isRowSelected) {
                    this.selectedId = ids[0];
                }
            } else {
                this.updateRadialChart = false;
            }

            if (this.selectedId && this.whichTab !== FC.nodesLabel) {
                this.getDataForSelectedRowFromEEE(JSON.stringify(this.createDataRequestForEEECall(FC.PREVIOUS_DAY)), FC.PREVIOUS_DAY);
            } else {
                this.showCharts = false;
                this.publishRowTabSelectForDetails();
            }
        },

        hideCurrentAndExpectedGraph: function () {
            this.showCharts = false;
            this.view.showLineChart(false);
            this.stopTimeTick();
        },

        onStop: function () {
            this.stopTimeTick();
            if (this.restoreChartPositionEvent) {
                this.getEventBus().unsubscribe(this.VIEW_CHANGE_ZOOM_CHART_EVENT, this.restoreChartPositionEvent);
            }
            if (this.updateSavingsEvtId) {
                this.getEventBus().unsubscribe(this.UPDATE_SAVINGS_ZOOM_CHART_EVENT, this.updateSavingsEvtId);
            }
        },

        /**
         * Update the title of the table tabs, including an update of the record number.
         * @method updateTabTitles
         * @param {type} tab - The table to be updated (unique if specified, otherwise all the tabs are updated)
         * @param {type} n - The number of records to be displayed (if specified, otherwise the number of entries in the collection is shown)
         */
        updateTabTitles: function (tab, n) {
            if (tab !== undefined && types[tab] !== undefined) {
                for (var i = 0; i < tabTypes.length; ++i) {
                    if (tab === tabTypes[i]) {
                        var rowNumber = n !== undefined ? n : types[tab].collection._collection.length;
                        this.tabs.setTabTitle(i, types[tab].title + ' (' + rowNumber + ')');
                        break;
                    }
                }
            } else {
                for (var x = 0; x < tabTypes.length; ++x) {
                    var t = tabTypes[x];
                    this.tabs.setTabTitle(x, types[t].title + ' (' + types[t].collection._collection.length + ')');
                }
            }
            this.tabs.setSelectedTab(this.tabs.options.curTab);
        },

        /**
         * Callback to manage the selection of a tab.
         * @method onTabSelected
         * @param {type} title - The title of the selected tab
         * @param {type} index - The index of the selected tab
         */
        onTabSelected: function (title, index) {
            this.tabs.options.curTab = index;
            this.showTable(tabTypes[index]);
        },

        /**
         * Show the table associated to the given tab.
         * @method showTable
         * @param {type} tab - Selected tab
         */
        showTable: function (tab) {
            this.whichTab = tab;
            if (this.current) {
                this.current.whichTab = this.whichTab;
            }
            if (this.map && this.map.map) {
                this.map.map.selectedItem = undefined;
                this.map.drawTopology();
            }
            if (this.tabs) {
                this.tabs.setSelectedTab(tabIndex[tab]);
            }
            if (this.table) {
                this.current = this.table[tab];
            }
            if (this.settingsShown) {
                this.settingsShown = false;
                this.getEventBus().publish('layouts:closerightpanel');
            }
            if (this.current && !this.current.columns) {
                this.setupColumns();
            }
            this.fillTable(tab);

            if (types[tab] && types[tab].selectedId) {
                this.selectedId = types[tab].selectedId;
            } else {
                // Nothing selected on table so I clear the selection saved before
                this.map.removeSelectedBefore();
                this.selectedId = undefined;
            }

            this.map.setSelectedTabAndIds(tab, this.selectedId);
            this.current.table.redraw();
            this.signalSelection(tab, this.selectedId);
        },

        /**
         * Prepare the column layout of the current table retrieving the columns to show and their attributes from the local storage, if present.
         * @method setupColumns
         */
        setupColumns: function () {
            var i, attrib;
            var type = types[this.whichTab];
            // Retrieve the widths of the current table as stored in the localStorage
            this.current.widths = JSON.parse(localStorage.getItem('eaEnergyEfficiency-colwidths-' + this.whichTab) || '{}');
            // Retrieve the columns data as stored in the localStorage
            var stored = localStorage.getItem('eaEnergyEfficiency-columns-' + this.whichTab);
            // if set: [ { attribute: x, visible: y} ]
            var typeColumns = type.columns;
            var cols = stored ? JSON.parse(stored) : typeColumns;

            var columns = this.current.columns = [];
            var included = {};
            for (i = 0; i < cols.length; ++i) {
                attrib = cols[i].attribute;
                if (!type.attributes[attrib]) {
                    continue;
                }
                // skip non-existing columns
                columns.push({
                    attribute: attrib,
                    title: type.attributes[attrib].title,
                    visible: cols[i].visible !== false
                });
                included[attrib] = true;
            }
            if (stored) { // add 'new' columns
                for (i = 0; i < typeColumns.length; ++i) {
                    attrib = typeColumns[i].attribute;
                    if (!included[attrib]) {
                        columns.push({
                            attribute: attrib,
                            title: type.attributes[attrib].title,
                            visible: type.attributes[attrib].visible !== false
                        });
                    }
                }
            }
        },

        /**
         * Assign the widths of the columns
         * @method setWidths
         */
        setWidths: function () {
            var widths = this.current.widths;
            var columns = this.current.columns;
            for (var i = 0; i < columns.length; ++i) {
                var width = widths[columns[i].attribute];
                if (width) {
                    columns[i].width = width;
                }
            }
        },

        /**
         * Fill the current table with the relevant content.
         * @method fillTable
         */
        fillTable: function () {
            this.setWidths();
            if (this.current && this.current.table) {
                this.current.table.redraw();
                return;
            }
            // (re)create the table.
            var type = types[this.whichTab];
            var cols = type.columns;
            this.redisplayTable(cols);
        },

        /**
         * Redraw the current table with the given columns.
         * @method redisplayTable
         * @param {type} columns
         */
        redisplayTable: function (columns) {
            var whichTab = this.current.whichTab;
            var type = types[whichTab];
            var idsSelectedBefore = [];
            if (!this.current.columns) {
                this.current.columns = type.columns.slice(0);
            }
            if (columns) { // rearranged
                this.current.columns = columns;
            }
            if (this.current.table) {
                idsSelectedBefore = this.current.table.table.getSelectedIds();
                this.current.table.destroy();
            }

            var columnRenderers = [];
            var rowRenderer = function (columnRenderers, columns, rows, data) {
                if (columnRenderers && columns) {
                    for (var i = 0; i < rows.length; i++) {
                        var tableRow = rows[i].getElement()._getHTMLElement();
                        for (var iCell = 0; iCell < tableRow.children.length; ++iCell) {
                            var columnInfo = columns[iCell];
                            if (columnRenderers[columnInfo.attribute]) {
                                if (tableRow.children[iCell] && tableRow.children[iCell].childNodes && tableRow.children[iCell].childNodes[0]) {
                                    tableRow.children[iCell].childNodes[0].nodeValue = columnRenderers[columnInfo.attribute](columnInfo.attribute, data[i]);
                                }
                            }
                        }
                    }
                }
            };

            //Not Used // Row/Column renderers of the various tables
            // if (whichTab === "statusMessages") {
            //     rowRenderer = function (columnRenderers, columns, rows, data) {
            //         for (var i = 0; i < rows.length; i++) {
            //             if (data[i].status.substring(0, 5) === "error") {
            //                 rows[i].getElement().setStyle({backgroundColor: 'LightCoral'});
            //             } else if (data[i].status.substring(0, 7) === "warning") {
            //                 rows[i].getElement().setStyle({backgroundColor: 'LightGoldenRodYellow'});
            //             } else if (data[i].status.substring(0, 7) === "success") { // success
            //                 rows[i].getElement().setStyle({backgroundColor: 'LightGreen'});
            //             }
            //             rows[i].getElement().setStyle({border: 'solid black 1px'});
            //         }
            //     };
            // }

            var table = new DataTable({
                type: type,
                columns: this.current.columns,
                collection: types[whichTab].collection,
                rowRenderer: rowRenderer,
                columnRenderers: columnRenderers
            });
            this.current.table = table;

            table.attachTo(this.current.div);
            table.addEventHandler('columnresize', this.onColumnResize, this);
            table.addEventHandler('selected', this.rowSelection, this);

            // Management of the "justCreated" info
            var jc = this.justCreated[whichTab];
            this.justCreated[whichTab] = undefined;
            table.autoSelectRow(jc);
            this.current.table.table.addSelectedIds(idsSelectedBefore);
            this.selectedId = idsSelectedBefore;
        },

        /**
         * Search the current table for all the occurrences of the given text (wherever in the table data).
         * @method searchTable
         * @param {type} text
         * @returns {Number}
         */
        // // NOT USED for now
        // searchTable: function (text) {
        //     // this.current.table.table.unselectAllIds();
        //     var nSel = 0;
        //     var ids = [];
        //     for (var i = 0; i < this.current.table.sortedData.length; ++i) {
        //         var row = this.current.table.sortedData[i];
        //         var record = {};
        //         for (var j = 0; j < this.current.columns.length; ++j) {
        //             record[this.current.columns[j].attribute] = row[this.current.columns[j].attribute];
        //         }
        //         var string = JSON.stringify(record);
        //         if (string.search(text) !== -1) {
        //             ids.push(row.id);
        //             nSel++;
        //         }
        //     }
        //     // this.current.table.table.addSelectedIds(ids);
        //     return nSel;
        // },

        /**
         * Show the panel on the right side with the columns of the current table to be selected for visualization.
         * @method showTableSettings
         */
        showTableSettings: function () {
            var settings = this.current.table.getTableSettings();
            this.getEventBus().publish('layouts:showpanel', {
                header: dictionary.get("main.tableSettingsTitle"),
                content: settings,
                value: 'tableSettings',
                side: 'right'
            });
            settings.addEventHandler('change', function () {
                this.redisplayTable(settings.getUpdatedColumns());
            }.bind(this));
        },

        /**
         * Attach the map to the slider top div and define the relevant event handlers.
         * @method showMap
         */
        showMap: function () {
            this.map.attachTo(this.slider.getTopDiv());
            // this.map.addEventHandler('selected', function (whichTab, ids) {
            //     //TODO - Selection of link from map to table. OK for tab link (instead of double verse link). For tabs Nodes and Bonding???
            //     if (this.whichTab && this.whichTab === FC.linksLabel) {
            //         this.current.table.table.unselectAllIds();
            //         this.current.table.table.addSelectedIds(ids);
            //     }
            //     this.rowSelection(whichTab, ids);
            // }.bind(this));
        },

        /**
         * Resize the map as soon as the size of the parent area changes.
         * @method onTopChanged
         * @param {type} newHeight
         */
        onTopChanged: function (newHeight) {
            localStorage.setItem('eaEnergyEfficiency-mapHeight', newHeight);
            if (this.map && this.mapOpen) {
                this.map.invalidateSize();
            }
        },

        /**
         * Resize the tables as soon as the size of the parent area changes.
         * @method onBottomChanged
         * @param {type} newHeight
         */
        onBottomChanged: function (newHeight) {
            if (this.tabs) {
                this.tabs.setHeight(newHeight);
            }
            if (this.current && this.current.table) {
                this.current.table.redraw();
            }
        },

        /**
         * Reload the map sending the relevant command to the NetworkMap widget.
         * @method reloadMap
         */
        reloadMap: function () {
            if (this.map) {
                this.map.reload();
            }
        },

        /**
         * Generate the events related to the change of the current table.
         * @param {type} whichTab - The tab of current table
         * @param {type} ids - row selected id
         */
        rowSelection: function (whichTab, ids) {
            FC.hideToast();
            this.stopTimeTick();
            this.selectedId = this.current.table.getSelectedId();
            this.selectedTabsWithSelectedRows = [];

            if (!ids || ids.length === 0) {
                this.showCharts = false;
                this.isRowSelected = false;
                this.map.unhighlight(); // remove all highlights
                this.map.setSelectedTabAndIds(whichTab);
                this.hideCurrentAndExpectedGraph();
                this.publishRowTabSelectForDetails();
            } else if (this.whichTab !== undefined && this.whichTab !== FC.nodesLabel) {
                this.isRowSelected = true;
                this.map.setSelectedTabAndIds(whichTab, ids);
                this.showCurrentAndExpectedGraphForSelectedId(ids);
            } else {
                this.showCharts = false;
                this.isRowSelected = true;
                this.publishRowTabSelectForDetails();
            }
            var currentSelectedTabWithSelectedRow = {whichTab: whichTab, selected: this.isRowSelected};
            this.selectedTabsWithSelectedRows.push(currentSelectedTabWithSelectedRow);

            // Highlight Selected - Event handled in EnergyEfficiency with selectInput() function.
            this.getEventBus().publish('tabSelect', whichTab, ids, this.selectedTabsWithSelectedRows);
        },

        /**
         * Generate the events related to the change of the current table.
         * @method signalSelection
         * @param {type} whichTab - The new table
         * @param {type} ids - The highlighted ids (array, but currently one only item is supported)
         */
        signalSelection: function (whichTab, ids) {
            this.isRowSelected = false;
            ids = this.current.table.table.getSelectedIds();
            // Highlight Selected - Event handled in EnergyEfficiency with selectInput() function.
            this.getEventBus().publish('tabSelect', whichTab, ids, this.selectedTabsWithSelectedRows);
            FC.hideToast();

            this.selectedId = this.current.table.getSelectedId();
            this.isRowSelected = !!(ids && ids.length > 0);

            if (this.whichTab === FC.nodesLabel) {
                this.showCharts = false;
                this.hideCurrentAndExpectedGraph();
            }
            // Call EEE
            this.showCurrentAndExpectedGraphForSelectedId(ids);
        },

        publishRowTabSelectForDetails: function () {
            if (this.rowTabSelectForDetailsEvtId) {
                this.getEventBus().unsubscribe('rowTabSelectForDetails', this.rowTabSelectForDetailsEvtId);
            }

            this.paramsForRightPanels = {
                whichTab: this.whichTab,
                showCharts: this.showCharts,
                id: this.selectedId,
                isRowSelected: this.isRowSelected,
                isRightPanelOpen: this.isRightPanelOpen,
                actionValueRightPanel: this.actionValueRightPanel
            };
            this.rowTabSelectForDetailsEvtId = this.getEventBus().publish('rowTabSelectForDetails', this.paramsForRightPanels, this.allDataForDetailsCharts);
        },

        /**
         * Highlight the given item of the given table on the map.
         * @method highlightOnMap
         * @param {type} whichTab - The table
         * @param {type} id - The id of the item to be highlighted
         * @param {type} unhighlight - Choose if unhighlight before
         * @param {type} style - The highlight style of the item to be highlighted, default is green
         */
        highlightOnMap: function (whichTab, id, unhighlight, style) {
            if (this.map) {
                this.map.highlight(whichTab, id, unhighlight, style);
            }
        },

        /**
         * Highlight/Unhighlighte on map and on table/left panel the given table element.
         * @method highlight
         * @param {type} whichTab - The table
         * @param {type} id - The id of the item to be highlighted
         * @param {type} unhighlight - The id of the item to be unhighlighted
         * @param {type} style - The highlight style of the item to be highlighted, default is green
         */
        highlight: function (whichTab, id, unhighlight, style) {
            if (whichTab === FC.bondingLabel) {
                id = types.bonding.getLinksOfBonding(this.selectedId);
            }
            this.highlightOnMap(whichTab, id, unhighlight, style);
        },

        redrawMap: function () {
            this.map.reload();
        },

        clearMap: function () {
            types.nodes.clearPositions();
            this.map.destroy();
            this.map = new NetworkMap({
                height: "100%"
            });
            this.mapOpen = true;
            this.showMap();
        },

        /**
         * Manage the change of the width of the map/table area.
         * @param {type} shown - True if the panel is shown or not
         * @param {type} width - The new width of the left/right panel
         * @param {type} panel - The name of the panel shown
         */
        widthChange: function (shown, width, panel) {
            this.map.panBy(shown ? width / 2 : -width / 2, 0);
            // panBy +dx => shift left; it's panning the window, not the content.
            var whichTab = this.whichTab;
            if (!shown && whichTab && this.current.table) {
                this.current.table.kickColumns(width);
            }
            if (panel.toString() === "tableSettings") {
                this.settingsShown = shown;
            }
        },

        onUpdate: function () {
            this.showCurrentAndExpectedGraphForSelectedId();
        },

        startTimeTick: function () {
            this.stopTimeTick();
            this._timeTickInterval = setInterval(function () {
                this.onUpdate(this.whichTab);
            }.bind(this), FC.getIntervalCall());
        },

        stopTimeTick: function () {
            if (this._timeTickInterval) {
                clearInterval(this._timeTickInterval);
            }
        },

        /**
         * Create the main chart with zoom.
         * @method createLineChart
         */
        createLineChart: function () {
            if (this.lineChart) {
                this.lineChart.destroy();
            }
            var dateTimeRequest = this.allDataForCurrentDay.time;
            localStorage.setItem('dateTimeRequest', dateTimeRequest);

            this.lineChart = new MasterDetailLine({
                element: this.view.getLineChartContent(),
                data: [
                    {
                        label: dictionary.get("main.labelReal"),
                        data: this.filteredDatasForLineChart.realData
                    },
                    {
                        label: dictionary.get("main.labelPredicted"),
                        data: this.filteredDatasForLineChart.predictedData
                    },
                    {
                        label: dictionary.get("main.labelConfiguredBandwidth"),
                        data: this.filteredDatasForLineChart.configuredBandwidth
                    }
                ],
                theme: { // Options for coloring and fonts
                    colors: [
                        '#89ba17',
                        '#00a9d4',
                        '#fabb00'
                    ]
                },
                plotOptions: {
                    line: {
                        datalabels: false,
                        chartType: 'line',
                        interpolateMode: 'linear', //cardinal
                        references: [{
                            x: this.allDataForCurrentDay.ifTrafficBandwidth.time,
                            options: {
                                'stroke-dasharray': [8, 8],
                                'stroke-width': 4,
                                stroke: '#e32119'
                            }
                        }]
                    },
                    area: {
                        references: [
                            {
                                x1: this.allDataForCurrentDay.ifTrafficBandwidth.time, // Current time
                                x2: this.getTimeForX2Point(), // Max Time avialable
                                options: { // styles/attributes applied to the area after the Stroke.
                                    opacity: 0.2,
                                    fill: '#16a64d',
                                    length: 'auto'
                                }
                            }
                            //, // Example for Horizontal Refrence if needed.
                            // { // If we want to color also Horizontally an Area (for i.e. for thresholds)
                            //     y1: 300,
                            //     y2: 800,
                            //     options: {
                            //         fill: '#e32119',
                            //         opacity: 0.3
                            //     }
                            // }
                        ]
                    },
                    scaleType: {
                        x: 'time',
                        y: 'linear'
                    }
                },
                grid: {
                    gridPadding: {},
                    tickSize: {
                        x: {
                            height: 30,
                            width: 50
                        }
                    },
                    tickFormat: {
                        y: function (d) {
                            return d;
                        },
                        x: '%X'  // %X is equal to '%H:%M:%S' format
                    },
                    axisLabels: {
                        x: function () {
                            var date = new Date(dateTimeRequest);
                            var options = {weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'};
                            var locales = 'en-US';  //TODO with the right locales, passed from server.
                            return date.toLocaleDateString(locales, options);
                        },
                        y: 'Bdw [Mbps]'
                    }
                },
                legend: {
                    events: {
                        click: 'toggle'
                    },
                    tooltip: {
                        format: function (entry) {
                            return entry.label + dictionary.get("main.labelData");
                        }
                    }
                },
                tooltip: {
                    interpolate: false
                }
            });
        },

        getTimeForX2Point: function () {
            if (this.allDataForCurrentDay.ifPredictionListSize && this.allDataForCurrentDay.ifPredictionListSize > 0) {
                var lengthList = this.allDataForCurrentDay.ifPredictionListSize - 1;
                return this.allDataForCurrentDay.ifPredictionList.filter(function (ifPrediction) {
                    return ifPrediction.index === lengthList;
                })[0].time;
            }
        },

        restoreChartPosition: function (range) {
            //TODO - To verify if can be done better, after every creation of the chart it loose the zoom selected before,
            // if we update the zoom is Ok, but the area after the vertical references is not shifting anymore.
            var newRange = this.timestampToPercentage(range);
            var isDefaultPosition = (newRange.start === 40 && newRange.end === 60);
            if (this.newPositionSet && (newRange.end - newRange.start === 1)) {
                // If difference is 1, it loops, so we add 1.
                newRange.end = newRange.end + 1;
            }
            if (!isDefaultPosition) {
                this.newPercRangePosition = newRange;
                this.newTimestampRangePosition = range;
            }
            if (this.newPercRangePosition) {
                this.newPositionSet = this.newPercRangePosition;
                this.lineChart.setPanelPosition(this.newPercRangePosition);
            }
        },

        timestampToPercentage: function (tsRange) {
            var dataRange = this.getDataMinMaxRange(), rangeLength = dataRange.max - dataRange.min;
            return {
                start: Math.floor((tsRange.start - dataRange.min) * (100 / rangeLength)),
                end: Math.ceil((tsRange.end - dataRange.min) * (100 / rangeLength))
            };
        },
        getDataMinMaxRange: function () {
            return {
                min: this.filteredDatasForLineChart.predictedData[0].x,
                max: this.filteredDatasForLineChart.predictedData[this.totalSamples - 1].x
            };
        },

        initializeMainChart: function (mode) {
            if (this.allDataForCurrentDay) {
                FC.setIntervalCall(this.allDataForCurrentDay.timeInterval); // Set the interval of rest calls from BE.
            }
            if (this.allDataForCurrentDay.ifMonitoredState === 1) {
                this.showCharts = true;
                if (this.isRowSelected) {
                    this.startTimeTick();
                    this.showCharts = true;
                } else {
                    this.stopTimeTick();
                    this.showCharts = false;
                }

                this.savingPerctangeValues = this.allDataForCurrentDay.ifSaving;
                this.totalSamples = 0;

                if (this.allDataForCurrentDay && this.allDataForCurrentDay.ifPredictionListSize > 0) {
                    this.view.showLineChart(this.showCharts);
                    this.view.setLineChartHeader(dictionary.get('main.lineChartHeader').replace('$1', "\"" + this.selectedId + "\""));
                    this.totalSamples = this.allDataForCurrentDay.ifPredictionListSize;
                    this.filteredDatasForLineChart = this.getDatasForLineChart();

                    this.createLineChart();
                    if (this.restoreChartPositionEvent) {
                        this.getEventBus().unsubscribe(this.VIEW_CHANGE_ZOOM_CHART_EVENT, this.restoreChartPositionEvent);
                    }
                    this.restoreChartPositionEvent = this.lineChart.addEventHandler(this.VIEW_CHANGE_ZOOM_CHART_EVENT, this.restoreChartPosition.bind(this));

                    // REST Call to operational ODL to check if there are changes in bandwidth, to change the color of the selected link/bonding.
                    var idDecoded;
                    if (this.whichTab === FC.bondingLabel) {
                        var ids = types.bonding.getLinksOfBonding(this.selectedId);
                        for (var i = 0; i < ids.length; i++) {
                            idDecoded = FC.replaceChar(ids[i], FC.SLASH, FC.SLASH_ENCODED);
                            this.callODLForChangeBandwidthCheck(idDecoded);
                        }
                    } else {
                        idDecoded = FC.replaceChar(this.selectedId, FC.SLASH, FC.SLASH_ENCODED);
                        this.callODLForChangeBandwidthCheck(idDecoded);
                    }
                }

                if (this.isRightPanelOpen && this.showCharts) {
                    // Update gauge charts in Savings panel or Details charts panel.
                    if (this.actionValueRightPanel === FC.savingsLabel) {
                        if (this.updateSavingsEvtId) {
                            this.getEventBus().unsubscribe(this.UPDATE_SAVINGS_ZOOM_CHART_EVENT, this.updateSavingsEvtId);
                        }
                        this.updateSavingsEvtId = this.getEventBus().publish(this.UPDATE_SAVINGS_ZOOM_CHART_EVENT, this.savingPerctangeValues);
                    }
                }
            } else {
                this.showCharts = false;
                this.view.showLineChart(this.showCharts);
                FC.showToast(dictionary.get("notMonitoredEEToastMessage"), this.getElement());
            }
            this.publishRowTabSelectForDetails();
        },

        checkForBandwidthChangeForSelectedId: function (data, idDecoded) {
            var color = FC.BLU_COLOR;
            var weight = 3;
            var operStatus = data["ietf-network-topology:link"][0]["ietf-te-topology:te"].state["oper-status"];

            var selectedIdUncoded = FC.replaceChar(idDecoded, FC.SLASH_ENCODED, FC.SLASH);
            var linkType = types.links.collection.getModel(selectedIdUncoded).attributes.linkType;
            var dashArray = (linkType === "radioLink") ? "10, 5" : "1";

            //Updating status of "Op. state" in "Links" tab
            types.links.collection.getModel(selectedIdUncoded).setAttribute("opState", operStatus);
            if (linkType === "bondingLogLink") {
                //Updating status of "Op. state" in "RL Bonding" tab
                types.bonding.collection.getModel(selectedIdUncoded).setAttribute("opState", operStatus);
            }

            var bondingId = null;
            if (this.whichTab === FC.linksLabel) {
                bondingId = types[this.whichTab].collection.getModel(this.selectedId).attributes.bondingId;
            }

            //Saving for Bonding (Link Down management)
            if (FC.OP_STATUS_DOWN === operStatus) {
                if (this.whichTab === FC.linksLabel) {
                    if (bondingId) {
                        color = FC.GREY_COLOR;
                        dashArray = "0.5, 4"; //.........
                    }
                    else if (linkType === "bondingLogLink") {
                        color = FC.GREY_COLOR;
                        dashArray = "0.5, 8"; //o o o o o o
                    }
                    else {
                        //Radio Link down color to be defined.
                        //According with Cinzia Saverino, at the moment will be left FC.BLU_COLOR
                        //color = FC.YELLOW_COLOR;
                    }
                }
                else {
                    color = FC.GREY_COLOR;
                    dashArray = "0.5, 4"; //.........
                }
            }
            //Saving for Radio Link
            else if (this.allDataForCurrentDay && this.allDataForCurrentDay.ifCurrentCapacity && this.allDataForCurrentDay.ifMaximumCapacity &&
                this.allDataForCurrentDay.ifCurrentCapacity < this.allDataForCurrentDay.ifMaximumCapacity) {
                if ((linkType === "radioLink") && (bondingId === undefined)) {
                    color = FC.YELLOW_COLOR; //this.map.highlightYellowColor;
                }
            }

            var style = {
                color: color,
                weight: (linkType === "bondingLogLink") ? (weight + 3) : weight,
                dashArray: dashArray
            };

            this.highlight(this.whichTab, selectedIdUncoded, undefined, style);
        },

        callODLForChangeBandwidthCheck: function (idDecoded) {
            net.ajax({
                url: datastore.ODL_OPERATIONAL_URL + idDecoded,
                type: "GET",
                dataType: "json",
                contentType: "application/json",
                crossOrigin: true,
                crossDomain: true,
                headers: {
                    Authorization: "Basic " + window.btoa(FC.USERNAME_ODL + ":" + FC.PASSWORD_ODL),
                    "Access-Control-Allow-Origin": datastore.ODL_TOPOLOGY_URL
                },
                success: function (data, xhr) {
                    this.checkForBandwidthChangeForSelectedId(data, idDecoded);
                }.bind(this),
                error: function (msg, xhr) {
                    //TODO - to remove
                    if (datastore.IS_LOCAL_MODE) {
                        this.checkForBandwidthChangeForSelectedId(datastore.getLinkInfoFromODL(), idDecoded);
                    } else {

                        FC.errorDialogFromXHR(msg, xhr);
                    }
                }.bind(this)
            });
        },

        createDataRequestForEEECall: function (mode) {
            return {
                ifRef: types[this.whichTab].collection.getModel(this.selectedId).attributes.ifRef,
                networkRef: FC.NETWORK_REF_MN_TOPO,
                mode: mode
            };
        },

        getDataForSelectedRowFromEEE: function (request, mode) {
            net.ajax({
                url: datastore.EEE_URL,
                type: "POST",
                dataType: "json",
                contentType: "application/json",
                cache: false,
                data: request,
                crossOrigin: true,
                crossDomain: true,
                success: function (data, xhr) {
                    if (FC.MODE_CURRENT_DAY === mode) {
                        this.allDataForCurrentDay = data;
                        datastore.currentDayChartDataCache = this.allDataForCurrentDay;
                        this.initializeMainChart(mode);
                    } else if (FC.PREVIOUS_DAY === mode) {
                        this.allDataForDetailsCharts = data;
                        datastore.last24HoursChartDataCache = this.allDataForDetailsCharts;
                        this.getDataForSelectedRowFromEEE(JSON.stringify(this.createDataRequestForEEECall(FC.MODE_CURRENT_DAY)), FC.MODE_CURRENT_DAY);
                    }
                }.bind(this),
                error: function (msg, xhr) {

                    //TODO - To remove, added for vapp usage purpose
                    if (datastore.IS_LOCAL_MODE) {
                        if (FC.MODE_CURRENT_DAY === mode) {
                            this.allDataForCurrentDay = datastore.getEngineURL(mode);
                            datastore.currentDayChartDataCache = this.allDataForCurrentDay;
                            this.initializeMainChart(mode);
                        } else if (FC.PREVIOUS_DAY === mode) {
                            this.allDataForDetailsCharts = datastore.getEngineURL(mode);
                            datastore.last24HoursChartDataCache = this.allDataForDetailsCharts;
                            this.getDataForSelectedRowFromEEE(JSON.stringify(this.createDataRequestForEEECall(FC.MODE_CURRENT_DAY)), FC.MODE_CURRENT_DAY);
                        }
                    } else {
                        FC.errorDialogFromXHR(msg, xhr);
                        if (FC.PREVIOUS_DAY === mode) {
                            this.getDataForSelectedRowFromEEE(JSON.stringify(this.createDataRequestForEEECall(FC.MODE_CURRENT_DAY)), FC.MODE_CURRENT_DAY);
                        }
                    }

                }.bind(this)
            });
        },

        getDatasForLineChart: function () {
            var realDatas = [];
            var predictedDatas = [];
            var configuredBandwidth = [];
            var newX = 0;
            var newY = 0;

            if (this.allDataForCurrentDay && this.allDataForCurrentDay.ifObservationListSize > 0) {
                for (var i = 0; i < this.allDataForCurrentDay.ifObservationListSize; i++) {
                    newX = this.allDataForCurrentDay.ifObservationList[i].time;
                    newY = this.allDataForCurrentDay.ifObservationList[i].ifTrafficBandwidth;
                    realDatas.push({
                        x: newX,
                        y: newY
                    });
                }
            }

            if (this.totalSamples) {
                for (var dataIndex = 0; dataIndex < this.totalSamples; dataIndex++) {
                    newX = this.allDataForCurrentDay.ifPredictionList[dataIndex].time;
                    newY = this.allDataForCurrentDay.ifPredictionList[dataIndex].predictedValue;
                    predictedDatas.push({
                        x: newX,
                        y: newY
                    });
                }
            }

            if (this.allDataForCurrentDay && this.allDataForCurrentDay.ifConfiguredBandwidthListSize > 0) {
                for (var j = 0; j < this.allDataForCurrentDay.ifConfiguredBandwidthListSize; j++) {
                    newX = this.allDataForCurrentDay.ifConfiguredBandwidthList[j].time;
                    newY = this.allDataForCurrentDay.ifConfiguredBandwidthList[j].ifConfiguredBandwidth;
                    configuredBandwidth.push({
                        x: newX,
                        y: newY
                    });
                }
            }

            data = {
                realData: realDatas,
                predictedData: predictedDatas,
                configuredBandwidth: configuredBandwidth
            };

            return data;
        }

    });

});