define([
    'jscore/core',
    'text!./DataTable.html',
    'styles!./DataTable.less'
], function (core, template, style) {

    return core.View.extend({

        getTemplate: function () {
            return template;
        },

        getStyle: function () {
            return style;
        },

        setTitle: function (title) {
            var elt = this.getElement().find('.eaEnergyEfficiency-wDataTable-title');
            if (elt) {
                elt.setText(title);
            }
        },

        addTableSettingsClickHandler: function (fn) {
            var btn = this.getElement().find(".eaEnergyEfficiency-wDataTable-header-tableSettingsButton");
            if (btn) {
                return btn.addEventHandler("click", fn);
            }
        },

        addTableFilterClickHandler: function (fn) {
            var btn = this.getElement().find(".eaEnergyEfficiency-wDataTable-header-tableFilterButton");
            if (btn) {
                return btn.addEventHandler("click", fn);
            }
        },

        getTableDiv: function () {
            return this.getElement().find('.eaEnergyEfficiency-wDataTable-table');
        },

        getPaginationDiv: function () {
            return this.getElement().find('.eaEnergyEfficiency-wDataTable-pagination');
        },

        getFilterBtn: function () {
            return this.getElement().find('.eaEnergyEfficiency-wDataTable-header-tableFilterButton');
        },

        getFilterIcon: function () {
            return this.getElement().find('.eaEnergyEfficiency-wDataTable-header-tableFilterButton .ebIcon');
        },

        getMessageHolder: function () {
            return this.getElement().find('.eaEnergyEfficiency-wDataTable-message');
        },

        disableHeader: function () {
            var header = this.getElement().find('.eaEnergyEfficiency-wDataTable-header');
            if (header) {
                header.setStyle({display: "none"});
            }
        }
    });
});
