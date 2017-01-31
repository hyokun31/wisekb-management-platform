Ext.define('Flamingo.component.combo.ComComboController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.comcombo',

    onAfterrender: function(view) {
        var me = this,
            store = me.getViewModel().getStore('comcombo');

        if (Ext.isEmpty(view.code)) {
            console.log('공통코드 콤보박스에 코드값이 설정되어 있지 않습니다.');
            return;
        }

        store.load({
            params: {
                prnt_code_id: view.code
            },
            callback: function(records, operation, success) {
                me.getView().fireEvent('comboStoreLoaded', records, operation, success);
            }
        });
    }
});