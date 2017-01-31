Ext.define('portlet.columns.Pie', {
    extend: 'portlet.HighchartColumn',
    xtype: 'pieColumn',

    dataBind: 'last',

    chartConfig: {
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            spacingBottom: 8,
            type: 'pie'
        },
        title: {
            text: ''
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                showInLegend: true,
                dataLabels: {
                    enabled: false
                }
            }
        },
        tooltip: {
            headerFormat: '',
            pointFormat: ' <span style="color:{point.color}">\u25CF</span> {point.name}: <b>{point.y}</b><br/>'
        },
        exporting: {
            buttons: {
                contextButton: {
                    enabled: false
                }
            }
        }
    }
});