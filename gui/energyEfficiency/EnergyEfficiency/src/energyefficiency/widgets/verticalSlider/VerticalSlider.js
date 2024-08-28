define([
    'jscore/core',
    './VerticalSliderView'
], function (core, View) {

    return core.Widget.extend({

        View: View,

        init: function (options) {
            var abar = localStorage.getItem('application-bar') === "yes";
            this.options = options;
            if (!this.options) {
                this.options = {};
            }
            if (!this.options.minTopHeight) {
                this.options.minTopHeight = 0;
            }
            if (!this.options.minBottomHeight) {
                this.options.minBottomHeight = 0;
            }
            if (!this.options.margin) {
                //TODO - Find a better way to calculate margin, better if auto
                // this.options.margin = 100 + (abar ? 100 : 0);
                this.options.margin = 0;
            }
            // and options.topHeight, options.bottomHeight
            if (!this.options.topStretch) {
                this.options.topStretch = 0;
            }
            // fraction of resize that stretches top: 0 => bottom only, 0.5 => top and bottom equal, 1.0 => top only.
            this._eventBus = new core.EventBus();
            // options.onTopChanged: called if top changes.
            // options.onBottomChanged: called if top changes.
            this.topClosed = false;
        },

        onViewReady: function () {
            var options = this.options;
            var topH = options.topHeight || 0; // force numeric;
            var botH = options.bottomHeight || 0;
            if (!topH || !botH) {
                var available = window.innerHeight - options.margin;
                // 6 for the slider
                if (topH) {
                    botH = Math.max(options.minBottomHeight, available - topH);
                } else if (botH) {
                    topH = Math.max(options.minTopHeight, available - botH);
                } else {
                    topH = Math.max(options.minTopHeight, available / 2);
                    botH = Math.max(options.minBottomHeight, available - topH);
                }
            }

            this.setHeights(topH, botH);

            var knob = this.view.getKnobDiv();
            knob.addEventHandler("mousedown", this.onMouseDown.bind(this));
            core.Window.addEventHandler('resize', this.recalculateHeights.bind(this));
        },

        recalculateHeights: function () {
            var options = this.options;
            var available = window.innerHeight - options.margin;
            if (this.topClosed) {
                this.setHeights(0, available);
                return;
            }

            var topH = this._currentTopHeight;
            var botH = this._currentBottomHeight;
            var dHeight = available - (topH + botH);

            topH = topH + options.topStretch * dHeight;
            botH = botH + (1 - options.topStretch) * dHeight;
            if (botH < options.minBottomHeight) {
                botH = options.minBottomHeight;
                topH = Math.max(options.minTopHeight, available - botH);
            } else if (topH < options.minTopHeight) {
                topH = options.minTopHeight;
                botH = Math.max(options.minBottomHeight, available - topH);
            }
            this.setHeights(topH, botH);
        },

        setHeights: function (topH, botH, reopenTop) {
            topH = +topH; // force number
            botH = +botH;

            var newTop = topH && ( this._currentTopHeight !== topH );
            if (reopenTop || newTop) { // so not if closing
                this._currentTopHeight = topH;
                this.view.getTopDiv().setStyle("height", Math.floor(topH) + "px");
            }
            var newBot = this._currentBottomHeight !== botH;
            if (newBot) {
                this._currentBottomHeight = botH;
                this.view.getBottomDiv().setStyle("height", Math.ceil(botH) + "px");
            }
            if (newTop && this.options.onTopChanged) {
                this.options.onTopChanged(Math.floor(topH));
            }
            if (newBot && this.options.onBottomChanged) {
                this.options.onBottomChanged(Math.ceil(botH));
            }

        },

        onMouseDown: function (e) {
            document.addEventListener("mousemove", this.preventDefault, false);
            this._startY = e.originalEvent.pageY;
            this._topH0 = this.view.getTopDiv().getProperty("offsetHeight");
            this._totH0 = this._topH0 + this.view.getBottomDiv().getProperty("offsetHeight");
            this._mouseMoveEvent = core.Element.wrap(document.body).addEventHandler("mousemove", this.onMouseMove.bind(this));
            this._mouseUpEvent = core.Element.wrap(document.body).addEventHandler("mouseup mouseleave", this.onMouseEnd.bind(this));
        },

        onMouseMove: function (e) {
            // var width = Math.max(60, this._startW - (this._startY - e.originalEvent.pageY));
            var topH = Math.max(this.options.minTopHeight, this._topH0 + (e.originalEvent.pageY - this._startY));
            var botH = this._totH0 - topH;
            if (botH < this.options.minBottomHeight) {
                botH = this.options.minBottomHeight;
                topH = this._totH0 - botH;
            }
            this.setHeights(topH, botH);
        },

        onMouseEnd: function () {
            core.Element.wrap(document.body).removeEventHandler(this._mouseUpEvent);
            core.Element.wrap(document.body).removeEventHandler(this._mouseMoveEvent);
            document.removeEventListener("mousemove", this.preventDefault, false);
        },

        preventDefault: function (e) {
            e.preventDefault();
        },

        getTopHeight: function () {
            return this._currentTopHeight; // wanted, maybe not actual
        },

        getBottomHeight: function () {
            return this._currentBottomHeight;
        },

        getTopDiv: function () {
            return this.view.getTopDiv();
        },

        getBottomDiv: function () {
            return this.view.getBottomDiv();
        },

        getEventBus: function () {
            return this._eventBus;
        },

        closeTop: function () {
            this.view.getElement().setModifier('top-closed');
            var available = window.innerHeight - this.options.margin;
            this.setHeights(0, available);
            this.topClosed = true;
        },

        openTop: function () {
            this.view.getElement().removeModifier('top-closed');
            var available = window.innerHeight - this.options.margin;
            this.topClosed = false;
            this.setHeights(this._currentTopHeight, available - this._currentTopHeight, true);
        }

    });

});