/**
 * Created by Park on 15. 8. 26..
 */
Ext.define('Flamingo.view.hadoop.mapreduce.timeline.MRTimeline', {
    extend: 'Ext.window.Window',

    requires: [
        'Flamingo.view.hadoop.mapreduce.timeline.MRTimelineController',
        'Flamingo.view.hadoop.mapreduce.timeline.TimelineModel',
        'Flamingo.view.oozie.dashboard.monitoring.Timeline',
        'Flamingo.view.hadoop.mapreduce.attempt.AttemptCounter'
    ],

    controller: 'mrtimeline',
    viewModel: 'mapreducetimeline',

    maximizable: true,
    modal: true,
    scrollable: true,
    bodyStyle: {
        background: '#FFFFFF'
    },

    items: [{
        xtype: 'timeline',
        reference: 'timlineView',
        width: '100%',
        bind: {
            store: '{mrTimeline}'
        },
        options: {
            editable: false,
            align: 'center',
            selectable: true,
            showCurrentTime: false,
            zoomable: false,
            type: 'range'
        }
    }],
    listeners: {
        afterrender: 'onAfterrender',
        beforeclose: 'onBeforeclose',
        timelineSelect: 'onTimelineSelect'
    }
});