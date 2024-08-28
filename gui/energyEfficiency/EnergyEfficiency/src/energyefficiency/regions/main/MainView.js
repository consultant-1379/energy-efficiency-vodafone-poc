define([
    'jscore/core',
    'text!./Main.html',
    'styles!./Main.less'
], function (core, template, style) {

    return core.View.extend({

        getTemplate: function () {
            return template;
        },

        getStyle: function () {
            return style;
        },

        getLineChart: function () {
            return this.getElement().find('.eaEnergyEfficiency-rMain-lineChart');
        },

        showLineChart: function (show) {
            if (show) {
                this.getLineChart().setModifier('show');
            } else {
                this.getLineChart().removeModifier('show');
            }
        },

        getLineChartContent: function () {
            return this.getElement().find('.eaEnergyEfficiency-rMain-lineChart-Content');
        },

        getLineChartHeader: function () {
            return this.getElement().find('.eaEnergyEfficiency-rMain-lineChart-header');
        },

        setLineChartHeader: function (text) {
            this.getLineChartHeader().setText(text);
        },

        setHeaderNetworksMap: function (text) {
            this.getElement().find('.eaEnergyEfficiency-rMain-networksMap-header').setText(text);
        }

        // ,
        // getSlidingWindow: function () {
        //     return this.getElement().find('.elChartlib-wMasterDetailLine-slidingWindow');
        // }


    });
});
