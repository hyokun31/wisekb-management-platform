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
Ext.define('Flamingo.view.hadoop.mapreduce.MapReduceController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.mapReduceController',

    onAfterrender: function() {
        var me = this,
            viewModel = me.getViewModel(),
            store = viewModel.getStore('mapReduceSum');

        store.load();
    },

    /**
     * 완료된 MapReduce Job 정보를 가져온다.
     *
     * @param grid
     * @param opts
     */
    onCompletedMRJobAfterRender: function (grid, opts) {
        var me = this;

        setTimeout(function () {
            grid.getStore().load({
                callback: function (records, operation, success) {
                    if (success) {
                        //grid.setLoading(false);
                        grid.setTitle(format(message.msg('monitoring.history.msg.finished_total'), this.getCount()));
                        if(this.getCount() > 0){
                            grid.getSelectionModel().select(0);
                            me.onMRJobGridItemClick();
                        }
                    } else {
                        //grid.setLoading(false);
                    }
                }
            });
        }, 10);
    },

    /**
     * 실행이 완료된 MapReduce Job 통계 정보를 업데이트한다.
     */
    onMRJobSumChartRefreshClick: function () {
        var me = this;

        me.onMapReduceSumChartAfterRender();
    },

    /**
     * MapReduce에서 선택한 각 탭의 정보를 가져온다.
     *
     * @param tabPanel
     * @param tab
     */
    onTabChanged: function (tabPanel, tab) {
        var me = this,
            refs = me.getReferences(),
            viewModel = me.getViewModel(),
            selection = refs.mapReduceJobGrid.getSelectionModel().getSelection()[0];

        if (selection) {
            var jobId = selection.get('id');
            var state = selection.get('state');
            var activeTab = tabPanel.getActiveTab();
            var activeTabIndex = tabPanel.items.findIndex('id', activeTab.id);

            switch (activeTabIndex) {
                case 0:
                    viewModel.getStore('jobSummaryStore').load({
                        params: {
                            jobId: jobId,
                            state: state
                        }
                    });
                    break;
                case 1:
                    viewModel.getStore('jobCounterStore').load({
                        params: {
                            jobId: jobId,
                            state: state
                        }
                    });
                    break;
                case 2:
                    var configurationGrid = query('mapReduceConfiguration');
                    configurationGrid.store.load({
                        params: {
                            jobId: jobId,
                            state: state
                        }
                    });
                    break;
                case 3:
                    viewModel.get('tasksStore').load();
                    break;
            }
        }
    },

    /**
     * 완료된 MapReduce Job 그리드에서 선택한 아이템의 상세 정보를 활성화된 탭 필드에 보여준다.
     */
    onMRJobGridItemClick: function () {
        var me = this,
            refs = me.getReferences();

        me.onTabChanged(refs.detailTab, null);
    },

    /**
     * 완료된 MapReduce Job 정보를 업데이트한다.
     */
    onCompletedMRJobRefreshClick: function () {
        var mapReduceJobsGrid = query('mapReduceJobs');

        //mapReduceJobsGrid.setLoading(true);
        mapReduceJobsGrid.getStore().load({
            callback: function (records, operation, success) {
                if (success) {
                    //mapReduceJobsGrid.setLoading(false);
                    mapReduceJobsGrid.setTitle(format(message.msg('monitoring.history.msg.finished_total'), this.getCount()));
                } else {
                    //mapReduceJobsGrid.setLoading(false);
                }
            }
        });
    },

    onLogClick: function(button, event) {
        var me = this,
            refs = me.getReferences(),
            selection = refs.mapReduceJobGrid.getSelectionModel().getSelection(),
            record = button['_rowContext'].record,
            attemptId;

        if( record.get('state') == 'SUCCEEDED') {
            attemptId = record.get('successfulAttempt')
        }
        else {
            attemptId = record.get('id').replace('task', 'attempt');
        }
    },

    onTimelineClick: function() {
        var me = this,
            refs = me.getReferences(),
            selection = refs.mapReduceJobGrid.getSelectionModel().getSelection();

        if (selection.length == 0) {
            error('Warning', 'Please select a Job to view timeline ');
            return;
        }

        Ext.create('Flamingo.view.hadoop.mapreduce.timeline.MRTimeline', {
            width: $(window).width() * 0.85,
            height: $(window).height() * 0.85,
            jobId: selection[0].get('id')
        }).show();
    },

    onViewLogClick: function(gird, rowIndex, colIndex) {
        var me = this,
            refs = me.getReferences(),
            selection = refs.mapReduceJobGrid.getSelectionModel().getSelection(),
            record = gird.getStore().getAt(rowIndex);

        Ext.create('Flamingo.view.hadoop.mapreduce.log.TaskLog', {
            jobId: selection[0].get('id'),
            attemptId: record.get('id'),
            width: window.innerWidth - 200,
            height: window.innerHeight - 200
        }).show();
    },

    onTasksBeforeload: function(store) {
        var me = this,
            refs = me.getReferences(),
            selection = refs.mapReduceJobGrid.getSelectionModel().getSelection()[0];

        if (!selection) return true;

        store.proxy.extraParams.jobId = selection.get('id');
        store.proxy.extraParams.state = selection.get('state');
    }

});
