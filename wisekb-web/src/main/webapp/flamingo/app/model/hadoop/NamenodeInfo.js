Ext.define('Flamingo.model.hadoop.NamenodeInfo', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'hostName'},
        {name: 'underReplicatedBlocks'},
        {name: 'blocksTotal'},
        {name: 'dead'},
        {name: 'used'},
        {name: 'jvmUsedMemory'},
        {name: 'capacityRemainingPercent'},
        {name: 'pendingReplicationBlocks'},
        {name: 'total'},
        {name: 'stale'},
        {name: 'missingBlocks'},
        {name: 'decommissioning'},
        {name: 'defaultBlockSize'},
        {name: 'totalLoad'},
        {name: 'jvmTotalMemory'},
        {name: 'startTime'},
        {name: 'totalFiles'},
        {name: 'free'},
        {name: 'jvmMaxMemory'},
        {name: 'live'},
        {name: 'totalBlocks'},
        {name: 'all'},
        {name: 'capacityUsedPercent'},
        {name: 'corruptReplicatedBlocks'},
        {name: 'capacityUsed'},
        {name: 'threads'},
        {name: 'scheduledReplicationBlocks'},
        {name: 'jvmFreeMemory'},
        {name: 'blockCapacity'},
        {name: 'capacityRemaining'},
        {name: 'port'},
        {name: 'capacityTotal'},
        {name: 'capacityUsedNonDFS'}
    ],

    proxy: {
        type: 'ajax',
        url: CONSTANTS.HADOOP.NAMENODE.INFO,
        headers: {'Accept': 'application/json'},
        reader: {
            type: 'json',
            keepRawData: true,
            rootProperty: 'map'
        }
    },

    constructor: function() {
        this.callParent(arguments);

    }
});


































