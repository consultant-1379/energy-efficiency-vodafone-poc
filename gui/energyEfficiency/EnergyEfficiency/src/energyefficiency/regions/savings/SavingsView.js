define([
    'jscore/core',
    'text!./Savings.html',
    'styles!./Savings.less'
], function (core, template, style) {

    var _prefix = '.eaEnergyEfficiency-rSavings';

    return core.View.extend({

        getTemplate: function () {
            return template;
        },

        getStyle: function () {
            return style;
        },

        getGaugeChart: function () {
            return this.getElement().find(_prefix + '-gauge-chart');
        },

        getDailyGaugeChart: function () {
            return this.getElement().find(_prefix + '-gauge-chart-daily');
        },

        getGaugeChartContent: function () {
            return this.getElement().find(_prefix + '-gauge-chart-content');
        },

        showGaugeCharts: function (show) {
            if (show) {
                this.getGaugeChartContent().setStyle("display", "block");
            } else {
                this.getGaugeChartContent().setStyle("display", "none");
            }
        },

        setGaugeChartHeader: function (text) {
            this.getElement().find(_prefix+'-gauge-chart-header').setText(text);
        },

        setDailyGaugeChartHeader: function (text) {
            this.getElement().find(_prefix+'-gauge-chart-header-daily').setText(text);
        },

        getInfoMessageEl: function() {
            return this.getElement().find(_prefix+'-infoMessage');
        },

        hideInfoMessage: function() {
            this.getInfoMessageEl().setModifier('hidden');
        },

        showInfoMessage: function() {
            this.getInfoMessageEl().removeModifier('hidden');
        },

        getInfoMessageHeaderEl: function() {
            return this.getElement().find(_prefix+'-infoMessageHeader');
        },

        setInfoMessageHeader: function(text) {
            this.getInfoMessageHeaderEl().setText(text);
        },

        getInfoMessageParagraphEl: function() {
            return this.getElement().find(_prefix+'-infoMessageParagraph');
        },

        setInfoMessageParagraph: function(text) {
            this.getInfoMessageParagraphEl().setText(text);
        }
    });

});
