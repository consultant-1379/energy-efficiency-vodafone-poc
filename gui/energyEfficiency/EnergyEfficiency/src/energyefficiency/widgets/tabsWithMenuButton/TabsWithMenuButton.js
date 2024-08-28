define([
    'jscore/core',
    'widgets/Tabs',
    'widgets/ContextMenu'
], function (core, Tabs, ContextMenu) {

    /**
     * A 'Tabs' widget with a 'ContextMenu' menu button (at the end of the tabs bar). The widget height is settable.
     *
     * @options
     * {Array<Object>} tabs - an array containing objects that may be used to create tabs. Options contain the following:
     * title (string), content (string/widget)
     * {boolean} showAddButton - Optional, if true, the + button is displayed. Default false.
     * {Array<Object>} menu - the context menu entries
     * {Number} height - the height of the widget
     *
     * @ui_events tabselect ({String} tabTitle, {Number} tabIndex) - Triggers when a tab has been clicked/selected.
     * @class widgets/TabsWithMenuButtons
     * @constructor
     * @param {Object} options
     * @extends widgets/Tabs
     */

    return Tabs.extend({

        onViewReady: function () {
            Tabs.prototype.onViewReady.call(this);
            if (this.options.menu) {
                var contextMenu = new ContextMenu(this.options.menu);
                var buttonDiv = new core.Element();
                contextMenu.attachTo(buttonDiv);
                buttonDiv.setStyle("padding", "7px 8px");
                var menuDiv = new core.Element();
                menuDiv.setStyle({
                    "flex-grow": "0",
                    "flex-shrink": "0",
                    "width": "28px",
                    "height": "27px",
                    "border": "1px solid #cccccc",
                    "border-width": "1px 1px 0 1px",
                    "background-image": "linear-gradient(180deg, #ffffff 0%, #e6e6e6 100%)",
                    "border-radius": "3px 3px 0 0"
                });
                menuDiv.append(buttonDiv);
                var top = this.view.getElement().find(".ebTabs-top");
                top.append(menuDiv);

            }
            if (this.options.height) {
                this.setHeight(this.options.height);
            }
        },

        /**
         * Set the height of the tabs widget
         *
         * @param {Number} height
         * @method setHeight
         */
        setHeight: function (height) {
            var netHeight = height - 30; // button bar is 30 high
            var contentDiv = this.view.getElement().find(".ebTabs-contentDiv");
            contentDiv.setStyle('height', netHeight + 'px');
        }

    });

});
