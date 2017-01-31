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
Ext.define('Flamingo.view.hadoop.yarnapplication.YarnApplicationController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.yarnApplicationController',

    requires: [
        'Flamingo.view.editor.AceEditor'
    ],

    onAfterrender: function() {
        var typesStore = this.getViewModel().getStore('types');

        typesStore.load();
    },

    /**
     * Yarn Application의 Summary 정보를 가져온다.
     *
     */
    onApplicationSumChartAfterRender: function () {
        var chart = query('applicationSumChart #appSumChart');

        /*setTimeout(function () {
            chart.getStore().load();
        }, 10);

        // Table Layout의 colspan 적용시 cell간 간격 조정이되지 않는 문제를 해결하기 위해서 적용함
        setTableLayoutFixed(chart);*/
    },

    /**
     * 모든 Yarn Application 정보를 가져온다.
     *
     */
    onAllApplicationsAfterRender: function () {
        var grid = query('allApplications');
        var me = this;

        //grid.setLoading(true);

        setTimeout(function () {
            grid.getStore().load({
                callback: function (records, operation, success) {
                    if (success) {
                        //grid.setLoading(false);
                        grid.setTitle(format(message.msg('monitoring.msg.all_yarn_app_total'), this.getCount()));
                        if(this.getCount() > 0){
                            grid.getSelectionModel().select(0);
                            me.onAllYarnAppGridItemClick();
                        }
                    } else {
                        //grid.setLoading(false);
                    }
                }
            });
        }, 10);
    },

    /**
     * Yarn Application의 Summary 정보를 업데이트한다.
     */
    onApplicationSumChartRefreshClick: function () {
        var me = this;

        me.onApplicationSumChartAfterRender();
    },

    /**
     * Yarn Application의 모든 Yarn Application 정보를 업데이트한다.
     */
    onAllApplicationRefreshClick: function () {
        var me = this;

        me.onAllApplicationsAfterRender();
    },

    onDownloadLogClick: function () {
        var grid = query('allApplications');
        var selection = grid.getSelectionModel().getSelection()[0];

        if (selection && (selection.get('yarnApplicationState') == 'FINISHED' ||
            selection.get('yarnApplicationState') == 'FAILED' ||
            selection.get('yarnApplicationState') == 'KILLED')) {

            var applicationId = selection.get('applicationId');

            Ext.core.DomHelper.append(document.body, {
                tag: 'iframe',
                id: 'testIframe' + new Date().getTime(),
                css: 'display:none;visibility:hidden;height:0px;',
                src: CONSTANTS.HADOOP.YARNAPPLICATION.APP_DOWNLOAD + '?applicationId=' + applicationId + '&appOwner=' + selection.data.user,
                frameBorder: 0,
                width: 0,
                height: 0
            });
        } else {
            Ext.MessageBox.show({
                title: message.msg('common.warn'),
                message: message.msg('monitoring.yarn.cannot_show_run_app_log'),
                buttons: Ext.MessageBox.OK,
                icon: Ext.MessageBox.WARNING
            });
        }
    },

    onShowApplicationLogClick: function () {
        var grid = query('allApplications');
        var selection = grid.getSelectionModel().getSelection()[0];

        if (selection) {
            var applicationId = selection.get('applicationId');

            if (selection.get('yarnApplicationState') == 'RUNNING') {
                Ext.MessageBox.show({
                    title: message.msg('common.warn'),
                    message: message.msg('monitoring.yarn.cannot_show_run_app_log'),
                    buttons: Ext.MessageBox.OK,
                    icon: Ext.MessageBox.WARNING
                });
            } else {
                var params = {
                    applicationId: applicationId,
                    appOwner: selection.data.user
                };

                invokeGet(CONSTANTS.HADOOP.YARNAPPLICATION.APP_LOG, params,
                    function (response) {
                        Ext.create('Ext.window.Window', {
                            title: message.msg('monitoring.yarn.app_log'),
                            modal: true,
                            width: window.innerWidth - 200,
                            height: window.innerHeight - 200,
                            layout: 'fit',
                            items: [
                                {
                                    border: true,
                                    layout: 'fit',
                                    xtype: 'aceEditor',
                                    parser: 'plain_text',
                                    highlightActiveLine: false,
                                    highlightGutterLine: false,
                                    highlightSelectedWord: false,
                                    forceFit: true,
                                    theme: 'eclipse',
                                    printMargin: false,
                                    readOnly: true,
                                    value: response.responseText
                                }
                            ]
                        }).center().show();
                    },
                    function () {
                        Ext.MessageBox.show({
                            title: message.msg('common.warn'),
                            message: format(message.msg('common.msg.server_error'), config['system.admin.email']),
                            buttons: Ext.MessageBox.OK,
                            icon: Ext.MessageBox.WARNING
                        });
                    }
                );
            }
        }
    },

    onShowApplicationMasterClick: function () {
        var grid = query('allApplications');
        var selection = grid.getSelectionModel().getSelection()[0];

        if (selection) {
            var url = selection.get('trackingUrl');

            // 애플리케이션 마스터 정보가 없으면 에러창을 표시한다.
            if (url && url == 'N/A') {
                Ext.MessageBox.show({
                    title: message.msg('common.warn'),
                    message: message.msg(''),
                    buttons: Ext.MessageBox.OK,
                    icon: Ext.MessageBox.WARNING
                });
            } else {
                Ext.create('Ext.window.Window', {
                    title: message.msg('monitoring.application_master'),
                    modal: false,
                    maximizable: true,
                    resizable: true,
                    width: 800,
                    height: 600,
                    layout: 'fit',
                    items: [
                        {
                            xtype: 'panel',
                            flex: 1,
                            closable: false,
                            showCloseOthers: false,
                            showCloseAll: false,
                            type: 'help',
                            forceFit: true,
                            printMargin: true,
                            html: '<iframe style="overflow:auto;width:100%;height:100%;" frameborder="0"  src="' + url + '"></iframe>',
                            border: false,
                            autoScroll: true
                        }
                    ]
                }).center().show();
            }
        }
    },

    /**
     * Yarn Application에서 모든 Yarn Application 그리드에서 선택한 아이템의 정보를 가져온다.
     */
    onAllYarnAppGridItemClick: function()  {
        var tabpanel = this.getReferences().yarnAppTab;
        this.onTabChanged(tabpanel, null);
    },

    /**
     * Yarn Application에서 모든 Yarn Application 그리드에서 선택한 아이템에 해당하는 각각의 탭 정보를 가져온다.
     *
     * @param tabPanel
     * @param tab
     */
    onTabChanged: function (tabPanel, tab) {
        var me = this;
        var grid = query('allApplications');
        var viewModel = me.getViewModel();
        var selection = grid.getSelectionModel().getSelection()[0];
        var refs = this.getReferences();
        if (selection) {
            var applicationId = selection.data.applicationId;
            var activeTab = tabPanel.getActiveTab();
            var activeTabIndex = tabPanel.items.findIndex('id', activeTab.id);

            switch (activeTabIndex) {
                case 0:

                    var summaryStore = viewModel.getStore('summary');

                    summaryStore.load({
                        params: {
                            applicationId: applicationId
                        }
                    });

                    break;
                case 1:
                    var logviewer = refs.logviewer;

                    if (selection.data.yarnApplicationState == 'RUNNING') {
                        logviewer.setValue(message.msg(''));
                    } else {
                        // 일단 선택하면 로그 패널의 내용을 모두 삭제한다.
                        logviewer.setValue('');

                        var params = {
                            applicationId: applicationId,
                            appOwner: selection.data.user
                        };

                        // 서버를 호출하여 애플리케이션 로그를 로그 패널에 추가한다.
                        $.ajax({
                            method: "GET",
                            url: CONSTANTS.HADOOP.YARNAPPLICATION.APP_LOG,
                            data: params,
                            complete: function(response) {
                                refs.logviewer.setValue(response.responseText);
                            }
                        });

                    }
                    break;
                case 2:
                    if (selection.get('trackingUrl') && selection.get('trackingUrl') == 'N/A') {
                        query('yarnApplication #applicationMaster').body.update('');
                    } else {
                        var html = '<iframe style="overflow:auto;width:100%;height:100%;" frameborder="0" src="' + selection.get('trackingUrl') + '"></iframe>'
                        var panel = refs.applicationMaster;
                        panel.body.update(html);
                    }
                    break;
            }
        }
    },

    onRefreshClick: function (event, toolEl, panel) {
        var grid = query('allApplications');
        var chart = query('applicationSumChart > cartesian');
        grid.getStore().load({
            callback: function (records, operation, success) {
                grid.setTitle(format(message.msg('monitoring.msg.all_yarn_app_total'), this.getCount()))
            }
        });

        chart.getStore().load();
    },

    onItemContextMenu: function (grid, record, item, index, event) {
        var me = this;
        var applicationId = record.get('applicationId');

        event.stopEvent();

        if (me.contextMenu) {
            me.contextMenu.close();
            me.contextMenu = undefined;
        }

        me.contextMenu = new Ext.menu.Menu({
            items: [
                {
                    text: message.msg('common.kill'),
                    iconCls: 'common-kill',
                    handler: function () {
                        Ext.MessageBox.show({
                            title: message.msg('monitoring.application_kill'),
                            message: message.msg('monitoring.yarn.msg.kill_app'),
                            buttons: Ext.MessageBox.YESNO,
                            icon: Ext.MessageBox.WARNING,
                            fn: function handler(btn) {
                                if (btn == 'yes') {
                                    var url = CONSTANTS.MONITORING.RM.APP_KILL;
                                    var params = {
                                        applicationId: record.get('applicationId')
                                    };

                                    invokeGet(url, params,
                                        function (response) {
                                            var obj = Ext.decode(response.responseText);
                                            if (obj.success) {
                                                Ext.MessageBox.show({
                                                    title: message.msg('common.confirm'),
                                                    message: message.msg('monitoring.yarn.msg.kill_app_finish'),
                                                    buttons: Ext.MessageBox.OK,
                                                    icon: Ext.MessageBox.INFO
                                                });
                                            } else {
                                                Ext.MessageBox.show({
                                                    title: message.msg('common.warn'),
                                                    message: obj.error.message,
                                                    buttons: Ext.MessageBox.OK,
                                                    icon: Ext.MessageBox.WARNING
                                                });
                                            }
                                        },
                                        function () {
                                            Ext.MessageBox.show({
                                                title: message.msg('common.warn'),
                                                message: format(message.msg('common.msg.server_error'), config['system.admin.email']),
                                                buttons: Ext.MessageBox.OK,
                                                icon: Ext.MessageBox.WARNING
                                            });
                                        }
                                    );
                                }
                            }
                        });
                    }
                }, '-',
                {
                    text: message.msg('monitoring.application_move'),
                    iconCls: 'common-import',
                    menu: {
                        items: Ext.create('Flamingo2.view.monitoring.applications.Queue', {
                            title: message.msg('monitoring.application_move_queue'),
                            height: 200,
                            width: 200,
                            listeners: {
                                afterrender: function (comp, eOpts) {
                                    comp.getStore().load({
                                        callback: function (records, operation, success) {
                                            comp.getRootNode().expand();
                                        }
                                    });
                                },
                                itemclick: function (view, record, item, index, e, eOpts) {
                                    me.contextMenu.close();
                                    me.contextMenu = undefined;

                                    var url = CONSTANTS.MONITORING.RM.APP_MOVE;
                                    var params = {
                                        applicationId: applicationId,
                                        queue: record.get('queue')
                                    };

                                    invokeGet(url, params,
                                        function (response) {
                                            var res = Ext.decode(response.responseText);
                                            if (res.success) {
                                                Ext.MessageBox.show({
                                                    title: message.msg('common.confirm'),
                                                    message: message.msg('monitoring.yarn.msg.app_moved'),
                                                    buttons: Ext.MessageBox.OK,
                                                    icon: Ext.MessageBox.INFO
                                                });
                                            } else {
                                                Ext.MessageBox.show({
                                                    title: message.msg('common.warn'),
                                                    message: obj.error.message,
                                                    buttons: Ext.MessageBox.OK,
                                                    icon: Ext.MessageBox.WARNING
                                                });
                                            }
                                        },
                                        function (response) {
                                            Ext.MessageBox.show({
                                                title: message.msg('common.warn'),
                                                message: format(message.msg('common.msg.server_error'), config['system.admin.email']),
                                                buttons: Ext.MessageBox.OK,
                                                icon: Ext.MessageBox.WARNING
                                            });
                                        }
                                    );
                                }
                            }
                        })
                    }
                }
            ]
        });

        if (record.get('yarnApplicationState') == 'RUNNING') {
            me.contextMenu.showAt(event.pageX - 5, event.pageY - 5);
        }
    },

    /**
     * 실행 중인 Yarn Application을 종료한다.
     *
     * @param button
     * @param event
     */
    onYarnApplicationKillClick: function (button, event) {
        var record = button['_rowContext'].record;

        if (record.data.yarnApplicationState == 'RUNNING') {
            var applicationId = record.data.applicationId;
            var params = {
                applicationId: applicationId
            };
            Ext.MessageBox.show({
                title: message.msg('monitoring.application_kill'),
                message: message.msg('monitoring.msg.application_kill'),
                buttons: Ext.MessageBox.YESNO,
                icon: Ext.MessageBox.WARNING,
                fn: function handler(btn) {
                    if (btn == 'yes') {
                        invokeGet(CONSTANTS.HADOOP.YARNAPPLICATION.APP_KILL, params,
                            function (response) {
                                var obj = Ext.decode(response.responseText);
                                if (obj.success) {
                                    Ext.MessageBox.show({
                                        title: message.msg('common.confirm'),
                                        message: message.msg('monitoring.yarn.msg.kill_app_finish'),
                                        buttons: Ext.MessageBox.OK,
                                        icon: Ext.MessageBox.INFO
                                    });
                                } else {
                                    Ext.MessageBox.show({
                                        title: message.msg('common.warn'),
                                        message: obj.error.message,
                                        buttons: Ext.MessageBox.OK,
                                        icon: Ext.MessageBox.WARNING
                                    });
                                }
                            },
                            function () {
                                Ext.MessageBox.show({
                                    title: message.msg('common.warn'),
                                    message: format(message.msg('common.msg.server_error'), config['system.admin.email']),
                                    buttons: Ext.MessageBox.OK,
                                    icon: Ext.MessageBox.WARNING
                                });
                            }
                        );
                    }
                }
            });

        }
    }
});