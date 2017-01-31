Ext.define('Flamingo.component.skins.Skins', {
    extend: 'Ext.window.Window',
    xtype: 'skins',

    requires: [
        'Flamingo.component.skins.SkinsController',
        'Flamingo.component.skins.SkinsModel'
    ],

    controller: 'skins',
    viewModel: 'skins',

    bodyCls: 'fem-panel-popup',

    width: 220,
    height: 76,
    header: false,
    resizable: false,

    layout: 'fit',

    items: [{
        xtype: 'dataview',
        bind: {
            store: '{skin}'
        },

        tpl: [
            '<tpl for=".">',
            '   <div class="thumb-wrap fem-skin fem-skin-{key}">',
            '   <span>',
            '       <span>{value}</span>',
            '   </span>',
            '   </div>',
            '</tpl>'
        ],

        itemSelector: 'div.thumb-wrap',

        listeners: {
            itemdblclick: 'onItemdblclick'
        }
    }]
});