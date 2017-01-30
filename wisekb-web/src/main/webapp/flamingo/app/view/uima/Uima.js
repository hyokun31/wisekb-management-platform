Ext.define('Flamingo.view.uima.Uima', {
    extend: 'Ext.panel.Panel',
    xtype: 'uima',

    requires: [
        'Flamingo.view.uima.UimaController',
        'Flamingo.view.uima.UimaModel',
        'Flamingo.model.oozie.dashboard.monitoring.Timeline'
    ],

    viewModel: 'uima',
    controller: 'uima',

    layout: 'border',


    items : [{
        xtype: 'panel',
        region: 'center',
        flex: 3,
        
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        
        items : [{
            xtype: 'panel',
            title: 'timeline',
            layout: 'fit',
            flex: 2,
            items: [{
                xtype: 'timeline',
                reference: 'timelineView',
                scrollable: true,
                flex: 1,
                bind: {
                    store: '{uimaTimeline}'
                },
                options: {
                    zoomable: true,
                    editable: false,
                    showCurrentTime: true,
                    align: 'center'
                }
            }]
        }, {
            xtype: 'cartesian',
            title: 'UIMA 로그통계',
            flex: 1,
            bind: {
                store: '{uimaChart}'
            },
            axes: [{
                type: 'numeric',
                position: 'left',
                fields: ['log_count'],
                minimum: 0
            },{
                type: 'category',
                position: 'bottom',
                fields: ['log_date'],
                grid: true
            }],

            series: [{
                type: 'area',
                xField: 'log_date',
                yField: 'log_count'
            }]
        },{
            xtype: 'grid',
            title: 'UIMA 로그',
            flex: 1,
            bind: {
                store: '{uimagrid}'
            },
            columns: [
                { text: '프로세스 ID', dataIndex: 'log_process_id' },
                { text: '프로세스 유형', dataIndex: 'log_process_type' },
                { text: '로깅레벨', dataIndex: 'log_level' },
                { text: '콜랙션 리더', dataIndex: 'log_collection_reader'},
                { text: 'IP', dataIndex: 'log_ip'},
                { text: 'Annotator 유형', dataIndex: 'log_annotator_type'},
                { text: '데이터', dataIndex: 'log_data', flex: 1},
                { text: '날짜', dataIndex: 'log_date'}
            ]
        }]
    }]

});