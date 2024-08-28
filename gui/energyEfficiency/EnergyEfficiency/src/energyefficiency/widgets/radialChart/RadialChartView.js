define([
    'jscore/core',
    'text!./RadialChart.html',
    'styles!./RadialChart.less'
], function (core, template, style) {

    return core.View.extend({

        getTemplate: function () {
            return template;
        },

        getStyle: function () {
            return style;
        }


    });
});
