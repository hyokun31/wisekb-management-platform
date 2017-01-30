/**
 * Created by seungmin on 2016. 8. 29..
 */

Ext.define('Flamingo.view.oozie.coordinator.CoordinatorWorkflowActionList', {
    extend: 'Ext.grid.Panel',
    xtype: 'coordinatorworkflowactionlist',

    columns: [
        {xtype: 'rownumberer', width: 40},
        {text: "Action ID", width: 340, dataIndex: 'jobId', align: 'center', menuDisabled: true},
        {text: "Name", dataIndex: 'app_name', minWidth: 180, flex: 2, style: 'text-align:center', menuDisabled: true},
        {text: "Type", dataIndex: 'type', flex: 1, minWidth: 140, align: 'center', menuDisabled: true},
        {text: "Status", dataIndex: 'status', width: 160, style: 'text-align:center', renderer: 'statusRenderer', menuDisabled: true},
        {text: "Transition", dataIndex: 'transition', flex: 1, minWidth: 80, align: 'center', menuDisabled: true},
        {text: "Duration", dataIndex: 'duration', flex: 1, minWidth: 80, align: 'center', menuDisabled: true},
        {text: "Started", dataIndex: 'start_time', flex: 1, minWidth: 135, align: 'center', menuDisabled: true},
        {text: "Ended", dataIndex: 'end_time', flex: 1, minWidth: 135, align: 'center', menuDisabled: true},
        {text: "Workflow Job Name", dataIndex: 'wf_app_name', hidden:true, menuDisabled: true},
        {text: "work app path", dataIndex: 'wf_app_path', hidden:true, menuDisabled: true},
        {text: "work parent", dataIndex: 'wf_parent', hidden:true, menuDisabled: true}
        
    ],
    columnLines: true,
    listeners: {
        itemdblclick: 'onWorkflowActionListRowDblClick'
    }
});


