Ext.define('portlet.columns.Line', {
    extend: 'portlet.HighchartColumn',
    xtype: 'lineColumn',

    tools: [{
        iconCls: 'fa fa-line-chart',
        handler: function(event, el, header) {
            var component = header.container.component;
            if (component.popup) {
                var panel = component.up('#splitHost'),
                    combo;

                if (panel) {
                    combo = panel.getReferences().hostnameCombo;
                }

                Ext.create(component.popup, {
                    chartID: component.xtype,
                    title: component.title,
                    params: component.params,
                    hostname: combo ? combo.getValue() : null
                }).show();
            }
        }
    }],

    chartConfig: {
        chart: {
            type: 'areaspline',
            spacing: [15, 15, 20, 15],
            animation: Highcharts.svg
        },
        title: {
            text: ''
        },
        xAxis: {
            type: 'datetime',
            tickInterval: 5,
            labels: {
                step: 2,
                style: {
                    fontSize: '10px'
                }
            }
        },
        yAxis: {
            title: {
                text: ''
            },
            lineWidth: 1,
            softMin: 0,
            min: 0
        },
        plotOptions: {
            areaspline: {
                lineWidth: 1,
                fillOpacity: 0.3,
                softThreshold: true,
                marker: {
                    enabled: false
                }
            }
        },
        tooltip: {
            crosshairs: [true, true],
            shared: true,
            followPointer: true,
            followTouchMove: true,
            useHTML: true
        },
        legend: {
            enabled: false
        }
    }
});
