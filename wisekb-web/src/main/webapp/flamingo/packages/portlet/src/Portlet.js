Ext.define('portlet.Portlet', {
    extend: 'Ext.container.Container',
    xtype: 'portlet',

    scrollable: true,
    margin: '13 0 0 13',

    config: {
        //컬럼의 최대값
        max_cols: undefined,

        //행의 최대값
        max_rows: undefined,

        //DEPRECATED
        max_size_x: false,

        //Widget 사이의 간격
        widgetMargins: [7, 7],

        //Widget 기본 크기
        widgetBaseDimensions: [140, 140],

        //Widget을 생성하는 selector
        widgetSelector: '.portlet-column',

        //gridster 컬럼이 동적으로 생성시킬지 여부. true일 경우 column을 이동하면 우측으로 스크롤바가 생기면서 넓이가 늘어난다.
        autogrowCols: true,

        //gridster 칼럼을 고정폭으로 지정할때 사용한다. 반드시 {autogrowCols: false, max_cols: 숫자값} 이 지정되어 있어야 한다.
        fitWidth: false,

        //gridster 행을 고정높이로 지정할때 사용한다. 반드시 {max_rows: 숫자값}이 지정되어 있어야 한다.
        fitHeight: false
    },

    cls: 'gridster',

    defaults: {
        cls: 'portlet-column',
        frame: true,
        style: {
            boxShadow: '0px 0px 7px RGBA(0,0,0,0.2)'
        }
    },

    beforeRender: function() {
        var me = this;

        me.callParent(arguments);

    },

    /**
     * ExtJS에서 기본적으로 제공하는 함수
     *
     * Ext.Component가 Render 된 이후에 실행된다.
     * store bind, gridster 생성, gridster 이벤트 바인딩 등 이곳에서 처리된다.
     * */
    afterRender: function() {
        var me = this,
            config = me.getConfig(),
            //items = me.gridItems,
            items = me.items.items,
            margins = me.getWidgetMargins(),
            addtion = margins[0] + margins[1],
            width, height, idx;

        me.callParent(arguments);

        var viewModel = me.getViewModel() ? me.getViewModel() : me.container ? me.container.component.getViewModel() : null;

        Ext.defer(function() {

            if (me.getFitWidth() && !me.getAutogrowCols()) {
                var gap = margins[0] + margins[1];

                me.setWidgetBaseDimensions([Math.floor(me.getWidth() / me.getMax_cols()) - gap - 5, Math.floor(me.getHeight() / me.getMax_rows()) - gap - 5]);
            }

            for (idx in items) {
                var item = items[idx];
                width = me.getWidgetBaseDimensions()[0] * item.dataSizex + ((item.dataSizex - 1) * addtion);
                height = me.getWidgetBaseDimensions()[1] * item.dataSizey + ((item.dataSizey - 1) * addtion);

                item.setSize(width, height);

                if (item.dataRow) item.getEl().set({"data-row": item.dataRow});
                if (item.dataCol) item.getEl().set({"data-col": item.dataCol});
                if (item.dataSizex) item.getEl().set({"data-sizex": item.dataSizex});
                if (item.dataSizey) item.getEl().set({"data-sizey": item.dataSizey});
            }

            me.gridster = $(".gridster > div > div").gridster({
                widget_margins: me.getWidgetMargins(),
                widget_base_dimensions: me.getWidgetBaseDimensions(),
                widget_selector: me.getWidgetSelector(),
                autogrow_cols: me.getAutogrowCols(),
                max_cols: me.getMax_cols(),
                max_rows: me.getMax_rows(),
                //max_size_x: me.getMax_size_x(),
                draggable: {
                    handle: '.x-panel-header-title div'
                },
                serialize_params: function(w, wgd) {
                    return { col: wgd.col, row: wgd.row, size_x: wgd.size_x, size_y: wgd.size_y, xtype: Ext.getCmp(w[0].id).getXType() };
                },
                resize: {
                    enabled: true,
                    //handle_append_to: false,
                    resize: function(e, ui, widget) {
                        var panel = Ext.getCmp(widget[0].id);

                        panel.setSize(widget[0].offsetWidth, widget[0].offsetHeight);

                        if (typeof panel.getColumnType === 'function') {
                            if (panel.getColumnType() == 'text') {
                                panel.textFill(widget[0].offsetWidth);
                            }
                            else if (panel.getColumnType() == 'gauge') {
                                panel.resizeText();
                            }
                        }
                    },
                    stop: function(e, ui, widget) {
                        var panel = Ext.getCmp(widget[0].id);

                        panel.dataSizex = widget.attr('data-sizex');
                        panel.dataSizey = widget.attr('data-sizey');

                        panel.setSize(widget[0].offsetWidth, widget[0].offsetHeight);

                        if (typeof panel.getColumnType === 'function') {
                            if (panel.getColumnType() == 'text') {
                                panel.textFill(widget[0].offsetWidth);
                            }
                            else if (panel.getColumnType() == 'gauge') {
                                panel.resizeText();
                            }
                        }
                    }
                }
            }).data('gridster');
            /*Ext.defer(function() {
                if (me.getFitWidth() && !me.getAutogrowCols()) {
                    me.resizeGridster(me.gridster, me.getWidth(), me.getHeight());
                }
            }, 300);*/

        }, 10);
    },

    /**
     * Widget을 json데이터 형식으로 반환한다.
     * */
    serialize: function() {
        return this.gridster.serialize();
    },

    /**
     * Json데이터를 받아서 widget을 추가한다.
     * */
    deserialize: function(json) {
        var me = this,
            viewModel = me.getViewModel() ? me.getViewModel() : me.container ? me.container.component.getViewModel() : null,
            margins = me.getWidgetMargins(),
            addtion = margins[0] + margins[1],
            width, height, idx,
            columns = [];

        for (idx in json) {
            var item = json[idx];
            width = me.getWidgetBaseDimensions()[0] * item.size_x + ((item.size_x - 1) * addtion);
            height = me.getWidgetBaseDimensions()[1] * item.size_y + ((item.size_y - 1) * addtion);

            var config = Ext.merge(item, {renderTo: gridster, width: width, height: height, style: {backgroundColor: '#000000'}});
            var column = Ext.create(config);
            var bind = column.getBind();

            if (bind) {
                column.setStore(viewModel.getStore(bind.store.stub.name));
            }

            columns.push(column);
        }

        Ext.defer(function() {
            for (idx in columns) {
                var column = columns[idx];
                me.gridster.add_widget(column.getEl().dom, column.size_x, column.size_y, column.col, column.row);
            }
        }, 300);

    },

    /**
     * 동적으로 widget을 추가한다
     * xtype : ExtJS의 xtype //Ext.ComponentManager에 이미 등록되어 있어야 한다.
     * */
    addWidget: function(xtype, size_x, size_y, col, row) {
        var me = this,
            margins = me.getWidgetMargins(),
            addtion = margins[0] + margins[1],
            width = me.getWidgetBaseDimensions()[0] * size_x + ((size_x - 1) * addtion),
            height = me.getWidgetBaseDimensions()[1] * size_y + ((size_y - 1) * addtion);

        var config = {
            xtype: xtype,
            renderTo: gridster,
            width: width,
            height: height,
            size_x: size_x * 1,
            size_y: size_y * 1,
            col: col,
            row: row,
            style: {backgroundColor: '#000000'}
        };
        var column = Ext.create(config);
        
        var viewModel = me.getViewModel() ? me.getViewModel() : me.container ? me.container.component.getViewModel() : null;
        var bind = column.getBind();
        
        Ext.defer(function() {
            if (bind && viewModel) {
                column.setStore(viewModel.getStore(bind.store.stub.name));
            }
            me.gridster.add_widget(column.getEl().dom, column.size_x, column.size_y, column.col, column.row)
        }, 300);
    },
    
    /**
     * Private
     * */
    addCallback: function(element) {
        
    },

    /**
     * Private
     *
     * 패널이 Resize될 때 gridster의 내부 columns도 같이 사이즈를 줄인다.
     * 만약 fitWidth가 true일 경우 gridster가 생성된 이후에도 실행된다.
     * */
    resizeGridster: function (gridster, width, height) {
        var gap = gridster.options.widget_margins[0] + gridster.options.widget_margins[1];

        gridster.wrapper_width = width - gap;
        gridster.drag_api.options.container_width = width - gap;
        gridster.drag_api.initial_container_width = width - gap;
        gridster.drag_api.baseX = Math.floor(width / gridster.options.max_cols) - gap;
        gridster.options.widget_base_dimensions[0] = Math.floor(width / gridster.options.max_cols) - gap - 5;
        gridster.options.widget_base_dimensions[1] = Math.floor(height / gridster.options.max_rows) - gap - 5;
        gridster.min_widget_width  = (gridster.options.widget_margins[0] * 2) + gridster.options.widget_base_dimensions[0];
        gridster.min_widget_height = (gridster.options.widget_margins[1] * 2) + gridster.options.widget_base_dimensions[1];

        gridster.$widgets.each($.proxy(function(i, widget) {
            var $widget = $(widget),
                panel = Ext.getCmp($widget[0].id),
                setWidth = gridster.min_widget_width * panel.dataSizex - gap,
                setHeight = gridster.min_widget_height * panel.dataSizey - gap;

            panel.setSize(setWidth, setHeight);

            gridster.resize_widget($widget)
        }, gridster));

        gridster.recalculate_faux_grid();
        gridster.generate_stylesheet();
    },

    listeners: {
        resize: function(panel, width, height) {
            var gridster = panel.gridster;

            if (gridster && panel.getFitWidth()) {
                this.resizeGridster(gridster, width, height);
            }
        }
    }
});