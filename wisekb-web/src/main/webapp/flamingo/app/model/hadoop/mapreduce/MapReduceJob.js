Ext.define('Flamingo.model.hadoop.mapreduce.MapReduceJob', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id'
        },
        {
            name: 'mapsCompleted'
        },
        {
            name: 'reducesCompleted'
        },
        {
            name: 'user'
        },
        {
            name: 'name'
        },
        {
            name: 'queue'
        },
        {
            name: 'state'
        },
        {
            name: 'reducesTotal'
        },
        {
            name: 'mapsTotal'
        },
        {
            name: 'startTime',
            convert: function (value) {
                return dateFormat2(value);
            }
        },
        {
            name: 'finishTime',
            convert: function (value) {
                return dateFormat2(value);
            }
        }
    ]
});