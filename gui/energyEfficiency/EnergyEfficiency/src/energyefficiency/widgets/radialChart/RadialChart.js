/**
 *   Uses the charting library to draw a radial graph.
 *   @class RadialChart
 */
define([
    'chartlib/base/d3',
    'chartlib/Chart',
    'i18n!energyefficiency/dictionary.json',
    '../../common',
    'jscore/core'
], function (d3, Chart, dictionary, FC, core) {

    var tooltip = [];
    var margin = 10;
    var innerRadiusNumber = 5; // Number of inner radial radius.
    var stacksNumber = 24; // 24 Hours
    // var DAY_INTERVAL = 86400000;  // in Milliseconds

    return Chart.extend({

        init: function (options) {
            this.options = options || {};
            this.setStackNumber(this.options.numberOfSamples ? this.options.numberOfSamples : stacksNumber); // 24 Hours
        },

        /**
         * Chart is ready.
         * @method onChartReady
         */
        onChartReady: function () {
            this.setDisplayTime();

            // core.Window.addEventHandler('resize', this.redraw.bind(this)); //TODO - force redraw to adapt if resize of window
        },

        /**
         * Set the displayTime to show on chart, the day before (24H before).
         * @method setDisplayTime
         */
        setDisplayTime: function () {
            var dateTimeRequest = localStorage.getItem('dateTimeRequest');
            var date = new Date();
            if (dateTimeRequest) {
                date = new Date(Number(dateTimeRequest));
            }
            date.setDate(date.getDate() - 1);
            this.displayTime = date.getTime();
        },

        /**
         * Removes the entire visualisation and recreates it.
         * @method redraw
         */
        redraw: function () {
            this.getD3Element().selectAll('*').remove();
            var width = this.getSize().width;
            this.g = this.getD3Element().append('g').attr('transform', 'translate(' + width / 2 + ',' + width / 2 + ')');
            this.drawBackground();
            this.drawStacks();
            this.drawOutline();
            this.drawTimestamp();
            this.updateStacks();
            this.updateTimestamp();
        },

        /**
         * Stops the animation interval.
         * @method stopAnimation
         */
        stopAnimation: function () {
            this._animInterval.stop();
        },

        /**
         * The chart is drawing assuming a radius range of 0-100. Returns a scaling function to convert from that range in PX.
         * @method getTransformer
         * @return {Function} scalingFunction
         */
        getTransformer: function () {
            var radius = this.getSize().width / 2;
            return d3.scale.linear().domain([0, 100]).range([margin, radius - margin]);
        },

        /**
         * Draws the background night arc.
         * @method drawBackground
         */
        drawBackground: function () {
            var t = this.getTransformer();

            // Day/Night SVG Thing.
            var nightArc = d3.svg.arc()
                .innerRadius(t(40))
                .outerRadius(t(90))
                .startAngle(-(360 / stacksNumber * 4) * (Math.PI / 180))
                .endAngle((360 / stacksNumber * 8) * (Math.PI / 180));

            this.g.append('path')
                .attr('d', nightArc)
                .attr('fill', '#ccc');
        },

        /**
         * Draws the outline for the graph.
         * @method drawOutline
         */
        drawOutline: function () {
            var t = this.getTransformer();
            var i;

            // Draw the grey outlines of the graph on top of the other svgs.
            for (i = 0; i < innerRadiusNumber + 1; i++) {
                var arc = d3.svg.arc()
                    .innerRadius(t(40 + (i * 10)))
                    .outerRadius(t(40 + (i * 10) + 1))
                    .startAngle(0)
                    .endAngle(360 * (Math.PI / 180));

                this.g.append('path')
                    .attr('d', arc)
                    .attr('fill', '#ddd');
            }

            // Draws the radius and numbers around the center.
            for (i = 1; i <= stacksNumber; i++) {
                var angle = (360 / stacksNumber * i);
                var rads = angle * (Math.PI / 180);
                this.g.append('svg:line')
                    .attr('x1', t(40) * Math.cos(rads))
                    .attr('y1', t(40) * Math.sin(rads))
                    .attr('x2', t(90) * Math.cos(rads))
                    .attr('y2', t(90) * Math.sin(rads))
                    .attr('stroke', '#ddd');

                this.g.append('svg:text')
                    .text(i)
                    .attr('fill', '#000')
                    .attr('style', 'text-anchor:middle')
                    .attr('y', t(-103))
                    .attr('transform', 'rotate(' + angle + ', 0, 0)');
            }
        },

        /**
         * Draw the stacks initially.
         * @method drawStacks
         */
        drawStacks: function () {
            var t = this.getTransformer();
            // Pre-draw the 'blocks' and change their dimensions dynamically, overlay the grid on top to give the
            // appearance of separate blocks.
            this.dataArc = d3.svg.arc()
                .innerRadius(t(40))
                .outerRadius(function (d) {
                    return t(40 + 10 * d);
                })
                .startAngle(function (d, i) {
                    return ((360 / (stacksNumber * 2) * i)) * (Math.PI / 180);
                })
                .endAngle(function (d, i) {
                    return (360 / (stacksNumber * 2) * (i + 1)) * (Math.PI / 180);
                });

            var data = [];
            for (var i = 0; i < stacksNumber * 2; i++) {
                data[i] = 0;
            }

            this.g.selectAll('.dataStack')
                .data(data)
                .enter().append('path')
                .attr('class', 'dataStack')
                .attr('d', this.dataArc)
                .attr('fill', function (d, i) {
                    if (i % 2 === 0) {
                        return '#00a9d4';
                    } else {
                        return '#89ba17'; //'#fabb00';
                    }
                });
        },

        /**
         * Draw the timestamp in the center of the graph.
         * @method drawTimestamp
         */
        drawTimestamp: function () {
            var t = this.getTransformer();

            // Draw the date in the center of the chart
            this.g.append('text')
                .attr('class', 'dateTextDay')
                .text('')
                .attr('transform', 'translate(0, ' + t(-10) + ')')
                .attr('style', 'text-anchor:middle');

            this.g.append('text')
                .attr('class', 'dateTextDate')
                .text('')
                .attr('transform', 'translate(0, ' + t(10) + ')')
                .attr('style', 'text-anchor:middle');
        },

        /**
         * Updates the stacks with the latest data.
         * @method updateStacks
         */
        updateStacks: function (data) {
            if (data) {
                this.options.data = data;
            }
            if (this.options.data && this.options.data.realData && this.options.data.predictedData) {
                // Create a blank array of data that we can apply the totals to.
                var dataCount = [];
                for (var i = 0; i < stacksNumber * 2; i++) {
                    dataCount[i] = 0;
                }

                var maxPredictionValue = FC.getMaxNumberFromArray(this.options.data.predictedData);
                var maxRealValue = FC.getMaxNumberFromArray(this.options.data.realData);
                var maxValue = FC.getMaxNumber(maxPredictionValue, maxRealValue);

                for (var j = 0; j < stacksNumber; j++) {
                    dataCount[j * 2] = (this.options.data.predictedData[j] / maxValue) * innerRadiusNumber;
                    dataCount[(j * 2) + 1] = (this.options.data.realData[j] / maxValue) * innerRadiusNumber;
                }

                this.getTooltip(dataCount, maxValue);
            }
        },

        /**
         * Create radial chart tooltips.
         * @method getTooltip
         * @param dataCount
         * @param maxValue
         */
        getTooltip: function (dataCount, maxValue) {
            tooltip = d3.select('body')
                .append('div')
                .style('z-index', '9999')
                .style('position', 'absolute')
                .style('max-width', '50%')
                .style('overflow-x', 'hidden')
                .style('opacity', '1')
                .style('margin', '0px')
                .style('text-align', 'center')
                .style('width', 'auto')
                .style('height', 'auto')
                .style('padding', '6px')
                .style('top', '160px')
                .style('left', '345px')
                .style('word-wrap', 'break-word')
                .style('background', 'rgb(255, 255, 255)')
                .style('pointer-events', 'none')
                .style('border', '1px solid rgb(153, 153, 153)')
                .style('border-radius', '3px')
                .style('border-shadow', 'rgba(0, 0, 0, 0.2) 0px 1px 3px')
                .style('visibility', 'hidden')
                .text('');

            this.g.selectAll('.dataStack')
                .on('mouseover', function (d, i) {
                    var additionalText = (i % 2 === 0 ? dictionary.get("main.labelPredicted") : dictionary.get("main.labelReal")) +
                        ' (' + Math.floor(i / 2) + 'h) : ';
                    tooltip.text(additionalText + Math.round((dataCount[i] / innerRadiusNumber) * maxValue) + ' Mbps');
                    return tooltip.style('visibility', 'visible');
                })
                .on('mousemove', function (d, i) {
                    return tooltip.style('top', (d3.event.pageY - 10) + 'px').style('left', (d3.event.pageX + 10) + 'px');
                })
                .on('mouseleave', function (d, i) {
                    return tooltip.style('visibility', 'hidden');
                })
                .data(dataCount)
                .transition()
                .attr('d', this.dataArc);
        },

        /**
         * Hide tooltip after update, if any.
         * @method hideTooltip
         */
        hideTooltip: function () {
            // We hide tooltips after any update, because if we don't it remains visible also on mouseleave.
            if (tooltip && tooltip.length > 0) {
                tooltip.style('visibility', 'hidden');
            }
        },

        /**
         * Update the timestamp with the latest data.
         * @method updateTimestamp
         */
        updateTimestamp: function () {
            this.setDisplayTime();
            var date = new Date(this.displayTime);
            var formattedDay = d3.time.format('%A')(date);
            var formattedDate = d3.time.format('%e %B')(date);
            this.g.selectAll('.dateTextDay').text(formattedDay);
            this.g.selectAll('.dateTextDate').text(formattedDate);
        },

        setStackNumber: function (numberOfSamples) {
            stacksNumber = numberOfSamples;
        },

        getStackNumber: function () {
            return stacksNumber;
        },

        setInnerRadiusNumber: function (radiusNumber) {
            innerRadiusNumber = radiusNumber;
        },

        getInnerRadiusNumber: function () {
            return innerRadiusNumber;
        }

    });

});