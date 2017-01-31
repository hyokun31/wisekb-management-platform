Ext.define('Flamingo.model.hadoop.mapreduce.JobCounters', {
    extend: 'Ext.data.TreeModel',
    fields: [
        {
            name: 'name', mapping: 'name', type: 'string'
        },
        {
            name: 'mapCounterValue', convert: convertComma
        },
        {
            name: 'reduceCounterValue', convert: convertComma
        },
        {
            name: 'totalCounterValue', convert: convertComma
        }
    ]
});