//在光标位置插入文本
(function ($) {
    $.fn.extend({
        /**
         * 在指定输入域的光标位置插入文本。
         *
         * @param {string} myValue 要插入的文本。
         */
        insertAtCaret: function (myValue) {
            // 通过jQuery获取原始DOM元素
            let $t = $(this)[0];

            // 检查浏览器是否支持document.selection（旧版IE浏览器）
            if (document.selection) {
                // 焦点定位到输入域，以便于后续的文本插入操作
                this.focus();
                // 创建选区范围
                let sel = document.selection.createRange();
                // 在选区范围内插入文本
                sel.text = myValue;
            } else if ($t.selectionStart || $t.selectionStart === 0) {
                // 非IE浏览器，支持selectionStart和selectionEnd属性

                // 获取当前的光标位置和选区结束位置
                let startPos = $t.selectionStart;
                let endPos = $t.selectionEnd;
                // 获取当前的滚动条位置，以便后续恢复
                let scrollTop = $t.scrollTop;

                // 在光标位置插入文本，并重新组织输入域的值
                $t.value = $t.value.substring(0, startPos) + myValue + $t.value.substring(endPos, $t.value.length);

                // 调整光标位置到插入文本后的结束位置
                $t.selectionStart = startPos + myValue.length;
                $t.selectionEnd = startPos + myValue.length;

                // 恢复之前的滚动条位置
                $t.scrollTop = scrollTop;
            } else {
                // 如果浏览器既不支持document.selection也不支持selectionStart和selectionEnd，直接在值的末尾插入文本
                this.value += myValue;
            }
            // 重新聚焦到输入域
            this.focus();
        }
    })
})(jQuery);