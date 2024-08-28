define([
    'jscore/core',
    './DataTableView',
    'tablelib/Table',
    'tablelib/TableSettings',
    'tablelib/plugins/QuickFilter',
    'tablelib/plugins/ResizableHeader',
    'tablelib/plugins/SortableHeader',
    'tablelib/plugins/VirtualScrolling',
    'tablelib/plugins/VirtualSelection',
    '../../common'
], function (core, View, Table, TableSettings, QuickFilter, ResizableHeader, SortableHeader, VirtualScrolling,
             VirtualSelection, FC) {

    /**
     * 'DataTable' displays one of the 'types/types'. It is implemented using 'tablelib/Table', with 'ResizableHeader',
     * 'SortableHeader', 'VirtualScrolling' and 'VirtualSelection' plugins. Even though it does implement virtual
     * scrolling, it does assume that all of the collection has been loaded. Event handlers are registered on the
     * collection so that a change in the collection will make the table redisplay itself.
     * It seems that with table width changes due to opening and closing side bars, with auto-width columns those can
     * get stuck at a narrow width (if the table was first drawn with sidebars open). 'kickColumns()' is a work-around
     * against that: it checks whether the table should become wider, and if so forces its width.
     * @options
     * {Type} type - the type to display. Used for 'type.whichTab', and for a possible 'compare' attribute on the attribute options.
     * {mvp.Collection} collection - the collection to display. The collection's models must implement 'displayAttribute()'.
     * {array} columns - the columns to display. An invisible 'id' columns is always added, so that 'VirtualSelection' can find the id.
     * @ui_events
     * selected ({String} whichTab, {Array} ids) - triggered when a row is selected. 'whichTab' is 'type.whichTab', ids contains.
     * the id attribute of the selected model. columnresize ({Object} col) triggered when a column is resized.
     * @class widgets/DataTable
     * @extends core.Widget
     */
    return core.Widget.extend({

        View: View,

        /**
         * Initialize.
         * @method init
         * @param options
         * @private
         */
        init: function (options) {
            for (var i = 0; i < options.columns.length; ++i) {
                options.columns[i].resizable = true;
                options.columns[i].sortable = true;
                options.columns[i].filter = {
                    type: 'text'
                };
            }
            this.columnsPlusId = options.columns.slice(0);
            this.columnsPlusId.push({attribute: "id", visible: false});
            this.options = options;
            this.fillSortedData();
            this.options.collection.addEventHandler('change', this.updateCollection.bind(this));
            this.options.collection.addEventHandler('add', this.updateCollection.bind(this));
            this.options.collection.addEventHandler('remove', this.updateCollection.bind(this));
        },

        getTableSettings: function () {
            return this.tableSettings;
        },

        onViewReady: function () {
            if (this.options.type) {
                this.view.setTitle(this.options.type.title);
            }
        },

        /**
         * Create the table; record its width.
         * @method onAttach
         * @private
         */
        onAttach: function () {
            this.createTable();
            this.table.attachTo(this.getElement()); //view.getTableDiv());
            this.maxWidthSeen = this.view.getElement().getProperty("offsetWidth");
            if (this.options.noHeader) {
                this.view.disableHeader();
            }
            // this.options.rowRenderer(this.options.columnRenderers, this.columnsPlusId, this.table.getRows(), this.sortedData);
        },

        onTableFilter: function (filters) {
            this.fillSortedData();
            var filterArr = preProcessFiltersData(filters);
            this.filterTable(filterArr);
            var filtersApplied = areFiltersApplied(filters);
            if (filtersApplied) {
                this.getElement().setModifier('filtered');
            } else {
                this.getElement().removeModifier('filtered');
            }
            this.table.setData(this.sortedData);
            this.table.reload();
            this.table.redraw();
            // this.options.rowRenderer(this.options.columnRenderers, this.columnsPlusId, this.table.getRows(), this.sortedData);
        },

        /**
         * Reload the table data from the Collection
         * @method fillSortedData
         * @private
         */
        fillSortedData: function () {
            var sortedData = [];
            var cols = this.columnsPlusId;
            this.options.collection.each(function (model) {
                var item = {};
                for (var i = 0; i < cols.length; ++i) {
                    item[cols[i].attribute] = model.displayAttribute(cols[i].attribute);
                }
                sortedData.push(item);
            });
            this.sortedData = sortedData;
        },

        /**
         * Create the table
         * @method createTable
         * @private
         */
        createTable: function () {
            this.table = new Table({
                modifiers: [{
                    name: "striped"
                }],
                columns: this.columnsPlusId,
                tooltips: true,
                plugins: [
                    new ResizableHeader(),
                    new SortableHeader(),
                    new QuickFilter(),
                    new VirtualScrolling({
                        totalRows: this.sortedData.length,
                        getData: this.getData.bind(this)
                    }),
                    new VirtualSelection({
                        checkboxes: false,
                        selectableRows: true,
                        multiselect: false,
                        getIds: this.getIds.bind(this),
                        getAllIds: this.getAllIds.bind(this)
                    })
                ]
            });

            this.tableSettings = new TableSettings({
                columns: this.options.columns // without id column
            }); // no pinColumns

            // Listen for the sort event
            this.table.addEventHandler('sort', this.sortTable.bind(this));

            // forward idselectend as 'selected'
            this.table.addEventHandler('idselectend', function (ids) {
                this.selectedIds = ids;
                this.trigger('selected', this.options.type.whichTab, ids);
            }.bind(this));

            this.table.addEventHandler('rowselect', function (row, select) {
                if (select) {
                    this.selectedRow = row;
                    this.selectedId = row.getData().id;
                } else {
                    this.selectedId = undefined;
                    this.selectedRow = undefined;
                }
            }.bind(this));

            this.table.addEventHandler('filter:change', this.onTableFilter.bind(this));
            this.initializeFilterEvents();
        },

        getSelectedRow: function () {
            return this.selectedRow;
        },

        getSelectedId: function () {
            return this.selectedId;
        },

        getSelectedIds: function () {
            return this.selectedIds;
        },

        // Automatic selection of row specified by jc.
        autoSelectRow: function (jc) {
            if (!jc) {
                return;
            }
            var rows = this.table.getRows();
            for (var i = 0; i < rows.length; i++) {
                var data = rows[i].getData();
                if (data[jc.attr] && (data[jc.attr] === jc.value)) {
                    this.table.trigger('rowselect', rows[i], true);
                    break;
                }
            }
        },

        //// Not used for now
        // addRow: function (data, index) {
        //     if (index === undefined) {
        //         this.options.data.push(data);
        //     } else {
        //         this.options.data.splice(index, data);
        //     }
        //     this.table.setTotalRows(this.options.data.length);
        //     this.table.reload();
        // },

        /**
         * Callback for VirtualScrolling.
         * @method getData
         * @param index
         * @param length
         * @param callback
         * @private
         */
        getData: function (index, length, callback) {
            this.curslice = this.sortedData.slice(index, index + length);
            callback(this.curslice);
            this.options.rowRenderer(this.options.columnRenderers, this.columnsPlusId, this.table.getRows(), this.curslice);
        },

        addTableSettingsClickHandler: function (fn) {
            return this.view.addTableSettingsClickHandler(fn);
        },

        addTableFilterClickHandler: function (fn) {
            return this.view.addTableFilterClickHandler(fn);
        },

        /**
         * Helper function: return the array index in sortedData of the element with the given key.
         * @param {String} key
         * @return {int}
         * @method tableIndexByKey
         * @private
         */
        tableIndexByKey: function (key) {
            for (var i = 0; i < this.sortedData.length; ++i) {
                if (this.sortedData[i].id === key) {
                    return i;
                }
            }
            return -1;
        },

        /**
         * Redraw the table. Should be called when the table has been resized or when the column settings have been changed.
         * @method redraw
         */
        redraw: function () {
            this.currentHeight = this.view.getElement().getProperty('offsetHeight');
            this.table.redraw();
            this.heightCheckCount = 4;
            window.requestAnimationFrame(this.recheckHeight.bind(this));
        },

        /**
         * 'redraw()' helper function. Sometimes, when redrawing after a height change, the table still sees its old height.
         * So check for 4 animation frames after that to see whether the height changes, and if so, call 'redraw()' again.
         * @method recheckHeight
         * @private
         */
        recheckHeight: function () {
            var newHeight = this.view.getElement().getProperty('offsetHeight');
            --this.heightCheckCount;
            if (newHeight !== this.currentHeight) {
                this.redraw(); // redraw and start again
            } else if (this.heightCheckCount) { // check again a bit later
                window.requestAnimationFrame(this.recheckHeight.bind(this));
            }
        },

        /**
         * Sort the table, callback for the 'sort' table event. If the attribute has a 'compare' option
         * (whichTab should be a function) that is used, otherwise the comparision is done with '<'.
         * @param {String} sortMode
         * @param {String} sortAttr
         * @method sortTable
         * @private
         */
        sortTable: function (sortMode, sortAttr) {
            // Set the new sort options
            var modeFac = sortMode === 'asc' ? 1 : -1;
            // keep first element first (or at least: in view)
            var firstKey = this.sortedData[this.table.getVirtualScrollBar().getPosition(0)].id;
            // for now: sort blindly set scroll in the fake div to the top
            var cmpFn = this.options.type.attributes[sortAttr].compare ||
                function (a, b) {
                    return a < b ? -1 : b < a ? 1 : 0;
                };

            this.sortedData.sort(function (a, b) {
                return modeFac * cmpFn(a[sortAttr], b[sortAttr]);
            });

            var newPosition = this.tableIndexByKey(firstKey);
            if (newPosition >= 0) {
                this.table.getVirtualScrollBar().setPosition(newPosition);
            }
            this.table.reload();
        },

        filterTable: function (filterArr) {
            filterArr.forEach(function (filter) {
                var attr = filter.name;
                var filterValue = filter.value;
                var type = 'string';
                if (filterValue === undefined || filterValue === '') {
                    return;
                }
                // Reduce the data set by applying the successive filters.
                this.sortedData = this.sortedData.filter(function (item) {
                    var value = item[attr];
                    switch (type) {
                        case 'string':
                            return compareString(filterValue, value);
                        case 'date':
                            return compareDate(filterValue, value);
                        case 'boolean':
                            return compareBoolean(filterValue, value);
                        case 'array-of-string':
                            return isArrayItemIsInString(filterValue, value);
                    }
                }.bind(this));
            }.bind(this));

//            window.app.getEventBus().publish('updateTabs', this.options.type.whichTab, this.sortedData.length);
        },

        initializeFilterEvents: function () {
            this.filtersEnabled = false;
            if (this.view.getFilterBtn()) {
                this.view.getFilterBtn().addEventHandler('click', this.toggleFilterRow.bind(this));
            }
        },

        toggleFilterRow: function (enable) {
            var filterIcon = this.view.getFilterIcon();
            if (enable === undefined) {
                this.filtersEnabled = !this.filtersEnabled;
            } else {
                this.filtersEnabled = enable;
            }
            if (!this.filtersEnabled) {
                this.resetFilters();
            }
            if (filterIcon) {
                filterIcon.removeModifier(this.filtersEnabled ? 'filterOff' : 'filterOn', 'ebIcon');
                filterIcon.setModifier(this.filtersEnabled ? 'filterOn' : 'filterOff', '', 'ebIcon');
            }
            this.table[this.filtersEnabled ? 'showFilter' : 'hideFilter']();
        },

        resetFilters: function () {
            var filters = {};
            for (var i = 0; i < this.options.columns.length; ++i) {
                if (filters[this.options.columns[i].attribute]) {
                    filters[this.options.columns[i].attribute] = '';
                }
                this.options.columns[i].filter = {
                    type: 'text',
                    options: {
                        submitOn: 'input'
                    },
                    value: ''
                };
            }
            if (this.table) {
                this.fillSortedData();
                this.table.setFilters(filters, false);
                var filterCells = this.view.getElement().findAll('.elTablelib-QuickFilter-text') || [];
                for (var j = 0; j < filterCells.length; ++j) {
                    filterCells[j].setValue('');
                }
            }
        },

        /**
         * Callback for the VirtualSelection plugin.
         * @method getIds
         * @param id1
         * @param id2
         * @param callback
         * @param error
         * @private
         */
        getIds: function (id1, id2, callback, error) {
            var idx1 = this.tableIndexByKey(id1);
            var idx2 = this.tableIndexByKey(id2);
            var ids = [];
            for (var i = idx1; i <= idx2; ++i) {
                ids.push(this.sortedData[i].id);
            }
            callback(ids);
        },

        /**
         * Callback for VirtualSelection plugin.
         * @method getAllIds
         * @param callback
         * @param error
         * @private
         */
        getAllIds: function (callback, error) {
            var ids = [];
            for (var i = 0; i < this.sortedData[i].length; ++i) {
                ids.push(this.sortedData[i].id);
            }
            return ids;
        },

        /**
         * Callback for collection (change/add/remove) events. To avoid update storms (in particular when loading a new
         * file) the actual table reloading is done inside a 'requestAnimationFrame' (and only once per frame).
         * @method updateCollection
         * @private
         */
        updateCollection: function () {
            if (!this.updateRequested) { // only once per frame
                this.updateRequested = window.requestAnimationFrame(this.reloadTable.bind(this));
            }
        },

        /**
         * Reload the table, after a collection change
         * @method reloadTable
         * @private
         */
        reloadTable: function () {
            this.updateRequested = null;
            var currentCount = this.sortedData.length;
            this.fillSortedData();
            if (this.sortedData.length !== currentCount) {
                this.table.setTotalRows(this.sortedData.length);
            }
            this.table.reload();
        },

        /**
         * Check whether the table should become wider. With opening and closing sidebars, and auto-width columns,a table
         * first created with sidebars open would get stuck at a narrow width: 'kickColumns()' checkes whether the new
         * width *width* is higher than the maximum width seen, and if so, resizes the actual table to that width (and its wrapper to 100%).
         * @method kickColumns
         * @param {Number} width
         */
        kickColumns: function (width) {
            var newWidth = this.view.getElement().getProperty("offsetWidth");
            var dWidth = Math.min(width, newWidth > this.maxWidthSeen);
            if (dWidth > 0) {
                this.maxWidthSeen = newWidth;
                var tableWidth = this.table.view.getElement().getProperty("offsetWidth") + dWidth;
                this.table.setWidth(tableWidth + 'px');
                this.table.view.getWrapper().setStyle('width', '100%');
                // make that fit as well.
                this.maxWidthSeen = newWidth;
            }
        },

        /**
         * Unselect any selected row(s)
         * @method unselect
         */
        unselect: function () {
            this.table.unselectAllIds();
        }

    });

    /**
     * Returns true is at least one value is not the default value
     * @param filters
     * @returns {Boolean}
     */
    function areFiltersApplied(filters) {
        return Object.keys(filters).some(function (key) {
            return filters[key] !== '';
        });
    }

    function preProcessFiltersData(filters) {
        return Object.keys(filters).map(function (key) {
            var value = filters[key];
            return {
                name: key,
                value: value
            };
        });
    }

    function compareString(filterValue, itemValue) {
        if (!itemValue) {
            return 0;
        }
        return itemValue.toLocaleLowerCase().indexOf(filterValue.toLocaleLowerCase()) !== -1;
    }

    function isArrayItemIsInString(filterValue, itemValue) {
        return filterValue.some(function (d) {
            return itemValue.indexOf(d) !== -1;
        });
    }

    function compareDate(filterValue, itemValue) {
        var input = new Date(filterValue);
        itemValue = new Date(itemValue);
        return input.getDate() === itemValue.getDate() && input.getMonth() === itemValue.getMonth() &&
            input.getFullYear() === itemValue.getFullYear();
    }

    function compareBoolean(filterValue, itemValue) {
        return filterValue === itemValue;
    }

});