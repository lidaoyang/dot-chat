export declare class DrawArrow {
    private beginPoint;
    private stopPoint;
    private polygonVertex;
    private angle;
    private arrowInfo;
    private size;
    /**
     * 绘制箭头
     * @param ctx 需要进行绘制的画布
     * @param originX 鼠标按下时的x轴坐标
     * @param originY 鼠标按下式的y轴坐标
     * @param x 当前鼠标x轴坐标
     * @param y 当前鼠标y轴坐标
     * @param color 箭头颜色
     * @param size 箭头尺寸
     */
    draw(ctx: CanvasRenderingContext2D, originX: number, originY: number, x: number, y: number, color: string, size: number): void;
    private arrowCord;
    private getRadian;
    private sideCord;
    /**
     * 设置箭头的相关绘制信息
     * @param edgeLen 长度
     * @param angle 角度
     * @private
     */
    private setArrowInfo;
    private dynArrowSize;
    private drawArrow;
}
