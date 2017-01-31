Ext.define('Flamingo.component.skins.SkinsModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.skins',

    stores: {
        skin: {
            fields: ['key', 'value'],
            data: [{
                key: 'basic', value: 'Default'
            },{
                key: 'navyblue', value: 'Navyblue'
            }]
        }
    }
});