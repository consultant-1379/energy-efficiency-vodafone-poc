define([
    'jscore/core',
    'jscore/ext/net',
    'i18n!energyefficiency/dictionary.json',
    'layouts/TopSection',
    'layouts/MultiSlidingPanels',
    './regions/left/Left',
    './regions/details/Details',
    'jscore/ext/locationController',
    'container/api',
    './regions/main/Main',
    './types/types',
    './datastore/datastore',
    './common',
    './regions/savings/Savings'
], function (core, net, dictionary, TopSection, MultiSlidingPanels, Side, Details, LocationController, container, Main,
             types, datastore, FC, Savings) {
    'use strict';

    var LEFT_WIDTH = 250;   // Width of the Left panel
    var RIGHT_WIDTH = 320;  // Width of the Right panel

    return core.App.extend({
        /**
         * 'EnergyEfficiency' is the entry point for the UI.
         * <h2>Types</h2>
         * **types/Type** is a class that records information about the various types, with information about the attribute
         * types: **types/nodes**, **types/links**, **types/bonding**, **types/statusmessages**, each defines
         * a 'Type' instance.
         * **types/types** is an object containing each of those 'Type' objects as properties, and a 'fetch()' method,
         * for fetching a number of those. Each 'types/Type' has information about how to display it in a ('DataTable') table.
         * If files A and B both depend on 'types/nodes' they get exactly the same instance. So then if class A subscribes
         * to the change handler of the nodes collection, and B changes a node model, A's event handler will be called.
         * Instead of passing types/collections in constructors etc., all files depend on 'types/types' or on an individual 'types/nodes'.
         * <h2>Regions</h2>
         * Normally the GUI shows the **Main** region, containing a 'NetworkMap' at the top and tabs with 'DataTable'-s
         * at the bottom, possibly with sidebars left (network topology) and right (details of lags / e-lines selected from the table).
         * <h2>Widgets</h2>
         * <h3>Generic widgets</h3>
         * - **MapMenu**: a menu button / drop down menu on a map.
         * - **SettingsPanel**: a 'tablelib/TableSettings' widget with an additional "restore default" button.
         * - **TabsWithMenuButton**: a 'widgets/Tabs' widget with a context menu.
         * - **VerticalSlider**: a vertical slider to split a region into two adjustable sub-regions.
         * <h3>App specific widgets</h3>
         * **DataTable** displays a table for one of the types.
         * **NetworkMap** shows a map with network elements and links, with selection, highlighting, and a menu from whichTab
         * one can create new network elements, links or traffic demands, place the network on the world map, and
         * perform single fault analysis. It is by far the biggest class in the project (with a mix of display logic and
         * business logic, so it could do with refactoring).
         * @module energyefficiency
         * @class /EnergyEfficiency
         * @extends core.App
         * constructor
         */

        /**
         * Called on application start, initializes the event handlers for the basic events managed by the GUI.
         * @param {type} parent
         */
        onStart: function (parent) {
            this.retryCount = 0;
            this.retryLimit = 3;
            this.RETRY_INTERVALS = [2000, 5000, 10000];
//            window.app = this;
            this.getEventBus().subscribe('dataStoreLoaded', this.onDatastoreLoaded.bind(this));
            this.getEventBus().subscribe('dataStoreUpdated', this.onDatastoreUpdated.bind(this));
            this.getEventBus().subscribe('reloadDataStore', this.refreshDatastore.bind(this));

            this.currentlySelected = {whichTab: '', id: ''};
            this.setupGui();

            this.getEventBus().subscribe('rowTabSelectForDetails', this.hideAndShowRightPanel.bind(this));

            //NOT used for now - Context Menu (Right click on Mouse) on Histogram Chart
            // container.getEventBus().subscribe('Details:notify-operation', function (server) {
            // }.bind(this));
        },

        // Not Used for now
        // showError: function(header, msg) {
        //     types.statusMessages.addMessage(msg, header);
        // },

        /**
         * Start the GUI if the datastore is reachable and correctly initialized.
         * @method setupGui
         */
        setupGui: function () {
            this.showMainWindow();
            this.readTopology(); // this.readEline();
        },

        // /**
        //  * Read Elines from ODL-Controller.
        //  * @method readEline
        //  */
        // readEline: function () {
        //     // Not requested for NOW.
        //     net.ajax({
        //         url: ELINES_URL,
        //         type: "GET",
        //         dataType: "json",
        //         cache: false,
        //         headers: {
        //             "Authorization": "Basic " + window.btoa(FC.FC.USERNAME_ODL_ODL + ":" + FC.PASSWORD_ODL)
        //         },
        //         success: function (data, xhr) {
        //             datastore.eLineDataCache = data;
        //             types.elines.collectionFromController(data);
        //             // Call rest to read Topology, NetworkElements, Links and Bondings.
        //             this.readTopology();
        //         }.bind(this),
        //         error: function (data, xhr) {
        //             //For now in case not in the same domanin change and call again the rest, for local test.
        //             TOPO_URL = "http://localhost:8181"+datastore.getTopologyURL();
        //             ELINES_URL = "http://localhost:8181"+datastore.getElineURL();
        //             this.retryRequest();
        //             // if (xhr.getStatus() === 400 || xhr.getStatus() === 404) {
        //             //     // Page not found is considered empty datastore.
        //             //     datastore.eLineDataCache = data;
        //             // } else {
        //             //     FC.errorDialog('Failure', 'Controller read failed');
        //             // }
        //             // this.readTopology();
        //             // types.statusMessages.addMessage("Couldn't read topology : " + datastore.getElineURL(), "error " + xhr.getStatus());
        //         }.bind(this)
        //     });
        // },

        /**
         * Read Topology, NetworkElements, Links and Lags from ODL-Controller.
         * @method readTopology
         */
        readTopology: function () {
            //TODO - method changed for vapp usage purpose.
            net.ajax({
                url: datastore.ODL_TOPOLOGY_URL,
                type: "GET",
                dataType: "json",
                cache: false,
                headers: {
                    "Authorization": "Basic " + window.btoa(FC.USERNAME_ODL + ":" + FC.PASSWORD_ODL)
                },
                success: function (data, xhr) {
                    datastore.topologyDataCache = data;
                    if (this.leftContentPanel) {
                        this.leftContentPanel.loadTree(datastore.topologyDataCache);
                    }
                    this.publishEventsForDataStoreLoaded(datastore.topologyDataCache);
                }.bind(this),
                error: function (msg, xhr) {
                    //TODO - To remove
                    if (datastore.IS_LOCAL_MODE) {
                        datastore.topologyDataCache = datastore.getTopologyURL();
                        if (this.leftContentPanel) {
                            this.leftContentPanel.loadTree(datastore.topologyDataCache);
                        }
                        this.publishEventsForDataStoreLoaded(datastore.topologyDataCache);
                        // var message = "Could not call ODL";
                        // types.statusMessages.addMessage(message, "error " + xhr.getStatus());
                        // TODO - For now in case not in the same domanin change and call again the rest, for local test.
                        // TOPO_URL = "http://localhost:8181" + datastore.getTopologyURL();
                        // ELINES_URL = "http://localhost:8181"+datastore.getElineURL();
                        //
                        // this.retryRequest();
                    } else {
                        FC.errorDialogFromXHR(msg, xhr);
                    }
                }.bind(this)
            });
        },

        /**
         * Retry current request at set intervals.
         * @method retryRequest
         */
        //TODO NOT used for now.
        retryRequest: function () {
            if (this.RETRY_INTERVALS[this.retryCount]) {
                this.resultsRetryTimeout = setTimeout(function () {
                    this.readTopology();
                    clearTimeout(this.resultsRetryTimeout);
                }.bind(this), this.RETRY_INTERVALS[this.retryCount]);
                this.retryCount++;
            } else {
                FC.errorDialog('Failure', 'Retried 3 times to call services, make sure that you have already PUT Topology and ELines Data'); //TODO - To change msg
                this.publishEventsForDataStoreLoaded();
            }
        },

        changeZIndex: function () {
            //If needed add timeout.
            // Overlap of zoom button with the top section when is maximized.
            if (this.getElement().find('.elMapping-ZoomControls') !== undefined) {
                this.getElement().find('.elMapping-ZoomControls').setStyle("zIndex", 9); // was z-index: 100
            }
            // Overlap of menu button with the top section when is maximized.
            if (this.getElement().find('.elMapping-Map-renderer-right') !== undefined) {
                this.getElement().find('.elMapping-Map-renderer-right').setStyle("zIndex", 9); // was z-index: 1000
            }
        },

        publishEventsForDataStoreLoaded: function (data) {
            for (var whichTab in types) {
                if (whichTab === 'eod') {
                    break;
                }
                types[whichTab].collectionFromController(data);
            }
            this.getEventBus().publish('updateTabs');
            this.getEventBus().publish('wait-end');
            this.getEventBus().unsubscribe('stopProgress');
            this.getEventBus().publish('dataStoreLoaded', data);
        },

        /**
         * Reload and display a single (or a set of) datastore (as specified).
         * @method refreshDatastore
         * @param {type} ds
         */
        refreshDatastore: function (ds) {
            // The datastore to be reloaded. It uses the following structure:
            //     cur:   The index of the current datastore to be loaded (0 initially)
            //     names: The array with the names of the datastores to be loaded
            //     show:  The name of the datastore whose table has to be opened on the GUI
            //     id:    The index of the record of the datastore to be detailed (0 if no details required)

            if (ds.cur === undefined) {
                ds.cur = 0;
            } else {
                ds.cur = ds.cur + 1;
            }
            if (ds.cur >= ds.names.length) {
                if (ds.show) {
                    if (types[ds.show] && ds.names.indexOf(ds.show) === -1) {
                        types[ds.show].collection._collection.reset();
                        types[ds.show].collectionFromController(datastore.topologyDataCache); //TODO - Maybe to be changed for real behavior.
                    }
                    var id = ds.id || 0;
                    this.mainRegion.showTable(ds.show);
                    this.selectInput(ds.show, [id]);
                }
                if (this.map) {
                    this.map.reloadMap();
                } else {
                    this.mainRegion.reloadMap();
                }
                this.getEventBus().publish('updateTabs');
                this.getEventBus().publish('wait-end');
                return;
            }
            this.refreshDatastore(ds);
        },

        /**
         * Read (again) the whole datastore.
         * @method refreshAll
         */
        refreshAll: function () {
            // For Now does F5 (reload/refresh the page)
            location.reload(true);
        },

        /**
         * Show Savings Panel.
         * @method showHideSavingsPanel
         */
        showSavingsPanel: function () {
            if (this.isRightPanelOpen && this.actionValueRightPanel === FC.savingsLabel) {
                this.getEventBus().publish('layouts:closerightpanel');
            } else {
                this.createSavingRegion();
            }
        },

        createSavingRegion: function () {
            this.savingsRegion = new Savings({
                context: this.getContext(),
                showchart: this.mainRegion.showCharts,
                id: this.mainRegion.selectedId,
                rowSelect: this.mainRegion.isRowSelected,
                savingPerctangeValues: this.mainRegion.savingPerctangeValues
            });
            this.savingsRegion.setSelectedId(this.mainRegion.selectedId);

            this.getEventBus().publish('layouts:showpanel', {
                header: dictionary.get("savings"),
                content: this.savingsRegion,
                value: 'savings',
                side: 'right'
            });
        },

        /*
         * Added to draw correctly the graph on the right Panel, cause only when the panel is opened with no selection,
         * and than you select a link/boning monitored the graphs are not shown correctly (so we close and reopen the panel).
         */
        hideAndShowRightPanel: function () {
            if (this.isRightPanelOpen && this.mainRegion.showCharts) {
                this.getEventBus().publish('layouts:closerightpanel');

                if (this.actionValueRightPanel === FC.detailsLabel) {
                    this.getEventBus().publish('layouts:showpanel', {
                        header: dictionary.get("details"),
                        content: this.rightContentPanel,
                        value: 'details',
                        side: 'right'
                    });
                }

                if (this.actionValueRightPanel === FC.savingsLabel) {
                    this.createSavingRegion();
                }
            }
        },

        /**
         * Draw the main window of the application.
         * @method showMainWindow
         */
        showMainWindow: function () {
            this.topSection = new TopSection({
                context: this.getContext(),
                breadcrumb: this.options.breadcrumb,
                title: this.options.properties.title,
                defaultActions: [
                    [
                        {
                            type: 'button',
                            icon: 'eye',
                            name: dictionary.get("savings"),
                            action: this.showSavingsPanel.bind(this),
                            main: true
                        },
                        {
                            type: 'button',
                            icon: 'refresh',
                            name: dictionary.get("refresh"),
                            action: this.refreshAll.bind(this),
                            main: true
                        }
                    ]
                ]
            });

            this.mainRegion = new Main({
                context: this.getContext(),
                header: dictionary.get('networkMapTitle')
            });

            this.leftContentPanel = new Side({
                context: this.getContext(),
                header: dictionary.get('networkTopology')
            });

            this.rightContentPanel = new Details({
                context: this.getContext(),
                header: dictionary.get('details')
            });

            this.topSection.attachTo(this.getElement());

            this.multiSlidingPanels = new MultiSlidingPanels({
                context: this.getContext(),
                resolutionThreshold: 700,
                resizeable: false, // TODO to be true when graphs will be resizable.
                redrawMode: MultiSlidingPanels.RESIZE_MODE.ON_DROP,
                showLabel: true,
                leftWidth: LEFT_WIDTH,
                rightWidth: RIGHT_WIDTH,
                main: {
                    label: dictionary.get('networkTopologyMap'),
                    content: this.mainRegion
                },
                left: [{
                    name: dictionary.get('networkTopology'),
                    value: 'networkTopology',
                    icon: 'topology',
                    label: dictionary.get('networkTopology'),
                    content: this.leftContentPanel,
                    expanded: true
                }],
                right: [{
                    name: dictionary.get('details'),
                    value: 'details',
                    icon: 'info',
                    label: dictionary.get('details'),
                    content: this.rightContentPanel
                }]
            });
            this.topSection.setContent(this.multiSlidingPanels);

            this.isRightPanelOpen = "";
            this.actionValueRightPanel = "";
            this.getContext().eventBus.subscribe('layouts:rightpanel:beforechange', function (shown, actionValue) {
                this.isRightPanelOpen = shown;
                this.actionValueRightPanel = actionValue;
                this.mainRegion.paramsForRightPanels.isRightPanelOpen = this.isRightPanelOpen;
                this.mainRegion.paramsForRightPanels.actionValueRightPanel = this.actionValueRightPanel;
                // If we open Details panel on the right we call the method to verify if we have to shows charts or not.
                // Methods afterchange are used in Main to adjust widths.
                if (actionValue === FC.detailsLabel && shown) {
                    this.rightContentPanel.setHeaderTitleAndUpdateCharts(this.mainRegion.paramsForRightPanels);
                    if (this.mainRegion.showCharts && this.mainRegion.whichTab !== FC.nodesLabel) {
                        this.rightContentPanel.updateHistogramChart(this.mainRegion.paramsForRightPanels, this.mainRegion.allDataForDetailsCharts);
                    }
                    if (this.savingsRegion) {
                        this.savingsRegion.setShowSavingsChartValue(false);
                    }
                } else {
                    this.rightContentPanel.setShowDetailsChartsValue(false);
                }
            }.bind(this));

            this.getEventBus().subscribe('wait-start', FC.startProgress.bind(this));
            this.getEventBus().subscribe('wait-end', FC.endProgress.bind(this));
            this.getEventBus().subscribe('wait-next', FC.stepProgress.bind(this));
            this.getEventBus().subscribe('tabSelect', this.selectInput.bind(this));
        },

        /**
         * Callback to manage the end of datastore realignment. Fills all the data tables (managed by the types array),
         * updates all the titles on the GUI, reload the map (with updates as needed), display the selected table (if
         * any, or the nodes table otherwise).
         * @method onDatastoreLoaded
         * @param {type} data
         */
        onDatastoreLoaded: function (data) {
            if (this.mainRegion) {
                this.mainRegion.reloadMap();
                this.mainRegion.showTable(FC.nodesLabel);
            }
            this.changeZIndex();
        },

        /**
         * Callback to manage the updates of the datastore.
         * @method onDatastoreUpdated
         * @param {type} tab - the name of the updated table (as in datastore.js)
         * @param {type} justCreated (true if the table has been just created, false otherwise)
         * @param {type} reloadMap (true to trigger a refresh of the graphical map)
         */
        onDatastoreUpdated: function (tab, justCreated, reloadMap) {
            //NOT used for now
            // if (tab === "statusMessages") {
            //     if (this.mainRegion) {
            //         this.mainRegion.showTable("statusMessages");
            //         reloadMap = true; // force redraw of the map to clear highlighted items
            //     }
            // } else { // trick to open the detail panel of a just created element
            if (this.mainRegion && justCreated) {
                this.mainRegion.justCreated[tab] = justCreated;
            }
            // this.readEline();
            this.readTopology(); // Refresh the relevant data table
            // }
            if (reloadMap) {
                if (this.mainRegions) {
                    this.mainRegion.reloadMap();
                }
            }
        },

        /**
         * @method selectInput
         * @param {type} whichTab - The table whose item has to be shown
         * @param {type} ids - The (set of) item to be shown
         * @param {type} selectedTabWithSelectedRow
         */
        selectInput: function (whichTab, ids, selectedTabWithSelectedRow) {
            if (types[whichTab]) {
                var id;
                if (!ids || !ids.length) {
                    this.currentlySelected = {whichTab: '', id: ''};
                    types[whichTab].selectedId = undefined;
                } else {
                    id = ids[0];
                    if (id > types[whichTab].collection._collection.length) {
                        id = 0;
                    }
                    this.currentlySelected = {whichTab: whichTab, id: id};
                    if (!localStorage.getItem('rememberSel') || localStorage.getItem('rememberSel') === 'yes') {
                        types[whichTab].selectedId = id;
                    }
                }
                if (id > 0 && selectedTabWithSelectedRow && selectedTabWithSelectedRow[0].whichTab === whichTab &&
                    selectedTabWithSelectedRow[0].selected === true || types[whichTab].selectedId) {
                    this.mainRegion.highlight(whichTab, id, undefined, undefined);
                } else {
                    if (whichTab === FC.nodesLabel) {
                        this.mainRegion.hideCurrentAndExpectedGraph();
                        this.rightContentPanel.setShowDetailsChartsValue(false);
                        if (this.savingsRegion) {
                            this.savingsRegion.setShowSavingsChartValue(false);
                        }
                    }
                }
            }
        }

    });

});