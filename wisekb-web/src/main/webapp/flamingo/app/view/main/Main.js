/*
 * Copyright 2012-2016 the Flamingo Community.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*Ext.define('Flamingo.view.main.Main', {
    extend: 'Ext.container.Viewport',
    xtype: 'app-main',

    requires: [
        'Flamingo.view.main.MainController',
        'Flamingo.view.main.MainModel'
    ],

    controller: 'main',
    viewModel: 'main',

    layout: 'border',

    items: [{
        xtype: 'component',
        region: 'north',
        height: 80,
        cls: 'header',
        html: '<div class="logo"></div>'
    },{
        xtype: 'dataview',
        region: 'west',
        bind: {
            store: '{menu}'
        },
        width: 260,
        cls: 'nav',
        itemSelector: 'li',
        tpl: [
            '<ul>',
            '<tpl for=".">',
                '<li class="depth_1">{text}<span class="icon"><i class="{icon}" aria-hidden="true"></i></span>',
            '</tpl>',
            '</ul>'
        ],
        listeners: {
            select: 'onSelect'
        }
    },{
        xtype: 'container',
        reference: 'mainContainer',
        region: 'center',
        flex: 1,
        layout: 'fit'
    },{
        xtype: 'component',
        region: 'south',
        height: 20
    }]

});*/

Ext.define('Flamingo.view.main.Main', {
    extend: 'Ext.container.Viewport',

    requires: [
        'Flamingo.view.hadoop.mapreduce.MapReduce',
        'Flamingo.view.hadoop.yarnapplication.YarnApplication',
        'Flamingo.store.NavigationTree',
        'Ext.button.Segmented',
        'Ext.list.Tree'
    ],

    controller: 'main',
    viewModel: 'main',

    cls: 'sencha-dash-viewport',
    itemId: 'mainView',

    layout: {
        /*type: 'vbox',
        align: 'stretch'*/
        type: 'border'
    },

    listeners: {
        render: 'onMainViewRender'
    },

    items: [
        {
            xtype: 'toolbar',
            cls: 'headerbar',
            border: false,
            region: 'north',
            height: 80,
            itemId: 'headerBar',
            items: [
                {
                    xtype: 'component',
                    reference: 'senchaLogo',
                    html: '<div class="header-logo"></div>'
                },
                '->',
                {
                    xtype: 'button',
                    iconCls: 'fa fa-bars fa-lg',
                    scale: 'large',
                    ui: 'header',
                    tooltip: 'Collapse/Expand',
                    margin: '0 20 0 0',
                    style: {
                        color: '#D1D1D1'
                    },
                    handler: 'onToggleNavigationSize'
                },
                {
                    xtype: 'button',
                    iconCls: 'fa fa-sign-out fa-lg',
                    scale: 'large',
                    ui: 'header',
                    tooltip: 'Logout',
                    margin: '0 20 0 0',
                    style: {
                        color: '#D1D1D1'
                    },
                    handler: 'onLogoutClick'
                }
            ]
        },
        {
            xtype: 'maincontainerwrap',
            id: 'main-view-detail-wrap',
            reference: 'mainContainerWrap',
            region: 'center',
            flex: 1,
            items: [
                {
                    xtype: 'container',
                    reference: 'navigationContainer',
                    itemId: 'navigationContainer',
                    scrollable: 'y',
                    width: 250,
                    /*layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },*/
                    items: [
                        {
                            xtype: 'treelist',
                            ui: 'treemenu',
                            cls: 'flamingo-tree-menu',
                            reference: 'navigationTreeList',
                            itemId: 'navigationTreeList',
                            store: Ext.create('Flamingo.store.NavigationTree'),
                            width: 250,
                            expanderFirst: false,
                            expanderOnly: false,
                            singleExpand: true,
                            listeners: {
                                selectionchange: 'onNavigationTreeSelectionChange'
                            }
                        }
                    ]
                },
                {
                    xtype: 'container',
                    flex: 1,
                    //reference: 'mainCardPanel',
                    reference: 'centerPanel',
                    cls: 'sencha-dash-right-main-container',
                    itemId: 'contentPanel',
                    layout: {
                        type: 'fit'
                    }
                }
            ]
        }
    ]
});
