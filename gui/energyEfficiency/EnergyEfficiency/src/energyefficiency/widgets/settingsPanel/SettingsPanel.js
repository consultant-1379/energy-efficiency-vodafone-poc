define([
    'jscore/core',
    './SettingsPanelView',
    'tablelib/TableSettings',
    'i18n!energyefficiency/dictionary.json'
], function (core, View, TableSettings, dictionary) {

    /**
     * 'TableSettings' widget with an added *Restore Defaults* button
     *
     * @options
     * {Array} columns - the table columns
     *
     * @ui_events
     * reset - triggered when the *Restore Defaults* button is pressed change ({Array} cols) - triggered when the columns are changed
     *
     * @class widgets/SettingsPanel
     * @extends core.Widget
     */
    return core.Widget.extend({

        View: View,

        /**
         * create the widget
         * @method onViewReady
         * @private
         */
        onViewReady: function () {
            this.createSettings(this.options.columns);
            this.view.setResetTitle(dictionary.get("restoreDefaultsTitle"));
            this.view.findElement('reset').addEventHandler('click', function () {
                this.trigger('reset');
            }, this);
        },

        /**
         * Update/recrate the table settings widget with new columns
         *
         * @param {Array} cols
         * @method update
         */
        update: function (cols) {
            this.settings.detach();
            this.settings.destroy();
            this.createSettings(cols);
        },

        /**
         * Return the current column settings
         *
         * @return {Array}
         * @method getUpdatedColumns
         */
        getUpdatedColumns: function () {
            return this.settings.getUpdatedColumns();
        },

        /**
         * (re)create the TableSettings widget
         *
         * @params {Array} cols
         * @method createSettings
         * @private
         */
        createSettings: function (cols) {
            this.settings = new TableSettings({columns: cols});
            this.settings.attachTo(this.view.findElement('settings'));
            this.settings.addEventHandler('change', function (cols) {
                this.trigger('change', cols);
            }, this);
        }
    });

});