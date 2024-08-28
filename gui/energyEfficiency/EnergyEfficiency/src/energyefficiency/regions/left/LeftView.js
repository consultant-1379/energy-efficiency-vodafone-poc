define([
    'jscore/core',
    'text!./Left.html',
    'styles!./Left.less'
], function (core, template, styles) {

    return core.View.extend({

        getTemplate: function () {
            return template;
        },

        getStyle: function () {
            return styles;
        },

        // setHeader: function (text) {
        //     this.getElement().find('.eaEnergyEfficiency-Left-SectionHeading').setText(text);
        // },

        getTree: function () {
            return this.getElement().find('.elLayouts-eaEnergyEfficiency-Left-Content-block');
        }

    });

});