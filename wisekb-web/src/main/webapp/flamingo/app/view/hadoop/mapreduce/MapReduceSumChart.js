/*
 * Copyright (C) 2011 Flamingo Project (http://www.cloudine.io).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
Ext.define('Flamingo.view.hadoop.mapreduce.MapReduceSumChart', {
    extend: 'portlet.HighchartColumn',
    xtype: 'mapReduceSumChart',

    bind: {
        store: '{mapReduceSum}'
    },

    chartConfig: {
        colors: ['#9CC101'],
        chart: {
            type: 'column',
            spacingBottom: 40,
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
            headerFormat: '',
            pointFormat: '<span style="color:{point.color}">{point.name}</span>: <b>{point.y}</b><br/>'
        },
        legend: {
            enabled: false
        }
    }
});