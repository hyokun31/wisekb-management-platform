Ext.define('Flamingo.view.hadoop.mapreduce.attempt.AttemptCounter', {
    extend: 'Ext.window.Window',

    requires: [
        'Flamingo.view.hadoop.mapreduce.attempt.AttemptCounterController',
        'Flamingo.view.hadoop.mapreduce.attempt.AttemptCounterModel'
    ],

    modal: true,
    resizable: false,
    width: 800,
    height: 600,

    controller: 'attemptcounter',
    viewModel: 'attemptcounter',

    layout: 'fit',

    items: [{
        xtype: 'treepanel',
        rootVisible: false,
        columnLines: true,
        bind: {
            store: '{counter}'
        },
        columns: [{
            xtype: 'treecolumn', text: 'ID', dataIndex: 'id', align: 'left', flex: 1, style: 'text-align: center;'
        },{
            text: 'Status', dataIndex: 'state', align: 'center'
        },{
            text: 'Value', dataIndex: 'value', align: 'right', style: 'text-align: center;'
        }]
    }],

    listeners: {
        afterrender: 'onAfterrender'
    }
});