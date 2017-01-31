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
Ext.define('Flamingo.view.hadoop.yarnapplication.AllApplications', {
    extend: 'Ext.grid.Panel',
    xtype: 'allApplications',

    requires: [
        'Flamingo.model.hadoop.yarn.Application'
    ],
    title: 'All YARN Applications',
    bind: {
        store: '{allApplicationsStore}'
    },
    viewConfig: {
        emptyText: 'Doesn\u0027t have completed YARN Application',
        deferEmptyText: false,
        columnLines: true,
        stripeRows: true,
        getRowClass: function (record, index) {
            // Change row color if state is running
            if (record.get('yarnApplicationState') == 'RUNNING') {
                return 'selected-grid cell-height-30';
            }
            return 'cell-height-30';
        }
    },
    tools: [
        {
            type: 'refresh',
            tooltip: 'Refresh',
            handler: 'onAllApplicationRefreshClick'
        }
    ],
    columns: [
        {
            xtype: 'rownumberer',
            width: 40,
            sortable: false
        },
        {
            text: message.msg('monitoring.application_id'),
            dataIndex: 'applicationId',
            width: 220,
            align: 'center',
            renderer: function (value, metaData, record, rowIdx, colIdx, store) {
                metaData.tdAttr = 'data-qtip="'
                    + message.msg('monitoring.application_id') + ' : ' + record.get('applicationId')
                    + '<br/>'
                    + message.msg('monitoring.application_type') + ' : ' + record.get('applicationType')
                    + '<br/>'
                    + message.msg('monitoring.application_name') + ' : ' + (record.get('name') ? record.get('name') : message.msg('monitoring.yarn.tip.na')) + '"';
                return value;
            },
            summaryType: 'count',
            summaryRenderer: function (value, summaryData, dataIndex) {
                return ((value === 0 || value > 1) ? '(' + value + ' ' + message.msg('monitoring.yarn.tip.count') + ')' : '(1 ' + message.msg('monitoring.yarn.tip.count') + ')');
            }
        },
        {
            text: message.msg('monitoring.application_name'), dataIndex: 'name', width: 200, align: 'center'
        },
        {
            text: message.msg('common.user'), dataIndex: 'user', width: 100, align: 'center', sortable: true
        },
        {
            xtype: 'widgetcolumn',
            text: 'Action',
            align: 'center',
            width: 73,
            widget: {
                xtype: 'button',
                text: 'Kill',
                bind: {
                    hidden: '{record.yarnApplicationState}' == 'RUNNING' ? false : true
                },
                handler: 'onYarnApplicationKillClick'
            }
        },
        {
            text: message.msg('common.status'),
            dataIndex: 'yarnApplicationState',
            width: 90,
            align: 'center',
            sortable: true
        },
        {
            text: message.msg('common.final_status'),
            dataIndex: 'finalApplicationStatus',
            width: 90,
            align: 'center',
            sortable: true
        },
        {
            text: message.msg('common.type'), dataIndex: 'applicationType', align: 'center', sortable: true
        },
        {
            text: 'Elapsed', dataIndex: 'elapsedTime', width: 80, align: 'center',
            renderer: function (value, metaData, record, row, col, store, gridView) {
                if (record.get('yarnApplicationState') == 'RUNNING') {
                    var start = Ext.Date.parse(record.get('startTime'), "Y-m-d H:i:s", true);
                    var end = new Date();
                    var diff = (end.getTime() - start.getTime()) / 1000;
                    return toHumanReadableTime(Math.floor(diff));
                } else if (
                    record.get('yarnApplicationState') == 'FINISHED' ||
                    record.get('yarnApplicationState') == 'FAILED' ||
                    record.get('yarnApplicationState') == 'KILLED') {
                    var start = Ext.Date.parse(record.get('startTime'), "Y-m-d H:i:s", true);
                    var end = Ext.Date.parse(record.get('finishTime'), "Y-m-d H:i:s", true);
                    var diff = (end.getTime() - start.getTime()) / 1000;
                    return toHumanReadableTime(Math.floor(diff));
                } else {
                    return '';
                }
            }
        },
        {
            xtype: 'widgetcolumn', text: 'Progress', dataIndex: 'progress', width: 110, align: 'center',
            widget: {
                bind: '{record.progress}',
                xtype: 'progressbarwidget',
                textTpl: [
                    '{value}%'
                ]
            }
        },
        {
            text: message.msg('common.memory'), dataIndex: 'neededResourcesMemory', align: 'center', sortable: true
        },
        {
            text: message.msg('common.core'),
            dataIndex: 'neededResourcesVcores',
            width: 70,
            align: 'center',
            sortable: true
        },
        {
            text: message.msg('common.queue'), dataIndex: 'queue', align: 'center'
        },
        {
            text: message.msg('common.start'), dataIndex: 'startTime', width: 140, align: 'center', sortable: true
        },
        {
            text: message.msg('common.finish'), dataIndex: 'finishTime', width: 140, align: 'center',
            renderer: function (value, metaData, record, row, col, store, gridView) {
                if (record.data.yarnApplicationState == 'RUNNING') {
                    return message.msg('common.running');
                } else if (
                    record.data.yarnApplicationState == 'FINISHED' ||
                    record.data.yarnApplicationState == 'FAILED' ||
                    record.data.yarnApplicationState == 'KILLED') {
                    return value;
                } else {
                    return '';
                }
            }
        }
    ],
    listeners: {
        itemcontextmenu: 'onItemContextMenu',
        itemclick: 'onAllYarnAppGridItemClick',
        afterrender: 'onAllApplicationsAfterRender'
    }
});