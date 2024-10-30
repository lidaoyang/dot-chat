export default class PlugInParameters {
    constructor();
    setInitStatus(status: boolean): void;
    getInitStatus(): boolean;
    getWebRtcStatus(): boolean;
    setWebRtcStatus(status: boolean): void;
    setScreenShotDom(dom: HTMLElement): void;
    getCutBoxBdColor(): string;
    setCutBoxBdColor(color: string): void;
    getScreenShotDom(): HTMLElement | null;
    getScreenFlow(): MediaStream | null;
    setScreenFlow(stream: MediaStream): void;
    getCanvasSize(): {
        canvasWidth: number;
        canvasHeight: number;
    };
    setCanvasSize(width: number, height: number): void;
    getShowScreenDataStatus(): boolean;
    setShowScreenDataStatus(status: boolean): void;
    setMaskColor(color: {
        r: number;
        g: number;
        b: number;
        a: number;
    }): void;
    getMaskColor(): {
        r: number;
        g: number;
        b: number;
        a: number;
    };
    setWriteImgState(state: boolean): void;
    getWriteImgState(): boolean;
    setSaveCallback(saveFn: (code: number, msg: string) => void): void;
    getSaveCallback(): ((code: number, msg: string) => void) | null;
    setMaxUndoNum(num: number): void;
    getMaxUndoNum(): number;
    setRatioArrow(state: boolean): void;
    getRatioArrow(): boolean;
    setImgAutoFit(state: boolean): void;
    getImgAutoFit(): boolean;
    setSaveImgTitle(title: string): void;
    getSaveImgTitle(): string | null;
}
