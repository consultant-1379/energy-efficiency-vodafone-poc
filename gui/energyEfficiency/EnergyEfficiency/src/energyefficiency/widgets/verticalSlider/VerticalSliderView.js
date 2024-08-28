define([
    'jscore/core',
    'text!./VerticalSlider.html',
    'styles!./VerticalSlider.less'
], function (core, template, style) {

    return core.View.extend({

        getTemplate: function () {
            return template;
        },

        getStyle: function () {
            return style;
        },

        getTopDiv: function () {
            return this.getElement().find('.eaEnergyEfficiency-wVerticalSlider-top');
        },

        getKnobDiv: function () {
            return this.getElement().find('.eaEnergyEfficiency-wVerticalSlider-knob');
        },

        getBottomDiv: function () {
            return this.getElement().find('.eaEnergyEfficiency-wVerticalSlider-bottom');
        }

    });
});
