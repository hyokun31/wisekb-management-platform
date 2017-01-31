Ext.define('Flamingo.model.hadoop.mapreduce.Tasks', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'elapsedTime', convert: convertTime
        },
        {
            name: 'finishTime', convert: convertDateTime
        },
        {
            name: 'id'
        },
        {
            name: 'jobTaskCounters'
        },
        {
            name: 'progress'
        },
        {
            name: 'startTime', convert: convertDateTime
        },
        {
            name: 'state'
        },
        {
            name: 'success'
        },
        {
            name: 'successfulAttempt'
        },
        {
            name: 'taskAttempts'
        },
        {
            name: 'type'
        }
    ]
});
