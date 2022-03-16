package cn.wildfire.imshat.kit.widget.indicator;

public class IndicatorItem {
    private int resId;
    private int tip;

    public IndicatorItem(int resIds, int tips) {
        this.resId = resIds;
        this.tip = tips;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public int getTip() {
        return tip;
    }

    public void setTip(int tip) {
        this.tip = tip;
    }
}
