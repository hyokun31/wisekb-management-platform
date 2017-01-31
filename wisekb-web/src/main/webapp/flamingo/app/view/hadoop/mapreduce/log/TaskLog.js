Ext.define('Flamingo.view.hadoop.mapreduce.log.TaskLog', {
    extend: 'Ext.window.Window',

    requires: [
        'Flamingo.view.hadoop.mapreduce.log.TaskLogController'
    ],

    controller: 'tasklog',
    width: window.innerWidth - 200,
    height: window.innerHeight - 200,
    modal: true,
    resizable: false,

    layout: 'fit',

    items: [{
        xtype: 'aceEditor',
        reference: 'ace'
    }],

    listeners: {
        afterrender: 'onAfterrender'
    }
});