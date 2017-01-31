Ext.define('Flamingo.view.hadoop.mapreduce.log.TaskLogController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.tasklog',

    onAfterrender: function(view) {
        var me = this,
            refs = me.getReferences();

        view.setTitle(view.jobId + ' ( ' + view.attemptId + ' )');

        $.ajax({
            method: 'GET',
            url: CONSTANTS.HADOOP.MAPREDUCE.LOG,
            data: {
                jobId: view.jobId,
                attemptId: view.attemptId
            },
            complete: function(jqXHR, textStatus) {
                refs.ace.setValue(jqXHR.responseText);
            }
        });
    }

});