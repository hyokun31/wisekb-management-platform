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
Ext.define('Flamingo.view.hadoop.mapreduce.Tasks', {
    extend: 'Ext.tree.Panel',
    xtype: 'tasks',

    rootVisible: false,

    bind: {
        store: '{tasksStore}'
    },

    columnLines: true,

    viewConfig: {
        emptyText: 'MapReduce Job ' + message.msg('monitoring.yarn.tip.na'),
        deferEmptyText: false,
        enableTextSelection: true,
        getRowClass: function (b, e, d, c) {
            return 'cell-height-30';
        }
    },

    columns: [
        {
            xtype: 'treecolumn', text: 'ID', dataIndex: 'id', align: 'left', flex: 1, minWidth: 270, style: 'text-align: center;'
        },
        {
            text: message.msg('common.type'), dataIndex: 'type', width: 65, align: 'center'
        },
        {
            text: message.msg('common.status'), dataIndex: 'state', width: 85, align: 'center'
        },
        {
            xtype: 'widgetcolumn', text: message.msg('common.progress'),
            dataIndex: 'progress', width: 110, align: 'center',
            widget: {
                bind: '{record.progress}',
                xtype: 'progressbarwidget',
                textTpl: [
                    '{value}%'
                ]
            }
        },
        {
            text: 'Elapsed', dataIndex: 'elapsedTime', width: 80, align: 'center'
        },
        {
            text: message.msg('common.start'), dataIndex: 'startTime', width: 140, align: 'center'
        },
        {
            text: message.msg('common.finish'), dataIndex: 'finishTime', width: 140, align: 'center'
        },
        {
            xtype: 'actioncolumn',
            text: 'Logs',
            align: 'center',
            hideMode: 'visibility',
            items: [{
                tooltip: 'Log',
                getClass: function(value, meta, record) {
                    var id = record.get('id');

                    if (id.indexOf('attempt') < 0) {
                        return '';
                    }

                    return 'x-fa fa-file-text-o';
                },
                handler: 'onViewLogClick'
            }]


        }
    ]
});