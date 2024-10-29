package com.dot.comm.em;

/**
 * 翻页类型枚举
 *
 * @author: Dao-yang.
 * @date: Created in 2024/4/18 10:06
 */
public enum PageFlippingTypeEm {

    // 翻页类型(PULL_UP:上拉翻页;HIS_PULL_UP:历史上拉翻页;PULL_DOWN:下拉翻页;LOCATE:定位翻页;FIRST:首次翻页;)
    PULL_UP("上拉翻页"),
    HIS_PULL_UP("历史上拉翻页"),
    PULL_DOWN("下拉翻页"),
    LOCATE("定位翻页"),
    FIRST("首次翻页");

    private String desc;

    PageFlippingTypeEm(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static String getPageFlippingTypeDesc(String pageFlippingType) {
        for (PageFlippingTypeEm pageFlippingTypeEm : PageFlippingTypeEm.values()) {
            if (pageFlippingTypeEm.name().equals(pageFlippingType)) {
                return pageFlippingTypeEm.getDesc();
            }
        }
        return null;
    }
}
