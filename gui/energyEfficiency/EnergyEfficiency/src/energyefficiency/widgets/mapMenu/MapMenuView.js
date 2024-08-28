define([
    'jscore/core',
    'text!./MapMenu.html',
    'styles!./MapMenu.less'
], function (core, template, style) {

    return core.View.extend({

        getTemplate: function () {
            return template;
        },

        getStyle: function () {
            return style;
        },

        findElement: function (whichTab) {
            return this.getElement().find(".eaEnergyEfficiency-wMapMenu-" + whichTab);
        },

        showMainMenu: function (open) {
            if (open) {
                this.findElement('main-menu').setModifier('open');
            } else {
                this.findElement('main-menu').removeModifier('open');
            }
        },

        showSubMenu: function (title, body) {
            var subMenuTitle = this.findElement('submenu-title');
            subMenuTitle.setText(title);
            var subMenuBody = this.findElement('submenu-body');
            var children = subMenuBody.children();
            for (i = 0; i < children.length; ++i) {
                children[i].remove();
            }
            subMenuBody.append(body);
            this.findElement('submenu').setModifier('open');
        },

        closeSubMenu: function () {
            this.findElement('submenu').removeModifier('open');
        }

    });
});