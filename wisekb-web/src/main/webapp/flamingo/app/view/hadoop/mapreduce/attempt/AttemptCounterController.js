Ext.define('Flamingo.view.hadoop.mapreduce.attempt.AttemptCounterController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.attemptcounter',

    onAfterrender: function(view) {
        view.setTitle('Job ID: ' + view.jobId + ' / Task ID: ' + view.taskId);
    },

    onBeforeload: function(store) {
        var me = this,
            view = me.getView();

        store.proxy.extraParams.jobId = view.jobId;
        store.proxy.extraParams.taskId = view.taskId;
    }
});