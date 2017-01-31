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
Ext.define('Flamingo.view.hadoop.mapreduce.MapReduceModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.mapReduceModel',

    requires: [
        'Flamingo.model.hadoop.mapreduce.JobCounters'
    ],

    data: {
        title: 'MapReduce'
    },

    stores: {
        mapReduceSum: {
            fields: ['name', 'y'],
            autoLoad: false,
            isSeriData: true,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.MAPREDUCE.TIME_SERIES,
                reader: {
                    type: 'json',
                    keepRawData: true,
                    rootProperty: 'list',
                    totalProperty: 'total'
                }
            }
        },

        tasksStore: {
            type: 'tree',
            autoLoad: false,
            rootVisible: false,
            model: 'Flamingo.model.hadoop.mapreduce.Tasks',
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.MAPREDUCE.TASKS,
                extraParams: {

                },
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'total'
                }
            },
            root: {
                text: 'FEM',
                expanded: true
            },
            sorters: [
                {
                    property: 'id',
                    direction: 'asc'
                }
            ],
            listeners: {
                beforeload: 'onTasksBeforeload'
            }
        },

        configurationStore: {
            autoLoad: false,
            remoteSort: false,
            fields: ['name', 'source', 'value'],
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.MAPREDUCE.CONF,
                reader: {
                    type: 'json',
                    rootProperty: 'conf.property',
                    totalProperty: 'total'
                }
            },
            sorters: [
                {
                    property: 'name',
                    direction: 'desc'
                }
            ]
        },

        mapReduceJobsStore: {
            model: 'Flamingo.model.hadoop.mapreduce.MapReduceJob',
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.MAPREDUCE.JOBS,
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'total'
                }
            },
            sorters: [
                {
                    property: 'id',
                    direction: 'desc'
                }
            ]
        },

        jobCounterStore: {
            type: 'tree',
            model: 'Flamingo.model.hadoop.mapreduce.JobCounters',
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.MAPREDUCE.COUNTERS
            },
            sorters: [
                {
                    property: 'name',
                    direction: 'asc'
                }
            ],
            rootVisible: false,
            root: {
                text: 'JobCounters',
                expanded: true,
                id: 'root'
            }
        },

        jobSummaryStore: {
            fields: ['key', {
                name: 'value',
                convert: function(value, record) {
                    switch (record.get('key')) {
                        case 'submitTime':
                        case 'startTime':
                        case 'finishTime':
                            return convertDateTime(value);
                            break;
                        default:
                            return value;
                    }
                }
            }],
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.MAPREDUCE.JOB,
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'total'
                }
            }
        }
    }
});