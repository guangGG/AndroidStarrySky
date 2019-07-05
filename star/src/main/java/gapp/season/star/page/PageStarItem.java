package gapp.season.star.page;

import gapp.season.star.bsc5.Star;
import gapp.season.star.solar.Solar;
import gapp.season.star.util.StarUtil;
import gapp.season.star.view.SkyView;

public class PageStarItem extends SkyView.StarItem {
    private Star mStar;
    private Solar mSolar;

    public Star getStar() {
        return mStar;
    }

    public void setStar(Star star) {
        mStar = star;
    }

    public Solar getSolar() {
        return mSolar;
    }

    public void setSolar(Solar solar) {
        mSolar = solar;
    }

    public boolean isSolar() {
        return getType() != 0;
    }

    public String getStarData() {
        if (isSolar()) {
            if (mSolar != null) {
                return String.format("%s\n坐标：[%s°, %s°]", mSolar.getStarData(),
                        StarUtil.formatNum(getLongitude(), 1),
                        StarUtil.formatNum(getLatitude(), 1));
            }
        } else {
            if (mStar != null) {
                return mStar.getStarData();
            }
        }
        return String.format("坐标：[%s°, %s°]", StarUtil.formatNum(getLongitude(), 1),
                StarUtil.formatNum(getLatitude(), 1));
    }
}
