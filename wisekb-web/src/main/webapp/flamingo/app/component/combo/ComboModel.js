Ext.define('Flamingo.component.combo.ComboModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.combo',

    stores: {
        comcombo: {
            fields: ['code_id', 'code_nm'],
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: CONSTANTS.SYSTEM.CODE.SELECT,
                headers: {'Accept': 'application/json'},
                reader: {
                    type: 'json',
                    rootProperty: 'list',
                    totalProperty: 'total'
                }
            }
        }
    }
});