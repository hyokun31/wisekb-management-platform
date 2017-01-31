Ext.define('Flamingo.component.timeprogress.Timeprogress', {
    extend: 'Ext.window.Window',
    xtype: 'timeprogress',

    requires: [
        'Flamingo.component.timeprogress.TimeprogressController',
        'Flamingo.component.timeprogress.TimeprogressModel'
    ],

    style: {
        backgroundColor: 'transparent'
    },
    bodyStyle: {
        backgroundColor: 'transparent'
    },

    controller: 'timeprogress',
    viewModel: 'timeprogress',

    header: false,
    modal: true,

    width: 240,
    height: 240,

    bind: {
        html: '<div class="c100 p{value} dark big active"><span>{value}%</span><div class="slice"><div class="bar"></div><div class="fill"></div></div></div>'
    },

    /**
     * Timer를 구동한다 time만큼 100%가 계산되어 표시된다.
     * Timer가 종료되면 timeprogressEnd 이벤트가 발생한다.
     * @time int Timer가 총 동작하는 시간
     * @tick int millisecond 설정한 tick값에 한번씩 Progress가 업데이트 된다
     * */
    startTimer: function(time, tick) {
        this.fireEvent('startTimer', time, tick);
    },

    listeners: {
        startTimer: 'startTimer'
    }
});