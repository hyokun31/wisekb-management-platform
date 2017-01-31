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
Ext.define('Flamingo.view.hadoop.yarnapplication.YarnApplicationModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.yarnApplicationModel',

    data: {
        title: message.msg('monitoring.yarn.title')
    },

    stores: {
        summary: {
            fields: ['key',
                {
                    name: 'value',
                    convert: function (value,record) {
                        if (record.get('key').match('Time') == null) {
                            return isNaN(value / 1) ? value : convertComma(value);
                        } else {
                            return convertDateTime(value);
                        }
                    }
                }
            ],
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.YARNAPPLICATION.APP_REPORT,
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'total'
                }
            }
        },
        queueStore: {
            type: 'tree',
            autoLoad: false,
            rootVisible: true,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.YARNAPPLICATION.QUEUES
            },
            root: {
                text: 'root',
                expanded: false,
                id: 'root'
            }
        },

        timeSeriesStore: {
            fields: ['time', 'sum'],
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.YARNAPPLICATION.TIME_SERIES,
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'total'
                }
            },
            remoteSort: true,
            sorters: [
                {
                    property: 'num',
                    direction: 'ASC'
                }
            ]
        },

        allApplicationsStore: {
            model: 'Flamingo.model.hadoop.yarn.Application',
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.YARNAPPLICATION.ALL_APPICATION,
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'total'
                }
            },
            remoteSort: false,  // groupField가 설정되면 groupField 기준으로 정렬이 됨
            sorters: [
                {
                    property: 'startTime',
                    direction: 'DESC'
                }
            ]
        },

        configurationStore: {
            autoLoad: false,
            remoteSort: false,
            fields: ['name', 'source', 'value'],
            proxy: {
                type: 'ajax',
                url: CONSTANTS.MONITORING.HS.CONF,
                reader: {
                    type: 'json',
                    rootProperty: 'conf.property',
                    totalProperty: 'total'
                }
            },
            sorters: [
                {
                    property: 'name',
                    direction: 'DESC'
                }
            ]
        },

        types: {
            fields: ['name', 'y'],
            autoLoad: false,
            isSeriData: true,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.YARNAPPLICATION.APP_TYPES,
                reader: {
                    type: 'json',
                    keepRawData: true,
                    rootProperty: 'list',
                    totalProperty: 'total'
                }
            }
        }
    }
});