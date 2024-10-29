export type cutOutBoxBorder = {
    x: number;
    y: number;
    width: number;
    height: number;
    index: number;
    option: number;
};
export type movePositionType = {
    moveStartX: number;
    moveStartY: number;
};
export type positionInfoType = {
    startX: number;
    startY: number;
    width: number;
    height: number;
};
export type textInfoType = {
    positionX: number;
    positionY: number;
    color: string;
    size: number;
};
export type zoomCutOutBoxReturnType = {
    tempStartX: number;
    tempStartY: number;
    tempWidth: number;
    tempHeight: number;
};
export type toolPositionValType = "left" | "right" | "center";
export type hideBarInfoType = {
    state: boolean;
    color?: string;
    fillWidth?: number;
    fillHeight?: number;
    fillState?: boolean;
};
export type drawCutOutBoxReturnType = {
    startX: number;
    startY: number;
    width: number;
    height: number;
};
export type toolIcoType = {
    [key: string]: boolean | undefined;
    square?: boolean;
    round?: boolean;
    rightTop?: boolean;
    brush?: boolean;
    mosaicPen?: boolean;
    text?: boolean;
    separateLine?: boolean;
    save?: boolean;
    undo?: boolean;
    confirm?: boolean;
};
export type toolbarType = {
    id: number;
    title: string;
};
export type crcEventType = {
    state: boolean;
    handleFn?: () => void;
};
export type screenShotType = {
    enableWebRtc?: boolean;
    screenFlow?: MediaStream;
    level?: number;
    canvasWidth?: number;
    canvasHeight?: number;
    completeCallback?: (imgInfo: {
        base64: string;
        cutInfo: positionInfoType;
    }) => void;
    closeCallback?: () => void;
    triggerCallback?: (res: {
        code: number;
        msg: string;
        displaySurface: string | null;
        displayLabel: string | null;
    }) => void;
    cancelCallback?: (res: {
        code: number;
        msg: string;
        errorInfo: string;
    }) => void;
    saveCallback?: (code: number, msg: string) => void;
    position?: {
        top?: number;
        left?: number;
    };
    clickCutFullScreen?: boolean;
    hiddenToolIco?: toolIcoType;
    showScreenData?: boolean;
    imgSrc?: string;
    loadCrossImg?: boolean;
    proxyUrl?: string;
    screenShotDom?: HTMLElement | HTMLDivElement | HTMLCanvasElement;
    cropBoxInfo?: {
        x: number;
        y: number;
        w: number;
        h: number;
    };
    wrcReplyTime?: number;
    wrcImgPosition?: {
        x: number;
        y: number;
        w: number;
        h: number;
    };
    noScroll?: boolean;
    maskColor?: {
        r: number;
        g: number;
        b: number;
        a: number;
    };
    toolPosition?: toolPositionValType;
    writeBase64?: boolean;
    hiddenScrollBar?: hideBarInfoType;
    wrcWindowMode?: boolean;
    customRightClickEvent?: crcEventType;
    cutBoxBdColor?: string;
    maxUndoNum?: number;
    useRatioArrow?: boolean;
    imgAutoFit?: boolean;
    saveImgTitle?: string;
};
