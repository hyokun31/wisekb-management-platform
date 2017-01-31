Ext.define('portlet.columns.Bar', {
    extend: 'portlet.HighchartColumn',
    xtype: 'barColumn',
    
    dataBind: 'last',

    chartConfig: {
        chart: {
            type: 'bar',
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
            title: {
                text: null
            },
            visible: false
        },
        yAxis: {
            min: 0,
            title: {
                text: null
            }
        },
        plotOptions: {
            bar: {
                dataLabels: {
                    enabled: true
                },
                pointPadding: 0.1,
                pointRange: 3,
                groupPadding: 0,
                borderWidth: 0
            }
        },
        credits: {
            enabled: false
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