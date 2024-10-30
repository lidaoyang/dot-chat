import { screenShotType } from "../../lib/type/ComponentType";
export default class CreateDom {
    private readonly screenShotController;
    private readonly toolController;
    private readonly optionIcoController;
    private readonly optionController;
    private readonly cutBoxSizeContainer;
    private readonly textInputController;
    private readonly completeCallback;
    private readonly closeCallback;
    private readonly hiddenIcoArr;
    private readonly toolbar;
    private readonly textFontSizeList;
    constructor(options: screenShotType);
    private setToolBarIco;
    private setTextSizeSelectPanel;
    private setBrushSelectPanel;
    private setTextInputPanel;
    private setAllControllerId;
    private hiddenAllDom;
    private setDomToBody;
    private clearBody;
    private setOptionIcoClassName;
    private filterHideIcon;
}
