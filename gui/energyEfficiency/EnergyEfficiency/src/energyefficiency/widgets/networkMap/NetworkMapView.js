define([
    'jscore/core',
    'text!./NetworkMap.html',
    'styles!./NetworkMap.less'
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
