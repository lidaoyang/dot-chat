export default class KeyboardEventHandle {
    private readonly toolController;
    constructor(screenShotController: HTMLCanvasElement, toolController: HTMLDivElement);
    /**
     * 触发工具栏指定模块的点击事件
     * @param eventName 事件名, 与截图工具栏中的data-title属性值保持一致
     * @private
     */
    triggerEvent(eventName: string): void;
}
