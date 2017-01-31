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
Ext.define('Flamingo.view.hadoop.yarnapplication.YarnApplication', {
    extend: 'Ext.panel.Panel',
    xtype: 'yarnapplication',

    requires: [
        'Flamingo.view.hadoop.yarnapplication.YarnApplicationController',
        'Flamingo.view.hadoop.yarnapplication.YarnApplicationModel',
        'Flamingo.view.hadoop.yarnapplication.AllApplications',
        'Flamingo.view.hadoop.yarnapplication.ApplicationSummary',
        'Flamingo.view.hadoop.yarnapplication.TypesChart',
        'Flamingo.component.editor.AbstractEditor'
    ],

    controller: 'yarnApplicationController',
    viewModel: 'yarnApplicationModel',

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
            html: '<h2 style="padding: 0; margin:20px 0 0 20px;">YARN (Yet Another Resource Negotiator)</h2>'
        },
        {
            xtype: 'container',
            margin: '13 13 0 13',
            height: 200,
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [{
                xtype: 'yarnapplicationTypes',
                title: 'YARN Application Summary',
                flex: 1,
                margin: 7,
                layout: 'fit',
                style: {
                    'box-shadow': '0 0 7px rgba(0, 0, 0, 0.2)'
                }
            }]
        },
        {
            xtype: 'allApplications',
            flex: 1,
            minHeight: 210,
            margin: '7 20 7 20',
            padding: '0 0 10 0',
            cls: 'panel-shadow'
        },
        {
            xtype: 'tabpanel',
            reference: 'yarnAppTab',
            height: 350,
            margin: '7 20 20 20',
            cls: 'panel-shadow',
            items: [
                {
                    title: 'Summary',
                    xtype: 'applicationSummary',
                    padding: '0 0 10 0'
                },
                {
                    xtype: 'abstractEditor',
                    title: 'Log',
                    tooltip: 'Shows the logs of every container that executed applications.',
                    layout: 'fit',
                    reference: 'logviewer',
                    parser: 'plain_text',
                    highlightActiveLine: false,
                    highlightGutterLine: false,
                    highlightSelectedWord: false,
                    forceFit: true,
                    theme: 'eclipse',
                    printMargin: false,
                    readOnly: true,
                    value: ''
                }
            ],
            tabBar: {
                items: [
                    {
                        xtype: 'tbspacer',
                        flex: 1
                    },
                    {
                        xtype: 'splitbutton',
                        text: 'View',
                        margin: '0 0 7 0',
                        menu: [
                            {
                                text: 'Download application log',
                                iconCls: 'fi icon-fm-download',
                                handler: 'onDownloadLogClick'
                            }, '-',
                            {
                                text: 'View application log in full page',
                                iconCls: 'fi icon-fm-document-search',
                                handler: 'onShowApplicationLogClick'
                            }
                        ]
                    }

                ]
            },
            listeners: {
                tabchange: 'onTabChanged'
            }
        }
    ],

    listeners: {
        afterrender: 'onAfterrender'
    }
});