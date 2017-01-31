Ext.define('Flamingo.store.NavigationTree', {
    extend: 'Ext.data.TreeStore',

    storeId: 'NavigationTree',

    fields: [{
        name: 'text'
    }],

    root: {
        expanded: true,
        children: [
            {
                text: 'Workflow Designer',
                iconCls: 'fa fa-pencil-square-o',
                viewType: 'designer',
                leaf: true
            },
            {
                text: 'Apache Hadoop',
                iconCls: 'fa fa-database',
                children: [
                    {
                        text: 'Yarn',
                        rowCls: 'row-has-noicon',
                        viewType: 'yarnapplication',
                        leaf: true
                    },
                    {
                        text: 'MapReduce',
                        rowCls: 'row-has-noicon',
                        viewType: 'mapreduce',
                        leaf: true
                    }
                ]
            },
            {
                text: 'Apache Oozie',
                iconCls: 'fa fa-area-chart',
                children: [
                    {
                        text: 'Timeline',
                        rowCls: 'row-has-noicon',
                        viewType: 'ooziedashboard',
                        leaf: true
                    },
                    {
                        text: 'Workflow',
                        rowCls: 'row-has-noicon',
                        viewType: 'oozieworkflow',
                        leaf: true
                    },
                    {
                        text: 'Coordinator',
                        rowCls: 'row-has-noicon',
                        viewType: 'ooziecoordinator',
                        leaf: true
                    },
                    {
                        text: 'Bundle',
                        rowCls: 'row-has-noicon',
                        viewType: 'ooziebundle',
                        leaf: true
                    },
                    {
                        text: 'System Information',
                        rowCls: 'row-has-noicon',
                        viewType: 'ooziesysteminfo',
                        leaf: true
                    }
                ]
            },
            {
                text: 'HDFS Browser',
                iconCls: 'fa fa-file-text-o',
                viewType: 'hdfsbrowser',
                leaf: true
            },
            {
                text: 'S3 Browser',
                iconCls: 'fa fa-file-text-o',
                viewType: 's3browser',
                leaf: true
            },
            {
                text: 'UIMA',
                iconCls: 'fa fa-file-text-o',
                viewType: 'uima',
                leaf: true
            }
        ]
    }

});
