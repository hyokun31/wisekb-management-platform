Ext.define('Flamingo.view.hadoop.mapreduce.timeline.TimelineModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.mapreducetimeline',

    stores: {
        mrTimeline: {
            model: 'Flamingo.model.spark.Timeline',
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.HADOOP.MAPREDUCE.TIMELINE,
                reader: {
                    type: 'json',
                    keepRawData: true,
                    rootProperty: 'list',
                    totalProperty: 'total'
                }
            }
        }
    }
});