Ext.define('Flamingo.component.highcharts.Treemap', {
    extend: 'portlet.HighchartColumn',
    xtype: 'highchartTreemap',

    initComponent: function() {
        this.callParent(arguments);
    },

    removeAll: function() {
        var me = this;

        if (!me.chart) return;

        me.chart.series[0].setData([]);

        me.chart.redraw();
    },

    onStoreLoad: function(store, records) {
        var me = this;

        if (!me.chart) return;

        var chart = me.chart,
            series = chart.series,
            serieData = [], idx, record;

        for (idx in records) {
            record = records[idx];

            serieData.push({
                name: record.get(me.nameField),
                value: record.get(me.valueField),
                color: record.get('color'),
                fullyQualifiedPath: record.get('fullyQualifiedPath'),
                allocated: record.get('allocated')
            });
        }

        chart.series[0].setData(serieData);

        chart.redraw();
    }
});