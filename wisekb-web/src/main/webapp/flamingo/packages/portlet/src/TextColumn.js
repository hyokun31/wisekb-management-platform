Ext.define('portlet.TextColumn', {
    extend: 'Ext.panel.Panel',
    xtype: 'textColumn',

    cls: 'portlet-column',

    requires: [
        'Ext.data.StoreManager',
        'Ext.data.Store'
    ],

    mixins: [
        'Ext.util.StoreHolder'
    ],
    
    config: {
        columnType: 'text',
        
        title: null,
        
        field: null,

        value: null
    },

    /*first, last*/
    dataBind: 'last',

    tpl: [
        '<div class="text-column">',
            '<div class="text-column-inner">',
                '<span class="text-column-span flex-font-widget">{value}</span>',
            '</div>',
        '</div>',
        '<span class="gs-resize-handle gs-resize-handle-both"></span>'
    ],

    initComponent: function() {
        var me = this,
            store;

        this.callParent(arguments);

        store = me.store = Ext.data.StoreManager.lookup(me.store || 'ext-empty-store');

        me.bindStore(store, true);
    },

    afterRender: function() {
        var me = this;

        me.callParent(arguments);
        
        me.tplEl = me.getEl().first();

        if (me.title)
            me.setTitle(me.title);

        if (me.value)
            me.setValue(me.value);

        Ext.defer(function() {
            me.textFill(me.getWidth());
        }, 300);
    },

    textFill: function(maxWidth) {
        var me = this;

        $('#' + me.id + ' > .text-column > .text-column-inner').textfill({
            minFontPixels: 14,
            maxFontPixels: 100,
            changeLineHeight: true,
            widthOnly: true,
            explicitWidth: maxWidth / 2.5
        });
    },

    onStoreLoad: function(store, records) {
        var me = this,
            record;
        if (me.getField()) {
            record = records[records.length - 1];

            me.update({title: me.getTitle(),value: record.get(me.getField())});

            me.textFill(me.getWidth());
        }
    },

    onSubscribe: function(callback, message) {
        //TODO Websoket 통신으로 수신받은 데이터 처리.
    },

    setValue: function(value) {
        console.debug(value);
        this.value = value;
        this.updateData('value', value);
    },

    updateData: function(key, value) {
        if (!this.data)
            this.data = {};

        this.data[key] = value;

        this.update(this.data, true);
    },

    setStore: function (newStore) {
        var me = this;

        if (me.store !== newStore) {
            if (me.isConfiguring) {
                me.store = newStore;
            } else {
                me.bindStore(newStore, /*initial*/ false);
                if (newStore.getData() && newStore.getData().items.length > 0) {
                    me.onStoreLoad(newStore, newStore.getData().items);
                }
            }
        }
    },

    getStoreListeners: function() {
        var me = this;
        return {
            load: me.onStoreLoad,
            subscribe: me.onSubscribe
        };
    }
});