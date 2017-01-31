Ext.define('portlet.HighchartColumn', {
    extend: 'Ext.panel.Panel',
    xtype: 'highchartColumn',

    frame: true,

    requires: [
        'Ext.data.StoreManager',
        'Ext.data.Store'
    ],

    cls: 'portlet-column',

    mixins: [
        'Ext.util.StoreHolder'
    ],

    isHighchart: true,
    
    isPortletColumn: true,

    defaultBindProperty: 'store',

    fields: null,

    store: Ext.StoreManager.lookup('ext-empty-store'),

    config: {
        columnType: 'highcharts',

        credits: {
            enabled: false
        }
    },

    chartConfig: null,
    
    chart: null,

    /*false, first, last*/
    dataBind: false,

    initComponent: function() {
        this.callParent(arguments);

        var me = this,
            store;

        store = me.store = Ext.data.StoreManager.lookup(me.store || 'ext-empty-store');

        me.bindStore(store, true);
    },

    html: '<div id="chart-{id}"></div>',

    /***
     * afterRender
     * Ext.Component에서 제공하는 함수 Render가 끝난 이후에 호출이 된다.
     *
     * 브라우저에서 Elemnets가 생성되기 전이라 바로 차트를 생성하면 Size가 맞지를 않게 된다.
     * Ext.defer로 차트를 생성하는 시점을 조금 늦춰서 Elements가 완전히 생성된 이후에 차트를 그린다.
     * */
    afterRender: function() {
        this.callParent(arguments);

        var me = this,
            chartId = 'chart-' + me.id,
            chartConfig = me.chartConfig,
            renderTo = {renderTo: chartId},
            events = {};

        me.update({id: chartId});

        if (!chartConfig.chart)
            chartConfig.chart = {};

        chartConfig.chart = Ext.merge(chartConfig.chart, {height: me.height, width: me.width});

        me.on('resize', me.resize);

        /*if (me.title && chartConfig.title) {
         chartConfig.title = Ext.merge(chartConfig.title, {text: me.title});
         }*/

        //Event bind
        events = {
            addSeries: function() {
                me.fireEvent('addSeries', me.chart);
            },
            afterPrint: function() {
                me.fireEvent('afterPrint', me.chart);
            },
            beforePrint: function() {
                me.fireEvent('beforePrint', me.chart);
            },
            click: function(e) {
                me.fireEvent('chartClick', me.chart, e);
            },
            drilldown: function(category, point, originalEvent, points, seriesOptions) {
                me.fireEvent('drilldown', me.chart, category, point, originalEvent, points, seriesOptions);
            },
            drillup: function() {
                me.fireEvent('drillup', me.chart);
            },
            drillupall: function() {
                me.fireEvent('drillupall', me.chart);
            },
            load: function(chart) {
                me.fireEvent('chartLoad', me.chart);
            },
            redraw: function() {
                me.fireEvent('redraw', me.chart);
            },
            selection: function(e) {
                me.fireEvent('selection', me.chart, e);
            }
        };

        if (chartConfig.chart != null && chartConfig.chart != undefined) {
            chartConfig.chart = Ext.merge(chartConfig.chart, renderTo, {events: events});
        }
        else {
            chartConfig.chart = Ext.merge(renderTo, {events: events});
        }

        chartConfig.credits = {enabled: false};
        chartConfig.viewId = me.getId();

        me.chart = new Highcharts.Chart(chartConfig, me.callback);
    },

    /***
     * callback
     * Hicharts의 컴포넌트가 생성된 이후 호출 된다.
     * */
    callback: function(chart) {
        //After chart rendered

        var view = Ext.getCmp(chart.options.viewId);

        if (view) {
            view.fireEvent('chartRender', chart);

            Ext.defer(function() {
                try {
                    view.resize(view, view.getWidth(), view.getHeight());
                } catch (err) {

                }
            }, 300);

        }
    },

    /***
     * resize
     * Ext.Component Reize 이벤트가 발생되면 Chart의 Size를 조정한다.
     * */
    resize: function(component, width, height) {
        var headerHeight = 0;

        if (component.header) {
            headerHeight = $('.portlet-column .' + component.header.componentCls).height() + 10;

            if (!headerHeight) {
                headerHeight = $('#' + component.header.getEl().id).height() + 10;
            }
        }

        if (component.chart)
            component.chart.setSize(width, height - headerHeight, false);
    },

    onStoreLoad: function(store, records) {
        var me = this;
        
        if (!me.chart) return;
        
        var chart = me.chart,
            type = me.chart.options.chart.type,
            series = chart.series,
            serieData = {},
            xAxis = [],
            idx, field, sere, start = 0, end = records.length;

        if (store.isSeriData) {
            var rawData = store.proxy.reader.rawData;

            if (!rawData.map.series) return;

            if (type == 'areaspline' || type == 'area' || type == 'line' || !type) {
                chart.xAxis[0].setCategories(rawData.map.categories, true);
                chart.unit_cd = store.unit_cd;

                for (idx in rawData.map.series) {
                    if (chart.isDataLoaded && chart.series.length > 0) {
                        series[idx].setData(rawData.map.series[idx].data);
                        series[idx].name = rawData.map.series[idx].name
                    }
                    else {
                        chart.addSeries({
                            name: rawData.map.series[idx].name,
                            data: rawData.map.series[idx].data
                        });
                    }
                }
            }
            else if (type == 'pie') {
                var pieData = [];
                for (idx in rawData.map.series) {
                    pieData.push({
                        name: rawData.map.series[idx].name,
                        y: rawData.map.series[idx].y
                    });
                }

                chart.addSeries({
                    name: 'pie',
                    data: pieData
                });
            }
            else if (type == 'solidgauge') {
                if (chart.isDataLoaded && chart.series.length > 0) {
                    series[0].setData([records[0].get('y')]);
                }
                else {
                    chart.addSeries({
                        name: 'gauge',
                        data: [records[0].get('y')]
                    });
                }

            }
            else if (type == 'column') {
                if (chart.isDataLoaded && chart.series.length > 0) {
                    series[0].setData(rawData.map.series);
                }
                else {
                    if (chart.options.plotOptions.column.stacking) {
                        chart.xAxis[0].setCategories(rawData.map.categories, true);

                        for (idx in rawData.map.series) {
                            chart.addSeries({
                                name: rawData.map.series[idx].name,
                                data: rawData.map.series[idx].data
                            });
                        }
                    }
                    else {
                        chart.addSeries({
                            name: 'column',
                            colorByPoint: true,
                            data: rawData.map.series
                        });
                    }
                }
            }

            chart.isDataLoaded = true;

        }
        else {
            if (type == 'pie' || type == 'solidgauge' || (chart.userOptions.xAxis && chart.userOptions.xAxis.type == 'category')) {
                if (chart.series[0].options.names && chart.series[0].options.fields) {
                    serieData = [];
                    switch (me.dataBind) {
                        case 'first':
                            idx = 1;
                            break;
                        case 'last':
                            idx = records.length - 1;
                            break;
                        default:
                            idx = 1;
                            break;
                    }

                    var names = chart.series[0].options.names,
                        fields = chart.series[0].options.fields,
                        record = store.getAt(idx), yValue;

                    for (idx in fields) {
                        serieData.push({
                            name: names[idx],
                            y: record.get(fields[idx])
                        });
                    }
                }
                else {
                    serieData = me.records2Json(store.getData().items);
                }
                chart.series[0].setData(serieData);
            }
            else {
                for (idx in series) {
                    eval('serieData.' + series[idx].options.field + '= []');
                }

                switch (me.dataBind) {
                    case 'first':
                        end = 1;
                        break;
                    case 'last':
                        start = records.length - 1;
                        break;
                }

                for (idx = start; idx < end; idx++) {

                    if (chart.options.categoryField) {
                        xAxis.push(records[idx].get(chart.options.categoryField));
                    }
                    else {
                        xAxis.push(Ext.Date.format(new Date(records[idx].get('timestamp')), 'H:i:s'));
                    }

                    for (sere in series) {
                        field = series[sere].options.field;

                        serieData[field].push(records[idx].get(field));
                    }
                }

                for (idx in series) {
                    field = series[idx].options.field;

                    series[idx].setData(serieData[field]);
                }

                chart.xAxis[0].setCategories(xAxis);

            }
        }
        
        chart.redraw();
    },

    onSubscribe: function(callback, message) {
        var me = this,
            chart = me.chart,
            series = chart.series,
            data = Ext.decode(message.body),
            idx, name;

        for (idx in series) {
            name = series[idx].name.toLowerCase();

            series[idx].addPoint([Ext.Date.format(new Date(data.timestamp), 'H:i:s'), data[name]], true, true);
        }
    },

    setStore: function (newStore) {
        var me = this;

        if (me.store !== newStore) {
            if (me.isConfiguring) {
                me.store = newStore;
            } else {
                me.bindStore(newStore, /*initial*/ false);
                if (newStore.getData() && newStore.getData().items.length > 0) {
                    me.onStoreLoad(newStore, newStore.getData().items);
                }
            }
        }
    },

    getStoreListeners: function() {
        var me = this;
        return {
            load: me.onStoreLoad,
            subscribe: me.onSubscribe
        };
    },

    records2Json : function(records) {
        var result = [];

        for (var j = 0; j < records.length; j++) {
            var models = {};
            var items = records[j].fields.items;
            for (var i = 0; i < items.length; i++) {
                var name = items[i].name;
                models[name] = records[j].get(name);
            }
            result.push(models);
        }

        return result;
    }
});
