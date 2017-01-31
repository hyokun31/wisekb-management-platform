Ext.define('Flamingo.view.hadoop.mapreduce.attempt.AttemptCounterModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.attemptcounter',

    stores: {
        counter: {
            type: 'tree',
            fields: ['id', 'map', 'reduce', {
                name: 'value',
                convert: convertComma
            }],
            autoLoad: false,
            rootVisible: false,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.MAPREDUCE.ATTEMPTS_COUNTER_TREE,
                extraParams: {}
            },
            root: {
                text: 'JobCounters',
                expanded: true,
                id: 'root'
            },
            listeners: {
                beforeload: 'onBeforeload'
            }
        }
    }

});