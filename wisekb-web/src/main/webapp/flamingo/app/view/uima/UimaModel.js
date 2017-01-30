Ext.define('Flamingo.view.uima.UimaModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.uima',

    stores: {
        uimagrid: {
            fields:[
                {
                    name: 'log_process_id',
                    type: 'integer',
                    mapping: 'log_process_id'
                },
                {
                    name: 'log_process_type',
                    type: 'string',
                    mapping: 'log_process_type'
                },
                {
                    name: 'log_level',
                    type: 'string',
                    mapping: 'log_level'
                },
                {
                    name: 'log_collection_reader',
                    type: 'string',
                    mapping: 'log_collection_reader'
                },
                {
                    name: 'log_ip',
                    type: 'string',
                    mapping: 'log_ip'
                },
                {
                    name: 'log_annotator_type',
                    type: 'string',
                    mapping: 'log_annotator_type'
                },
                {
                    name: 'log_data',
                    type: 'string',
                    mapping: 'log_data'
                },
                {
                    name: 'log_date',
                    type: 'integer',
                    mapping: 'log_date',
                    convert: function (value) {
                        if (value !== null) {
                            return Ext.Date.format (new Date(value), 'Y-m-d H:i:s');
                        }
                    }
                } ],
            autoLoad: true,
            proxy: {
                type: 'ajax',
                url: '/uima/getUimaLog',
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'total',
                    keepRawData: true
                }
            }
        },
        uimaChart: {
            fields: [
                {
                    name: 'log_date',
                    type: 'integer',
                    mapping: 'log_date',
                    convert: function (value) {
                        if (value !== null) {
                            return Ext.Date.format (new Date(value), 'Y-m-d H:i');
                        }
                    }
                }, 
                {
                    name: 'log_count',
                    type: 'integer',
                    mapping: 'log_count'
                }],
            autoLoad: true,
            proxy: {
                type: 'ajax',
                url: '/uima/getCountByDate',
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'total',
                    keepRawData: true
                }
            }
        },
        uimaTimeline: {
            fields:[
                {
                    name: 'log_process_id',
                    type: 'integer',
                    mapping: 'log_process_id'
                },
                {
                    name: 'log_process_type',
                    type: 'string',
                    mapping: 'log_process_type'
                },
                {
                    name: 'log_level',
                    type: 'string',
                    mapping: 'log_level'
                },
                {
                    name: 'log_collection_reader',
                    type: 'string',
                    mapping: 'log_collection_reader'
                },
                {
                    name: 'log_ip',
                    type: 'string',
                    mapping: 'log_ip'
                },
                {
                    name: 'log_annotator_type',
                    type: 'string',
                    mapping: 'log_annotator_type'
                },
                {
                    name: 'log_data',
                    type: 'string',
                    mapping: 'log_data'
                },
                {
                    name: 'content',
                    type: 'string',
                    mapping: 'content'
                },
                {
                    name: 'start',
                    type: 'integer',
                    mapping: 'log_date',
                    convert: function (value) {
                        if (value !== null) {
                            return Ext.Date.format (new Date(value), 'Y-m-d H:i:s');
                        }
                    }
                } ],
            autoLoad: true,
            proxy: {
                type: 'ajax',
                url: '/uima/getTimeline',
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'total',
                    keepRawData: true
                }
            }
        },
        uimaAnnotatorType: {
            fields: [
                {
                    name: 'log_annotator_type',
                    type: 'string',
                    mapping: 'log_annotator_type'
                }],
            autoLoad: true,
            proxy: {
                type: 'ajax',
                url: '/uima/getAnnotatorType',
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'total',
                    keepRawData: true
                }
            }
        }

    }
});