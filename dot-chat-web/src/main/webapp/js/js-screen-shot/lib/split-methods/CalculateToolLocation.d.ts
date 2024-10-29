import { positionInfoType, toolPositionValType } from "../../lib/type/ComponentType";
/**
 * 计算截图工具栏位置
 * @param position 裁剪框位置信息
 * @param toolWidth 截图工具栏宽度
 * @param containerWidth 截图容器宽度
 * @param placement 展示位置
 * @param containerLocation 截图容器位置信息
 */
export declare function calculateToolLocation(position: positionInfoType, toolWidth: number, containerWidth: number, placement: toolPositionValType, containerLocation: {
    top: number;
    left: number;
}): {
    mouseX: number;
    mouseY: number;
};
