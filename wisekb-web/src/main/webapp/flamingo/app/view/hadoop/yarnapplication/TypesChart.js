Ext.define('Flamingo.view.hadoop.yarnapplication.TypesChart', {
    extend: 'portlet.HighchartColumn',
    xtype: 'yarnapplicationTypes',

    bind: {
        store: '{types}'
    },

    chartConfig: {
        chart: {
            type: 'column',
            spacing: 20
        },
        title: {
            text: ''
        },
        yAxis: {
            min: 0,
            title: {
                text: ''
            }
        },
        xAxis: {
            type: 'category'
        },
        plotOptions: {
            series: {
                borderWidth: 0,
                dataLabels: {
                    enabled: true
                }
            }
        },
        tooltip: {
            headerFormat: ''
        },
        legend: {
            enabled: false
        }
    }
});