/**
 * 绘制裁剪框
 * @param mouseX 鼠标x轴坐标
 * @param mouseY 鼠标y轴坐标
 * @param width 裁剪框宽度
 * @param height 裁剪框高度
 * @param context 需要进行绘制的canvas画布
 * @param borderSize 边框节点直径
 * @param controller 需要进行操作的canvas容器
 * @param imageController 图片canvas容器
 * @param drawBorders
 * @private
 */
export declare function drawCutOutBox(mouseX: number, mouseY: number, width: number, height: number, context: CanvasRenderingContext2D, borderSize: number, controller: HTMLCanvasElement, imageController: HTMLCanvasElement, drawBorders?: boolean): {
    startX: number;
    startY: number;
    width: number;
    height: number;
} | undefined;
