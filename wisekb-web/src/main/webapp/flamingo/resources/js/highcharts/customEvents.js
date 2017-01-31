/**
 * Custom events v1.4.1 (2016-07-05)
 *
 * (c) 2012-2016 Black Label
 *
 * License: Creative Commons Attribution (CC)
 */

/* global Highcharts setTimeout clearTimeout module:true */
/* eslint no-loop-func: 0 */

(function (factory) {
    if (typeof module === 'object' && module.exports) {
        module.exports = factory;
    } else {
        factory(Highcharts);
    }
}(function (HC) {
    /* global Highcharts :true */

    'use strict';

    var UNDEFINED,
        DBLCLICK = 'dblclick',
        COLUMN = 'column',
        MAP = 'map',
        TOUCHSTART = 'touchstart',
        CLICK = 'click',
        each = HC.each,
        pick = HC.pick,
        wrap = HC.wrap,
        protoTick = HC.Tick.prototype,
        protoAxis = HC.Axis.prototype,
        protoChart = HC.Chart.prototype,
        protoLegend = HC.Legend.prototype,
        protoSeries = HC.Series.prototype,
        protoColumn = HC.seriesTypes.column && HC.seriesTypes.column.prototype,
        protoBar = HC.seriesTypes.bar && HC.seriesTypes.bar.prototype,
        protoPie = HC.seriesTypes.pie && HC.seriesTypes.pie.prototype,
        protoBubble = HC.seriesTypes.bubble && HC.seriesTypes.bubble.prototype,
        protoColumnRange = HC.seriesTypes.columnrange && HC.seriesTypes.columnrange.prototype,
        protoAreaRange = HC.seriesTypes.arearange && HC.seriesTypes.arearange.prototype,
        protoAreaSplineRange = HC.seriesTypes.areasplinerange && HC.seriesTypes.areasplinerange.prototype,
        protoErrorbar = HC.seriesTypes.errorbar && HC.seriesTypes.errorbar.prototype,
        protoBoxplot = HC.seriesTypes.boxplot && HC.seriesTypes.boxplot.prototype,
        protoPlotBands = HC.PlotLineOrBand && HC.PlotLineOrBand.prototype,
        protoFlags = HC.seriesTypes.flags && HC.seriesTypes.flags.prototype,
        seriesAnimate = protoSeries && protoSeries.animate,
        columnAnimate = protoColumn && protoColumn.animate,
        barAnimate = protoBar && protoBar.animate,
        pieAnimate = protoPie && protoPie.animate,
        defaultOptions = HC.getOptions().plotOptions;

    function noop() { return false; }

    //  Highcharts functions
    function isArray(obj) {
        return Object.prototype.toString.call(obj) === '[object Array]';
    }

    //  reseting all events, fired by Highcharts
    HC.Chart.prototype.callbacks.push(function (chart) {
        var resetAxisEvents = chart.customEvent.resetAxisEvents,
            forExport = chart.renderer.forExport,
            series = chart.series,
            serLen = series.length,
            xAxis = chart.xAxis,
            yAxis = chart.yAxis,
            i;

        if (forExport) {    //  skip custom events when chart is exported
            return false;
        }

        resetAxisEvents(xAxis, 'xAxis', chart);
        resetAxisEvents(yAxis, 'yAxis', chart);

        for (i = 0; i < serLen; i++) {
            series[i].update({
                animation: {
                    enabled: true
                },
                customEvents: {
                    series: series[i].options.events,
                    point: series[i].options.point.events
                },
                events: {
                    click: noop
                },
                point: {
                    events: {
                        click: noop
                    }
                }
            }, false);
        }

        chart.xAxis[0].isDirty = true;
        return false;
    });

    //  custom event body
    var customEvent = HC.Chart.prototype.customEvent = function (obj, proto) {
        customEvent.add = function (elem, events, elemObj, series) {

            for (var key in events) {
                if (key) {

                    (function (val) {
                        if (events.hasOwnProperty(val) && elem) {

                            if ((!elem[val] || elem[val] === UNDEFINED) && elem.element) {

                                if ((val === DBLCLICK)) { //  #30

                                    var tapped = false;

                                    HC.addEvent(elem.element, TOUCHSTART, function (e) {

                                        if (!tapped) {

                                            tapped = setTimeout(function () {
                                                tapped = null;
                                                events[CLICK].call(elemObj, e); //	call single click action
                                            }, 300);

                                        } else {
                                            clearTimeout(tapped);

                                            tapped = null;

                                            events[val].call(elemObj, e);

                                        }

                                        return false;
                                    });

                                }

                                HC.addEvent(elem.element, val, function (e) {

                                    if (elemObj && elemObj.textStr) { //   labels
                                        elemObj.value = elemObj.textStr;
                                    }

                                    if (series && defaultOptions[series.type] && defaultOptions[series.type].marker) {

                                        var chart = series.chart,
                                            normalizedEvent = chart.pointer.normalize(e),
                                            i;

                                        elemObj = series.searchPoint(normalizedEvent, true);

                                    }

                                    events[val].call(elemObj, e);

                                    return false;
                                });
                            }

                            elem[val] = function () {
                                return true;
                            };
                        }
                    })(key);
                }

            }
        };

        HC.Chart.prototype.customEvent.resetAxisEvents = function (axis, type, chart) {
            var axisLength = axis.length,
                userOptions = chart.userOptions,
                redraw = false,
                plotBandsLength, plotLinesLength, plotLines, plotBands, cAxis, t, i, j;

            for (i = 0; i < axisLength; i++) {

                if (type) {
                    cAxis = HC.splat(userOptions[type]);
                    plotLines = cAxis[i] && cAxis[i].plotLines;
                    plotBands = cAxis[i] && cAxis[i].plotBands;
                }

                if (plotLines !== UNDEFINED) {
                    plotLinesLength = plotLines.length;

                    for (j = 0; j < plotLinesLength; j++) {
                        t = plotLines[j].events;
                        if (t) {
                            plotLines[j].customEvents = t;
                            plotLines[j].events = null;
                        }
                    }

                    redraw = true;
                }

                if (plotBands !== UNDEFINED) {
                    plotBandsLength = plotBands.length;

                    for (j = 0; j < plotBandsLength; j++) {
                        t = plotBands[j].events;
                        if (t) {
                            plotBands[j].customEvents = t;
                            plotBands[j].events = null;
                        }
                    }

                    redraw = true;
                }

                if (redraw) {
                    axis[i].update({
                        plotLines: plotLines,
                        plotBands: plotBands
                    }, false);
                }
            }
        };

        wrap(obj, proto, function (proceed) {
            var events,
                element,
                eventsPoint,
                elementPoint,
                eventsSubtitle,
                elementSubtitle,
                parent,
                type,
                op,
                len,
                i,
                j;

            //  call default actions
            var ob = proceed.apply(this, Array.prototype.slice.call(arguments, 1));

            //  switch on object
            switch (proto) {
                case 'addLabel':
                    parent = this.parent;
                    eventsPoint = this.axis.options.labels.events;
                    elementPoint = [this.label];

                    if (parent) {
                        var step = this; // current label

                        while (step) {
                            if (isArray(step)) {
                                len = step.length;

                                for (i = 0; i < len; i++) {
                                    elementPoint.push(step[i].label);
                                }
                            } else {
                                elementPoint.push(step.label);
                            }

                            step = step.parent;
                        }

                    }

                    break;
                case 'setTitle':
                    events = this.options.title && this.options.title.events;
                    element = this.title;
                    eventsSubtitle = this.options.subtitle && this.options.subtitle.events;
                    elementSubtitle = this.subtitle;
                    break;
                case 'drawDataLabels':
                    events = this.dataLabelsGroup ? this.options.dataLabels.events : null;
                    element = this.dataLabelsGroup ? this.dataLabelsGroup : null;
                    break;
                case 'render':
                    if (this.axisTitle) {
                        events = this.options.title.events;
                        element = this.axisTitle;
                    }

                    if (this.options.value || this.options.from) {
                        events = this.options.customEvents;
                        element = this.svgElem;
                    }

                    if (this.options.stackLabels && this.options.stackLabels.enabled) {
                        events = this.options.stackLabels.events;
                        element = this.stackTotalGroup;
                        eventsPoint = this.options.stackLabels.events;
                        elementPoint = this.stacks;
                    }

                    break;
                case 'drawPoints':
                    op = this.options;
                    type = this.type;
                    events = op.events;
                    element = this.group;
                    eventsPoint = op.customEvents ? op.customEvents.point : op.point.events;

                    if (defaultOptions[type] && defaultOptions[type].marker) {
                        elementPoint = [this.markerGroup];
                    } else {
                        elementPoint = this.points;
                    }

                    break;
                case 'renderItem':
                    events = this.options.itemEvents;
                    element = this.group;
                    break;
                default:
                    events = element = UNDEFINED;
                    return false;
            }


            if ((events !== UNDEFINED) || (eventsPoint !== UNDEFINED)) {

                if (eventsPoint) {

                    len = elementPoint.length;

                    for (j = 0; j < len; j++) {
                        var elemPoint = HC.pick(elementPoint[j].graphic, elementPoint[j]);

                        if (elemPoint && elemPoint !== UNDEFINED) {
                            customEvent.add(elemPoint, eventsPoint, elementPoint[j], this);
                        }
                    }
                }

                if (eventsSubtitle) {
                    customEvent.add(elementSubtitle, eventsSubtitle, this);
                }

                customEvent.add(element, events, this);
            }

            return ob;
        });
    };
    //  labels
    customEvent(protoTick, 'addLabel');

    //  axis / title
    customEvent(protoAxis, 'render');

    //  series events & point events
    customEvent(protoSeries, 'drawPoints');

    //  datalabels events
    customEvent(protoSeries, 'drawDataLabels');

    //  title events
    customEvent(protoChart, 'setTitle');

    //  legend items
    customEvent(protoLegend, 'renderItem');

    //  plotbands + plotlines
    if (protoPlotBands) {
        customEvent(protoPlotBands, 'render');
    }

    //  bubble charts
    if (protoBubble) {
        customEvent(protoBubble, 'drawPoints');
        customEvent(protoBubble, 'drawDataLabels');
    }

    //  column chart
    if (protoColumn) {
        customEvent(protoColumn, 'drawDataLabels');
        customEvent(protoColumn, 'drawPoints');
    }

    //  pie chart
    if (protoPie) {
        customEvent(protoPie, 'drawDataLabels');
        customEvent(protoPie, 'drawPoints');
    }

    //	columnrange
    if (protoColumnRange) {
        customEvent(protoColumnRange, 'drawDataLabels');
        customEvent(protoColumnRange, 'drawPoints');
    }

    //	arearange
    if (protoAreaRange) {
        customEvent(protoAreaRange, 'drawDataLabels');
        customEvent(protoAreaRange, 'drawPoints');
    }

    //	areasplinerange
    if (protoAreaSplineRange) {
        customEvent(protoAreaSplineRange, 'drawDataLabels');
        customEvent(protoAreaSplineRange, 'drawPoints');
    }

    //	errorbar
    if (protoErrorbar) {
        customEvent(protoErrorbar, 'drawDataLabels');
        customEvent(protoErrorbar, 'drawPoints');
    }

    //	boxplot
    if (protoBoxplot) {
        customEvent(protoBoxplot, 'drawDataLabels');
        customEvent(protoBoxplot, 'drawPoints');
    }

    //  flags
    if (protoFlags) {
        customEvent(protoFlags, 'drawDataLabels');
        customEvent(protoFlags, 'drawPoints');
    }

}));