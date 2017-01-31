/**
 * Created by Park on 15. 8. 26..
 */
Ext.define('Flamingo.view.hadoop.mapreduce.timeline.MRTimelineController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.mrtimeline',

    onAfterrender: function(view) {
        var me = this,
            refs = me.getReferences(),
            viewModel = me.getViewModel(),
            store = viewModel.getStore('mrTimeline');

        view.setTitle('MapReduce Task Timeline: ' + view.jobId);

        store.load({
            params: {
                jobId: view.jobId
            },
            callback: function() {
                Ext.defer(function() {
                    $("[data-toggle=tooltip]").tooltip({container: 'body'});

                    refs.timlineView.timeline.on('select', function(properties) {
                        view.fireEvent('timelineSelect', properties);
                    })
                }, 300);
            }
        });

    },

    onBeforeclose: function() {
        $('.ui-helper-hidden-accessible').remove();
        $('.tooltip.fade.top.in').remove();
    },

    onTimelineSelect: function(properties) {
        var me = this,
            view = me.getView(),
            viewModel = me.getViewModel(),
            store = viewModel.getStore('mrTimeline');
        if (properties.items.length == 0) return;

        Ext.create('Flamingo.view.hadoop.mapreduce.attempt.AttemptCounter', {
            jobId: view.jobId,
            taskId: store.getById(properties.items[0]).get('taskId')
        }).show();
    }
});