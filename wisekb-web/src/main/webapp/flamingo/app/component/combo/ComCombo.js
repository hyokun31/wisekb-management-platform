Ext.define('Flamingo.component.combo.ComCombo', {
    extend: 'Ext.form.field.ComboBox',
    xtype: 'comcombo',

    requires: [
        'Flamingo.component.combo.ComboModel',
        'Flamingo.component.combo.ComComboController'
    ],

    controller: 'comcombo',
    viewModel: 'combo',

    displayField: 'code_nm',
    valueField: 'code_id',
    queryMode: 'local',
    editable: false,

    code: null,

    bind: {
        store: '{comcombo}'
    },

    listeners: {
        afterrender: 'onAfterrender'
    }
});