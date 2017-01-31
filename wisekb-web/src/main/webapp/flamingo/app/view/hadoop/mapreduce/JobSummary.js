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
Ext.define('Flamingo.view.hadoop.mapreduce.JobSummary', {
    extend: 'Ext.grid.Panel',
    xtype: 'jobSummary',

    bind: {
        store: '{jobSummaryStore}'
    },

    viewConfig: {
        emptyText: 'No data to display'
    },

    columnLines: true,

    columns: [{
        text: 'Key', dataIndex: 'key', flex: 1, style: 'text-align: center;'
    },{
        text: 'Value', dataIndex: 'value', flex: 3, style: 'text-align: center;'
    }]
});