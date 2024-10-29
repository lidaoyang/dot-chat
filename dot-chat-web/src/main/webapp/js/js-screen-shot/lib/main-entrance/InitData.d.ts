import { positionInfoType, textInfoType } from "../../lib/type/ComponentType";
export default class InitData {
    constructor();
    setInitStatus(status: boolean): void;
    setScreenShotInfo(width: number, height: number): void;
    setScreenShotPosition(left: number, top: number): void;
    showScreenShotPanel(): void;
    getScreenShotContainer(): HTMLCanvasElement | null;
    getToolController(): HTMLDivElement | null;
    getCutBoxSizeContainer(): HTMLDivElement | null;
    getTextInputController(): HTMLDivElement | null;
    getTextStatus(): boolean;
    getScreenShotImageController(): HTMLCanvasElement | null;
    setScreenShotImageController(imageController: HTMLCanvasElement): void;
    setToolStatus(status: boolean): void;
    setCutBoxSizeStatus(status: boolean): void;
    setCutBoxSizePosition(x: number, y: number): void;
    setTextEditState(state: boolean): void;
    getTextEditState(): boolean;
    setCutBoxSize(width: number, height: number): void;
    setTextStatus(status: boolean): void;
    setToolInfo(left: number, top: number): void;
    getToolClickStatus(): boolean;
    setToolClickStatus(status: boolean): void;
    setResetScrollbarState(state: boolean): void;
    getResetScrollbarState(): boolean;
    getCutOutBoxPosition(): positionInfoType;
    getDragging(): boolean;
    setDragging(status: boolean): void;
    getDraggingTrim(): boolean;
    getToolPositionStatus(): boolean;
    setToolPositionStatus(status: boolean): void;
    setDraggingTrim(status: boolean): void;
    setCutOutBoxPosition(mouseX: number, mouseY: number, width: number, height: number): void;
    setFontSize(size: number): void;
    setOptionStatus(status: boolean): void;
    getFontSize(): number;
    setTextSizeOptionStatus(status: boolean): void;
    setTextSizePanelStatus(status: boolean): void;
    setBrushSelectionStatus(status: boolean): void;
    hiddenOptionIcoStatus(): void;
    getOptionIcoController(): HTMLDivElement | null;
    getTextSizeContainer(): HTMLDivElement | null;
    getOptionTextSizeController(): HTMLDivElement | null;
    getBrushSelectionController(): HTMLDivElement | null;
    getOptionController(): HTMLDivElement | null;
    setOptionPosition(position: number): void;
    getToolPosition(): {
        left: number;
        top: number;
    } | undefined;
    getSelectedColor(): string;
    setSelectedColor(color: string): void;
    getColorSelectPanel(): HTMLElement | null;
    getToolName(): string;
    setToolName(itemName: string): void;
    getPenSize(): number;
    setPenSize(size: number): void;
    getMosaicPenSize(): number;
    setMosaicPenSize(size: number): void;
    getBorderSize(): number;
    getHistory(): Record<string, any>[];
    shiftHistory(): Record<string, any> | undefined;
    popHistory(): Record<string, any> | undefined;
    pushHistory(item: Record<string, any>): void;
    getUndoClickNum(): number;
    setUndoClickNum(clickNumber: number): void;
    getColorPanel(): HTMLElement | null;
    setColorPanelStatus(status: boolean): void;
    getNoScrollStatus(): boolean;
    setNoScrollStatus(status?: boolean): void;
    setActiveToolName(toolName: string): void;
    getActiveToolName(): string;
    setTextInfo(info: textInfoType): void;
    getTextInfo(): textInfoType;
    getRightPanel(): HTMLElement | null;
    setRightPanel(status: boolean): void;
    setUndoStatus(status: boolean): void;
    cancelEvent(): void;
    getUndoController(): HTMLElement | null;
    destroyDOM(): void;
}
