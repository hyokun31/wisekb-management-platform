Ext.define('Flamingo.component.timeprogress.TimeprogressController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.timeprogress',

    data: {
        value: 0
    },

    startTimer: function(time, tick) {
        var me = this,
            runner = new Ext.util.TaskRunner(),
            loop = 1,
            task, value;

        task = runner.newTask({
            run: function() {
                value = parseInt(loop * tick / time * 100);
                me.getViewModel().set('value', value);

                if (loop * tick > time) {
                    task.stop();
                    me.fireViewEvent('timeprogressEnd');
                }

                ++loop;
            },
            interval: tick
        });

        me.task = task;

        task.start();
    }
});