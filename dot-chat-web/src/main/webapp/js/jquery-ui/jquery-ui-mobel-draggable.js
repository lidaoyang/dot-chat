// jQuery UI draggable + scale 适配移动端

let moveFlag = 0; // 是否移动的flag
// /iPad|iPhone|Android/.test( navigator.userAgent ) &&
(function ($) {
    const proto = $.ui.mouse.prototype, _mouseInit = proto._mouseInit;
    //图片缩放变量
    let startDistance = 0, scalingFactor = 1;
    $.extend(proto, {
        _mouseInit: function () {
            this.element.on("touchstart." + this.widgetName, $.proxy(this, "_touchStart"));
            _mouseInit.apply(this, arguments);
        }, _touchStart: function (event) {
            this.element.on("touchmove." + this.widgetName, $.proxy(this, "_touchMove")).on("touchend." + this.widgetName, $.proxy(this, "_touchEnd"));
            this._modifyEvent(event);
            $(document).trigger($.Event("mouseup"));
            //reset mouseHandled flag in ui.mouse
            this._mouseDown(event);

            startDistance = 0;
            const touches = event.originalEvent.touches;
            if (touches.length === 2 && touches[0].target.tagName === 'IMG') { // 双指缩放
                // console.log('two finger touch');
                startDistance = Math.hypot(touches[1].pageX - touches[0].pageX, touches[1].pageY - touches[0].pageY);
            }

            //--------------------touchStart do something--------------------
            // console.log("i touchStart!");
        }, _touchMove: function (event) {
            moveFlag += 1;
            const touches = event.originalEvent.touches;
            if (startDistance > 0) { // 双指缩放
                const currentDistance = Math.hypot(touches[1].pageX - touches[0].pageX, touches[1].pageY - touches[0].pageY);
                const scaleFactor = currentDistance / startDistance;
                const newScale = scalingFactor * scaleFactor;

                const target = touches[0].target;
                this.element.css('transform', 'scale(' + newScale + ')');
                startDistance = currentDistance;
                scalingFactor = newScale;
            } else {
                this._modifyEvent(event);
                this._mouseMove(event);
            }
            //--------------------touchMove do something--------------------
            // console.log('i touchMove!', 'startDistance', startDistance, 'moveFlag', moveFlag);
        }, _touchEnd: function (event) {
            this.element.off("touchmove." + this.widgetName).off("touchend." + this.widgetName);
            // 主动触发点击事件
            if (moveFlag <= 7 && startDistance === 0) {
                this.element.click();
                scalingFactor = 1;
            } else {
                this._mouseUp(event);
            }
            moveFlag = 0;
            //--------------------touchEnd do something--------------------
            // console.log("i touchEnd!");
        }, _modifyEvent: function (event) {
            event.which = 1;
            const target = event.originalEvent.targetTouches[0];
            event.pageX = target.clientX;
            event.pageY = target.clientY;
        }
    });
})(jQuery);