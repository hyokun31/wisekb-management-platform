Ext.define('portlet.columns.Gauge', {
    extend: 'portlet.HighchartColumn',
    xtype: 'gaugeColumn',

    dataBind: 'last',

    columnType: 'gauge',

    chartConfig: {
        chart: {
            type: 'solidgauge'
        },

        title: {
            text: ''
        },

        pane: {
            background: {
                borderWidth: 0,
                innerRadius: '60%',
                outerRadius: '100%',
                shape: 'arc'
            }
        },

        tooltip: {
            enabled: false
        },

        plotOptions: {
            solidgauge: {
                dataLabels: {
                    borderWidth: 0,
                    verticalAlign: 'middle',
                    format:'{y}%',
                    style: {
                        fontSize: '18px'
                    },
                    x: 0,
                    y: 0
                }
            }
        },

        // the value axis
        yAxis: {
            stops: [
                [0.1, '#55BF3B'], // green
                [0.5, '#DDDF0D'], // yellow
                [0.9, '#DF5353'] // red
            ],
            lineWidth: 0,
            minorTickInterval: null,
            tickWidth: 0,
            title: {
                text: ''
            },
            labels: {
                enabled: false
            },
            min: 0,
            max: 100
        }
    },

    resizeText: function() {
        var id = this.id,
            width = this.width ? this.width : this.chart.plotWidth,
            height = this.height ? this.height : this.chart.plotHeight,
            minValue = (height < width) ? height : width,
            size = Math.floor(minValue / 10),
            textSize, textHeight, spanTop;

        if (size > 50) size = 50;

        $('#' + id + ' .highcharts-data-labels > div > span').css('font-size', size + 'px');
        
        textSize = $('#' + id + ' .highcharts-data-labels > div > span').width();
        textHeight = $('#' + id + ' .highcharts-data-labels > div > span').height();
        var top = $('#' + id + ' .highcharts-data-labels > div > span').css('top');
        spanTop = top ? parseInt(top.replace('px', '')) : 0;

        var left = width / 2 - 15 - (textSize / 2);
        var top;
        
        if (spanTop <= 10)
            top = (height - 42) / 2 - spanTop - (textHeight / 2) - spanTop;
        else
            top = (height - 42) / 2 - spanTop - (textHeight / 2);
        
        $('#' + id + ' .highcharts-data-labels > div').css('left', left + 'px');
        $('#' + id + ' .highcharts-data-labels > div').css('top', top + 'px');
    }
});