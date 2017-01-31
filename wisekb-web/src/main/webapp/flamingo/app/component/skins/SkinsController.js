Ext.define('Flamingo.component.skins.SkinsController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.skins',

    onItemdblclick: function(view, record) {

        if (Ext.manifest.profile == record.get('key')) {
            return;
        }

        this.updateTheme(record.get('key'));
    },

    updateTheme: function(theme) {
        var params = {
            theme: theme
        };

        invokePostByMap(CONSTANTS.SYSTEM.USER.UPDATE_THEME, params,
            function (response) {
                var obj = Ext.decode(response.responseText);

                if (obj.success) {
                    window.location.reload(true);
                } else {
                    error(message.msg('common.notice'), obj.error.message);
                }
            },
            function () {
                error(message.msg('common.warning'), format(message.msg('common.failure'), config['system.admin.email']));
            }
        );
    }
});