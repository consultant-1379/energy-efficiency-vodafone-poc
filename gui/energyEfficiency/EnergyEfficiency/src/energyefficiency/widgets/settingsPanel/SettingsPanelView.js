define([
    'jscore/core',
    'text!./SettingsPanel.html',
    'styles!./SettingsPanel.less'
], function (core, template, style) {

    return core.View.extend({

        getTemplate: function () {
            return template;
        },

        getStyle: function () {
            return style;
        },

        findElement: function (whichTab) {
            return this.getElement().find(".eaEnergyEfficiency-wSettingsPanel-" + whichTab);
        },

        setResetTitle: function (title) {
            this.getElement().find(".eaEnergyEfficiency-wSettingsPanel-reset-title").setText(title);
        }

    });
});
