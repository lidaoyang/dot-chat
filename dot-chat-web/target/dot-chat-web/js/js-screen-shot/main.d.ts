import "@/assets/scss/screen-shot.scss";
import { screenShotType } from "./lib/type/ComponentType";
export default class ScreenShot {
    private readonly data;
    private readonly videoController;
    private screenShotContainer;
    private screenShotDom;
    private toolController;
    private screenShotImageController;
    private screenShotCanvas;
    private textInputController;
    private optionController;
    private optionIcoController;
    private cutBoxSizeContainer;
    private plugInParameters;
    private wrcReplyTime;
    private keyboardEventHandle;
    private drawGraphPosition;
    private tempGraphPosition;
    private wrcImgPosition;
    private hiddenScrollBar;
    private wrcWindowMode;
    private cutOutBoxBorderArr;
    private borderOption;
    private movePosition;
    private dragFlag;
    private clickCutFullScreen;
    private getFullScreenStatus;
    private drawGraphPrevX;
    private drawGraphPrevY;
    private degreeOfBlur;
    private dpr;
    private fullScreenDiffHeight;
    private position;
    private imgSrc;
    private loadCrossImg;
    private mouseInsideCropBox;
    private proxyUrl;
    private drawStatus;
    private captureStream;
    private cropBoxInfo;
    private textInputPosition;
    private placement;
    private drawArrow;
    private customRightClickEvent;
    constructor(options: screenShotType);
    getCanvasController(): HTMLCanvasElement | null | undefined;
    destroyComponents(): void;
    completeScreenshot(): void;
    private registerForRightClickEvent;
    private load;
    private sendStream;
    private loadScreenFlowData;
    private startCapture;
    private stopCapture;
    private screenShot;
    private mouseDownEvent;
    private mouseMoveEvent;
    private adjustContainerLevels;
    private initCropBox;
    /**
     * 从窗口数据流中截取页面body内容
     * @param videoWidth 窗口宽度
     * @param videoHeight 窗口高度
     * @param containerWidth body内容宽度
     * @param containerHeight body内容高度
     * @private
     */
    private getWindowContentData;
    private registerContainerShortcuts;
    private showToolBar;
    private setGlobalParameter;
    private setOptionalParameter;
    private mouseUpEvent;
    /**
     * 操作裁剪框
     * @param currentX 裁剪框当前x轴坐标
     * @param currentY 裁剪框当前y轴坐标
     * @param startX 鼠标x轴坐标
     * @param startY 鼠标y轴坐标
     * @param width 裁剪框宽度
     * @param height 裁剪框高度
     * @param context 需要进行绘制的canvas画布
     * @private
     */
    private operatingCutOutBox;
    /**
     * 显示最新的画布状态
     * @private
     */
    private showLastHistory;
    private setScreenShotContainerEventListener;
    /**
     * 向截图容器中绘制图片
     * @param triggerCallback
     * @param context
     * @param imgSrc
     * @private
     */
    private drawPictures;
    /**
     * 初始化截图容器
     * @param triggerCallback
     * @param context
     * @param screenShotContainer
     * @private
     */
    private initScreenShot;
}
