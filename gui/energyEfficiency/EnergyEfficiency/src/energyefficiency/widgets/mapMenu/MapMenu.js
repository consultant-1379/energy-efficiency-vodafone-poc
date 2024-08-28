define([
    'jscore/core',
    './MapMenuView',
    'jscore/ext/dom',
    'mapping/Leaflet',
    'i18n!energyefficiency/dictionary.json'
], function (core, View, dom, L, dictionary) {

    var submenuCallback = function (item, context) {
        return function (e) {
            e.originalEvent.stopPropagation();
            e.originalEvent.preventDefault();
            context.menuSelected(item);
        };
    };

    /**
     * 'MapMenu' is a Leaflet control that shows a menu button, opening to a drop down menu, with items that can execute 
     * a single action or replace the menu with a sub-menu with multiple steps to perform.
     * 'MapMenu' is intended to be generic, but implements only the features required by 'NetworkMap'.
     * Is an 'L.Control', a container for a single 'MapMenuWidget', whichTab extends 'core.Widget', and contains the functionality.
     * Possible menu items are:
     * - **Single action**: {'title': *title*, 'action': *callback* } registers a single callback, without a submenu.
     * - **Separator**: '{type: 'separator'}' generates an empty line in the top level menu.
     * - **Radio buttons**: {'title': *title*, 'radio: [ { description: ' *desc1*, 'value': *value1* },... ], getter': *getter_callback*,
     *   'setter': *setter_ballback* } registers a group of radio buttons. The setter callback gets called with the selected value
     *   when the user selects an entry; the getter callback, if defined, should return the value of the entry to be shown as selected.
     * - **Submenu with steps**: {'title': *title*, 'steps': [ *array of steps* ], 'cleanup': *cleanup_callback* } to change to a submenu
     *   with a list of steps.
     * The 'cleanup' callback is called when the menu is closed or cancel is pressed.
     * Each step should be an object with a 'title' and an extended 'description', plus 'start', 'click', 'next' and/or 
     * 'done' callbacks. Any 'start' callback is called when the step becomes active. A 'click' callback requires a 'target', either a
     * leaflet layer or the leaflet map itself, to install a click handler on. Clicking will then call the callback, with the
     * leaflet event, and move on to the next step.
     * 'next' adds a "Next" button, clicking on whichTab will call the 'next' callback and move to the next step; 'done' 
     * will add a "Done" button, clicking on whichTab will call the 'done' callback and close the menu.
     * With 'next' or 'done' the 'MapMenu' itself does not register any handlers for click or other events - the 'start', 'next' and
     * 'done' callbacks should register and unregister those if required.
     * item titles must be unique.
     *
     * @class widgets/MapMenu
     * @constructor
     * @param {Object} options
     * @extends L.Control
     */
    var MapMenuWidget = core.Widget.extend({

        View: View,

        onViewReady: function () {
            var menuDiv = this.view.findElement('main-menu');
            for (var i = 0; i < this.options.items.length; ++i) {
                var item = this.options.items[i];
                var itemDiv = new core.Element();
                itemDiv.setAttribute('class', 'eaEnergyEfficiency-wMapMenu-menu-item');
                if (item.type !== 'separator') {
                    itemDiv.setText(item.title);
                    itemDiv.addEventHandler('click', submenuCallback(item, this));
                }
                menuDiv.append(itemDiv);
            }
            this.submenu = {};

            this.view.findElement('menubutton').addEventHandler('click', this.toggleMenu, this);
            this.view.findElement('cancel').addEventHandler('click', this.cancel, this);

            this.view.findElement('menubutton').setAttribute('title', dictionary.get("mapMenu"));
        },

        /**
         * Callback to select / switch to a submenu (or execute the action).
         *
         * @method menuSelected
         * @private
         */
        menuSelected: function (item) {
            var i, submenu;
            this.view.showMainMenu(false);
            this.currentItem = item;
            this.stepDivs = [];
            this.stepIndex = 0;
            if (item.action) {
                this.opened = false; // next toggle: open main
                item.action.call(null, item); // pass in item
            } else if (item.radio) {
                var selected;
                // recreate every time, so we can select the wanted entry. Should get cleaned up by onRadioClick.
                if (this.submenu[item.title]) {
                    this.submenu[item.title].remove();
                }
                if (item.getter) {
                    selected = item.getter.call(this);
                }
                submenu = new core.Element();
                var form = new core.Element('form');
                submenu.append(form);
                for (i = 0; i < item.radio.length; ++i) {
                    var buttonDiv = new core.Element();
                    var button = new core.Element('input');
                    button.setAttribute('type', 'radio');
                    if (item.radio[i].description === selected) {
                        button.setProperty('checked', true);
                    }
                    button.setAttribute('value', item.radio[i].value);
                    buttonDiv.append(button);
                    var label = new core.Element('span');
                    label.setText(item.radio[i].description);
                    buttonDiv.append(label);
                    form.append(buttonDiv);
                    button.addEventHandler('click', this.onRadioClick, this);
                }
                this.submenu[item.title] = submenu;
                this.view.showSubMenu(item.title, submenu);
            } else { // shoud have steps
                submenu = this.submenu[item.title];
                if (!submenu) {
                    /** create body */
                    submenu = new core.Element();
                    // require steps
                    for (i = 0; i < item.steps.length; ++i) {
                        var step = item.steps[i];
                        var stepDiv = new core.Element();
                        stepDiv.setAttribute('class', 'eaEnergyEfficiency-wMapMenu-step');
                        this.stepDivs.push(stepDiv);
                        if (step.title) {
                            var stepTitleDiv = new core.Element();
                            stepTitleDiv.setAttribute('class', 'eaEnergyEfficiency-wMapMenu-step-title');
                            stepTitleDiv.setText(step.title);
                            stepDiv.append(stepTitleDiv);
                        }
                        if (step.description) {
                            var stepDescDiv = new core.Element();
                            stepDescDiv.setAttribute('class', 'eaEnergyEfficiency-wMapMenu-step-description');
                            stepDescDiv.setText(step.description);
                            stepDiv.append(stepDescDiv);
                        }
                        if (step.next || step.done) {
                            var stepButtonDiv = new core.Element();
                            stepButtonDiv.setText(step.next ? 'Next' : 'Done');
                            stepButtonDiv.setAttribute('class', 'eaEnergyEfficiency-wMapMenu-step-button');
                            stepDiv.append(stepButtonDiv);
                            step.target = stepButtonDiv;
                        }
                        submenu.append(stepDiv);
                    }
                }
                this.view.showSubMenu(item.title, submenu);
                this.activateStep(0);
            }
        },

        /**
         * Open the main menu or close the menu.
         * @method toggleMenu
         * @private
         */
        toggleMenu: function () {
            this.opened = !this.opened;
            this.view.closeSubMenu(); // always
            this.view.showMainMenu(this.opened);
            // remove click handler from current step -- also on 'cancel'
            this.removeClickHandler();
            if (!this.opened && this.currentItem) {
                if (this.currentItem.cleanup) {
                    this.currentItem.cleanup.call();
                }
                this.currentItem = undefined;
            }
        },

        /**
         * Close the menu if it was opened
         *
         *  @method close
         */
        close: function () {
            if (this.opened) {
                this.toggleMenu();
            }
        },

        /**
         * handler for the 'Cancel button'
         * @method cancel
         * @private
         */
        cancel: function () {
            if (this.options.cancel) {
                this.options.cancel.call();
            }
            this.toggleMenu();
        },

        /**
         * Method to activate a step. If the indicated step does not exist, close the menu; otherwise, highlight the
         * selected step, call the start callback if present, and register a click handler for the 'click' target if
         * a 'click' callback is registered, or for the "Next" or "Done" button if either of those is registered.
         *
         * @param {index} index - Integer
         * @method activateStep
         * @private
         */
        activateStep: function (index) {
            if (!this.stepDivs.length) return;
            this.stepDivs[this.stepIndex].removeModifier('active');
            var step = this.currentStep = this.currentItem.steps[index];
            if (!step) {
                this.toggleMenu(); // done
                return;
            }
            this.stepIndex = index;
            this.stepDivs[this.stepIndex].setModifier('active');
            if (step.start) { // start handler? => call
                step.start.call();
            }
            if (step.click && step.target) { // leaflet object => 'on'
                this.clickTarget = step.target;
                step.target.on('click', this.onStepClick, this);
            } else if (step.target && (step.next || step.done)) {
                // jscore widget => addEventHandler
                this.buttonHandler = step.target.addEventHandler('click', this.onStepClick, this);
            }
        },

        /**
         * Callback for click on 'click' targets or "Next" or "Done" buttons. Removes the click handler, calls the
         * respective callback and activates the next step (if any).
         *
         * @method onStepClick
         * @private
         */
        onStepClick: function (e) {
            e.originalEvent.stopPropagation();
            e.originalEvent.preventDefault();
            var index = this.stepIndex;
            var step = this.currentItem.steps[index];

            this.removeClickHandler();
            if (step.click) { // should be the case
                step.click.call(null, e);
            } else if (step.next) {
                step.next.call(null, e);
            } else if (step.done) {
                step.done.call(null, e);
            }
            this.activateStep(index + 1);
        },

        /**
         * Remove the onStepClick event callback.
         *
         * @method removeClickHandler
         * @private
         */
        removeClickHandler: function () {
            if (this.clickTarget) { // jscore
                this.clickTarget.off('click', this.onStepClick, this);
                this.clickTarget = undefined;
            }
            if (this.buttonHandler) { // jscore widget/handler
                dom.removeEventHandler(this.buttonHandler);
                this.buttonHandler = undefined;
            }
        },

        /**
         * Handler for a radio button click. calls the setter callback, closes the menu and removes the radio buttons.
         *
         * @method onRadioClick
         * @private
         */
        onRadioClick: function (e) {
            var setter = this.currentItem.setter;
            if (setter) {
                setter.call(null, e.originalEvent.target.value);
            }
            var title = this.currentItem.title;
            this.toggleMenu();
            if (this.submenu[title]) {
                this.submenu[title].remove();
                delete this.submenu[title];
            }
        }
    });

    var MapMenu = L.Control.extend({
        options: {
            position: 'topright',
            items: []
        },

        /**
         * initializes the 'L.Control', and creates the 'MapMenuWidget'
         *
         * @method initialize
         * @private
         */
        initialize: function (options) {
            L.Util.setOptions(this, options);
        },

        /**
         * onAdd for the 'L.Control': create and append the 'MapMenuWidget'.
         *
         * @method onAdd
         */
        onAdd: function (map) {
            // create the control container with a particular class name
            var menuControl = L.DomUtil.create('div');
            var widget = this.widget = new MapMenuWidget(this.options);
            // get event bus of the MapMenuWidget & forward
            menuControl.appendChild(widget.getElement().getNative());

            return menuControl;
        },

        /**
         * Close the menu if it was opened
         *
         *  @method close
         */
        close: function () {
            this.widget.close();
        }

    });

    return MapMenu;

});