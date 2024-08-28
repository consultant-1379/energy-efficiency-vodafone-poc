define([
    'jscore/core',
    'text!./Details.html',
    'styles!./Details.less'
], function (core, template, styles) {

    var rootClassName = '.eaEnergyEfficiency-Details';

    return core.View.extend({

        getTemplate: function () {
            return template;
        },

        getStyle: function () {
            return styles;
        },

        setHeader: function (text) {
            this.getElement().find(rootClassName+'-SectionHeading').setText(text);
        },

        getDetailsContent: function () {
            return this.getElement().find(rootClassName+'-Content');
        },

        getFirstContentDetails: function () {
            return this.getElement().find(rootClassName+'-Content-block-first');
        },

        getSecondContentDetails: function () {
            return this.getElement().find(rootClassName+'-Content-block-second');
        },

        setRadialChartHeader: function (text) {
            this.getElement().find(rootClassName+'-Content-block-second-header').setText(text);
        },

        setColumnChartHeader: function (text) {
            this.getElement().find(rootClassName+'-Content-block-first-header').setText(text);
        },

        showCharts: function (show) {
            if (show) {
                this.getDetailsContent().setStyle("display", "block");
            } else {
                this.getDetailsContent().setStyle("display", "none");
            }
        },

        getInfoMessageEl: function() {
            return this.getElement().find(rootClassName+'-infoMessage');
        },

        hideInfoMessage: function() {
            this.getInfoMessageEl().setModifier('hidden');
        },

        showInfoMessage: function() {
            this.getInfoMessageEl().removeModifier('hidden');
        },

        getInfoMessageHeaderEl: function() {
            return this.getElement().find(rootClassName+'-infoMessageHeader');
        },

        setInfoMessageHeader: function(text) {
            this.getInfoMessageHeaderEl().setText(text);
        },

        getInfoMessageParagraphEl: function() {
            return this.getElement().find(rootClassName+'-infoMessageParagraph');
        },

        setInfoMessageParagraph: function(text) {
            this.getInfoMessageParagraphEl().setText(text);
        }

    });

});
