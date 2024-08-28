define([
    'test/bit/bitPromises',
    'test/resources/cssNamespaces'
], function(promises, css) {

    var TIMEOUT = 3000;

    /**
     * Helper methods for the Main Region
     * Extends bitPromises.js
     */
    return {

        getMainRegion: function() {
            return promises.waitForElementVisible('.eaEnergyEfficiency-rMain', TIMEOUT);
        },

        getActionBarWrapper: function() {
            return promises.waitForElementVisible('.elLayouts-QuickActionBarWrapper', TIMEOUT);
        },

        getTable: function() {
            return promises.waitForElementVisible('.elTablelib-Table-body', TIMEOUT);
        },

        getActionBarButtons: function() {
            return promises.waitForElementVisible('.elLayouts-ActionBarButton', TIMEOUT);
        },

        getTableRows: function() {
            return promises.waitForElementVisible('.elTablelib-Table-body .ebTableRow', TIMEOUT);
        },

        getWidgetTableCheckboxes: function() {
            return promises.waitForElementVisible('.elTablelib-CheckboxCell', TIMEOUT);
        },

        getDeleteDialogBox: function() {
            return promises.waitForElementVisible('.ebDialogBox', TIMEOUT);
        }

    };
});