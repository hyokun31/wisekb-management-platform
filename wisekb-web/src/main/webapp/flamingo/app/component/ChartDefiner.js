Ext.define('Flamingo.Definer', {
    singleton: true
}, function() {
    var me = this;

    me.define = function(type, params) {
        var extend, idx, store, series = [], keys = [], data;
        var records = params.list;

        switch (type) {
            case 'area':
            case 'areaspline':
            case 'line':
                extend = 'portlet.columns.Line';
                store = Ext.create('Flamingo.store.metrics.Line');
                break;
            case 'column':
                extend = 'portlet.columns.Column';
                store = Ext.create('Flamingo.store.metrics.Column');
                break;
            case 'bar':
                extend = 'portlet.columns.Bar';
                store = Ext.create('Flamingo.store.metrics.Column');
                break;
            case 'pie':
                extend = 'portlet.columns.Pie';
                store = Ext.create('Flamingo.store.metrics.Pie');
                break;
            case 'gauge':
                extend = 'portlet.columns.Gauge';
                if (params.xprssn) {
                    store = Ext.create('Flamingo.store.metrics.GaugeColumn', {expression: params.xprssn});
                }
                else {
                    store = Ext.create('Flamingo.store.metrics.Gauge');
                }
                break;
        }

        if (type == 'pie' || type == 'gauge') {
            for (idx in records) {
                if (records[idx].isModel) {
                    data = records[idx].getData();
                }
                else {
                    data = records[idx];
                }
                keys.push(data['meta_key']);
            }

            series.push({
                name: 'name',
                field: 'y'
            });
        }
        else {
            for (idx in records) {
                if (records[idx].isModel) {
                    data = records[idx].getData();
                }
                else {
                    data = records[idx];
                }

                keys.push(data['meta_key']);
                series.push({
                    name: data['meta_key'],
                    field: data['meta_key']
                });
            }
        }

        Ext.define('Flamingo.view.dynamic.' + params.chrt_id, {
            extend: extend,
            xtype: params.chrt_id,
            title: params.title,
            host_sprt_yn: params.host_sprt_yn,
            cllct_prod_cd: params.cllct_prod_cd,
            params: params,
            store: store,
            popup: 'Flamingo.view.chart.DetailPop',
            initComponent: function () {
                var me = this;
                var config = {};

                switch (params.unit_cd) {
                    case 'CD0003001':
                        config = {
                            tooltip: {
                                pointFormatter: function() {
                                    return '<span style="color:' + this.color +'">\u25CF</span> ' + this.series.name + ': <b>' + byteConverter(this.y) + '</b><br/>';
                                }
                            }
                        };
                        break;
                    case 'CD0003003':
                        config = {
                            tooltip: {
                                pointFormatter: function() {
                                    return '<span style="color:' + this.color +'">\u25CF</span> ' + this.series.name + ': <b>' + megaByteConverter(this.y) + '</b><br/>';
                                }
                            }
                        };
                        break;
                }

                me.chartConfig = Ext.merge(config, me.chartConfig);

                me.keys = keys;

                me.callParent(arguments);
            },
            listeners: {
                afterrender: function(view) {
                    view.store.proxy.extraParams = {
                        expression: params.xprssn,
                        chrt_id: params.chrt_id,
                        host_sprt_yn: params.host_sprt_yn,
                        cllct_prod_cd: params.cllct_prod_cd,
                        period_cd: params.period_cd,
                        unit_cd: params.unit_cd
                    };
                },
                beforedestroy: function(view) {
                    if (view.task) {
                        view.task.stop();
                    }
                },
                chartRender: function(chart) {
                    var view = this;

                    view.store.chart = chart;
                    view.store.load({
                        callback: function() {
                            view.fireEvent('storeLoaded', view, this);
                        }
                    });
                },
                storeLoaded: function(view, store) {

                    var rawData = store.proxy.reader.rawData;

                    if (!store.interval) return;

                    if (!store.isSeriData) return;

                    if (!rawData.map.series) return;

                    var interval;
                    switch(view.params.period_cd) {
                        case 'CD0008001':
                            interval = 10000;
                            break;
                        case 'CD0008002':
                            interval = 60000;
                            break;
                    }

                    store.fireEvent('dynamicLoaded', store, view.params);
                }
            }
        });

        return 'Flamingo.view.dynamic.' + params.chrt_id;
    }
});