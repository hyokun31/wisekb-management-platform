Ext.define('portlet.ContainerColumn', {
    extend: 'Ext.panel.Panel',
    xtype: 'containerColumn',

    cls: 'portlet-column',

    config: {
        columnType: 'container'
    },

    layout: 'fit'
});