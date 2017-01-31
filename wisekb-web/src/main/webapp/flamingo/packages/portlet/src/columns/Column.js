Ext.define('portlet.columns.Column', {
    extend: 'portlet.HighchartColumn',
    xtype: 'columnColumn',

    dataBind: 'last',

    chartConfig: {
        chart: {
            type: 'column',
            spacingBottom: 8
        },
        title: {
            align: 'left',
            margin: 10,
            style: {
                fontSize: '14px'
            }
        },
        xAxis: {
            visible: false
        },
        yAxis: {
            min: 0,
            title: {
                text: ''
            }
        },
        plotOptions: {
            column: {
                pointPadding: 0.1,
                pointRange: 3,
                groupPadding: 0,
                borderWidth: 0
            }
        },
        tooltip: {
            headerFormat: ''
        },
        legend: {
            itemDistance: 5,
            margin: 15,
            padding: 0
        }
    }
});