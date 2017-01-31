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
Ext.define('Flamingo.view.hadoop.mapreduce.MapReduceJobs', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.mapReduceJobs',

    title: message.msg('monitoring.history.title.finished'),
    bind: {
        store: '{mapReduceJobsStore}'
    },
    columns: [
        {
            xtype: 'rownumberer',
            width: 40,
            sortable: false
        },
        {
            text: message.msg('dashboard.jobdetail.job.jobid'),
            dataIndex: 'id',
            width: 170,
            align: 'center'
        },
        {
            text: message.msg('dashboard.wh.column.text'),
            dataIndex: 'name',
            minWidth: 200,
            flex: 1,
            style: 'text-align:center',
            renderer: function (value, metaData, record, rowIdx, colIdx, store) {
                metaData.tdAttr = 'data-qtip="'
                    + message.msg('dashboard.jobdetail.job.jobid') + ' : ' + record.get('id')
                    + '<br/>'
                    + message.msg('dashboard.wh.column.text') + ' : ' + (record.get('name') ? record.get('name') : message.msg('monitoring.yarn.tip.na')) + '"';
                return value;
            }
        },
        {
            text: message.msg('common.status'), dataIndex: 'state', width: 100, align: 'center', sortable: true
        },
        {
            text: message.msg('common.user'), dataIndex: 'user', width: 80, align: 'center'
        },
        {
            text: message.msg('common.queue'), dataIndex: 'queue', width: 80, align: 'center'
        },
        {
            text: 'Map', dataIndex: 'mapsTotal', width: 70, align: 'center', sortable: true
        },
        {
            text: 'Reduce', dataIndex: 'reducesTotal', width: 70, align: 'center', sortable: true
        },
        {
            text: 'Elapsed', dataIndex: 'elapsedTime', width: 80, align: 'center',
            renderer: function (value, metaData, record, row, col, store, gridView) {
                var end, start = new Date(record.data.startTime);
                if (record.data.state == 'RUNNING') {
                    end = new Date();
                } else {
                    end = new Date(record.data.finishTime);
                }
                var diff = (end.getTime() - start.getTime()) / 1000;
                return toHumanReadableTime(Math.floor(diff));
            }
        },
        {
            text: message.msg('common.start'), dataIndex: 'startTime', width: 140, align: 'center', sortable: true
        },
        {
            text: message.msg('common.finish'), dataIndex: 'finishTime', width: 140, align: 'center'
        }
    ],
    viewConfig: {
        emptyText: message.msg('monitoring.history.msg.no_mapreduce.jog'),
        deferEmptyText: false,
        columnLines: true,
        stripeRows: true,
        getRowClass: function (b, e, d, c) {
            return 'cell-height-30';
        }
    },
    tools: [
        {
            iconCls: 'x-fa fa-sliders',
            tooltip: 'MapReduce Timeline',
            callback: 'onTimelineClick'
        },
        {
            type: 'refresh',
            tooltip: message.msg('common.refresh'),
            handler: 'onCompletedMRJobRefreshClick'
        }
    ],
    listeners: {
        itemclick: 'onMRJobGridItemClick',
        afterrender: 'onCompletedMRJobAfterRender'
    }
});