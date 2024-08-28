define([
    'test/bit/bitPromises',
    'test/resources/cssNamespaces'
], function (promises, css) {

    var TIMEOUT = 3000;
    var ACTION_BAR_TIMEOUT = 5000;

    /**
     * Helper methods for the Main Region
     * Extends bitPromises.js
     */
    return {

        getActionBarButtons: function () {
            return promises.waitForElementVisible('.elLayouts-ActionBarButton', ACTION_BAR_TIMEOUT);
        },

        getFirstActionBarButton: function () {
            return promises.waitForElementVisible('.elLayouts-ActionBarButton', TIMEOUT, null, 0);
        },

        getSecondActionBarButton: function () {
            return promises.waitForElementVisible('.elLayouts-ActionBarButton', TIMEOUT, null, 1);
        },

        getBreadcrumb: function () {
            return promises.waitForElementVisible('.ebBreadcrumbs', TIMEOUT, null, 0);
        },

        getBreadcrumbLinks: function () {
            return promises.waitForElementVisible('.ebBreadcrumbs-link', TIMEOUT);
        },

        getAppHeading: function () {
            return promises.waitForElementVisible('.elLayouts-TopSection-title', TIMEOUT);
        },

        getDialogBox: function () {
            return promises.waitForElementVisible('.ebDialogBox', TIMEOUT);
        },

        getNotificationContent: function () {
            return promises.waitForElementVisible('.ebNotification-content', TIMEOUT);
        }
   };

});