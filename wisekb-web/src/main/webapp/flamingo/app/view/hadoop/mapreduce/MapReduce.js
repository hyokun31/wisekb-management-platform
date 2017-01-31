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
Ext.define('Flamingo.view.hadoop.mapreduce.MapReduce', {
    extend: 'Ext.panel.Panel',
    xtype: 'mapreduce',

    requires: [
        'Flamingo.view.hadoop.mapreduce.MapReduceController',
        'Flamingo.view.hadoop.mapreduce.MapReduceModel',
        'Flamingo.view.hadoop.mapreduce.MapReduceSumChart',
        'Flamingo.view.hadoop.mapreduce.MapReduceJobs',
        'Flamingo.view.hadoop.mapreduce.JobSummary',
        'Flamingo.view.hadoop.mapreduce.Configuration',
        'Flamingo.view.hadoop.mapreduce.JobCounters',
        'Flamingo.view.hadoop.mapreduce.Tasks'
    ],

    controller: 'mapReduceController',

    viewModel: 'mapReduceModel',

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    bodyCls: 'fem-panel',

    scrollable: true,

    defaults: {
        frame: true
    },

    items: [
        {
            xtype: 'component',
            height: 60,
            cls: 'fem-header-title',
            html: '<h2 style="padding: 0; margin:20px 0 0 20px;">MapReduce</h2>'
        },
        {
            xtype: 'mapReduceSumChart',
            height: 200,
            title: message.msg('monitoring.history.msg.mr_job_statistics'),
            margin: '20 20 7 20',
            layout: 'fit',
            cls: 'panel-shadow'
        },
        {
            xtype: 'mapReduceJobs',
            reference: 'mapReduceJobGrid',
            flex: 1,
            margin: '7 20 7 20',
            minHeight: 220,
            padding: '0 0 10 0',
            cls: 'panel-shadow'
        },
        {
            xtype: 'tabpanel',
            reference: 'detailTab',
            margin: '7 20 20 20',
            height: 343,
            cls: 'panel-shadow',
            frame: true,
            border: true,
            items: [
                {
                    title: message.msg('monitoring.history.msg.mr_job_sum'),
                    xtype: 'jobSummary',
                    padding: '0 0 10 0'
                },
                {
                    title: message.msg('monitoring.history.msg.mr_job_counter'),
                    xtype: 'panel',
                    layout: 'fit',
                    items: [
                        {
                            xtype: 'jobCounters',
                            reference: 'jobCounters',
                            padding: '0 0 10 0'
                        }
                    ]
                },
                {
                    title: message.msg('monitoring.history.msg.mr_job_cofig'),
                    xtype: 'mapReduceConfiguration',
                    padding: '0 0 10 0'
                },
                {
                    xtype: 'tasks',
                    title: message.msg('monitoring.history.msg.mr_job_task'),
                    reference: 'tasksTree',
                    padding: '0 0 10 0'
                }
            ],
            listeners: {
                tabchange: 'onTabChanged'
            }
        }
    ],

    listeners: {
        afterrender: 'onAfterrender'
    }
});