package gapp.season.star.bsc5;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import gapp.season.star.util.StarUtil;

//http://tdc-www.harvard.edu/catalogs/bsc5.readme
public class Star {
    public String index; //HR： [1/9110]？哈佛修订号（明星号码）
    public String name; //名称，通常是拜耳和/或Flamsteed名称
    public String dm; //Durchmusterung Identification（星表识别）
    public String hd; //[1/225300]？Henry Draper目录编号
    public String sao; //[1/258997]？SAO目录编号
    public String fk5; //FK5星号

    //1900年位置
    public String rah1900; //经度-时
    public String ram1900; //经度-分
    public String ras1900; //经度-秒
    public String degsign1900; //纬度-符号
    public String deg1900; //纬度-度
    public String arcm1900; //纬度-分度
    public String arcs1900; //纬度-秒度
    //2000年位置
    public String rah2000; //经度-时
    public String ram2000; //经度-分
    public String ras2000; //经度-秒
    public String degsign2000; //纬度-符号
    public String deg2000; //纬度-度
    public String arcm2000; //纬度-分度
    public String arcs2000; //纬度-秒度

    public String glon; //银河经度
    public String glat; //银河纬度

    public String mag; //视星等
    public String sptype; //频谱类型
    public String arcsec; //三角视差(单位：弧秒)(1秒差距约等于3.261光年)

    public static Star readLine(String line) {
        Star star = new Star();
        try {
            star.index = line.substring(0, 4);
            star.name = line.substring(4, 14); //3位编号+3位希腊字母缩写+1位多星编号+3位星座名
            star.dm = line.substring(14, 25);
            star.hd = line.substring(25, 31);
            star.sao = line.substring(31, 37);
            star.fk5 = line.substring(37, 41);
            //1900年位置
            star.rah1900 = line.substring(60, 62);
            star.ram1900 = line.substring(62, 64);
            star.ras1900 = line.substring(64, 68);
            star.degsign1900 = line.substring(68, 69);
            star.deg1900 = line.substring(69, 71);
            star.arcm1900 = line.substring(71, 73);
            star.arcs1900 = line.substring(73, 75);
            //2000年位置
            star.rah2000 = line.substring(75, 77);
            star.ram2000 = line.substring(77, 79);
            star.ras2000 = line.substring(79, 83);
            star.degsign2000 = line.substring(83, 84);
            star.deg2000 = line.substring(84, 86);
            star.arcm2000 = line.substring(86, 88);
            star.arcs2000 = line.substring(88, 90);
            //其他(星表最短行长度为160)
            star.glon = line.substring(90, 96);
            star.glat = line.substring(96, 102);
            star.mag = line.substring(102, 107);
            if (line.length() >= 147) star.sptype = line.substring(127, 147);
            if (line.length() >= 166) star.arcsec = line.substring(161, 166);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return star;
    }

    public String getLongitudeStr(boolean lon1900) {
        if (lon1900) {
            return String.format("%s:%s:%s", rah1900, ram1900, ras1900);
        } else {
            return String.format("%s:%s:%s", rah2000, ram2000, ras2000);
        }
    }

    public String getLatitudeStr(boolean lat1900) {
        if (lat1900) {
            return String.format("%s%s:%s:%s", degsign1900, deg1900, arcm1900, arcs1900);
        } else {
            return String.format("%s%s:%s:%s", degsign2000, deg2000, arcm2000, arcs2000);
        }
    }

    /**
     * 获取经度
     */
    public double getLongitude(boolean lon1900) {
        try {
            double rah = Double.valueOf(lon1900 ? rah1900 : rah2000);
            double ram = Double.valueOf(lon1900 ? ram1900 : ram2000);
            double ras = Double.valueOf(lon1900 ? ras1900 : ras2000);
            return rah * 15 + ram * 15 / 60 + ras * 15 / 3600;
        } catch (Exception e) {
            if (!(e instanceof NumberFormatException)) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 获取纬度
     */
    public double getLatitude(boolean lat1900) {
        try {
            int sign = Integer.valueOf((lat1900 ? degsign1900 : degsign2000) + "1");
            double deg = Double.valueOf(lat1900 ? deg1900 : deg2000);
            double arcm = Double.valueOf(lat1900 ? arcm1900 : arcm2000);
            double arcs = Double.valueOf(lat1900 ? arcs1900 : arcs2000);
            return (deg + arcm / 60 + arcs / 3600) * sign;
        } catch (Exception e) {
            if (!(e instanceof NumberFormatException)) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 获取亮度(0等星亮度以100计)
     */
    public double getLuminance() {
        try {
            if (mag != null && mag.trim().length() > 0) {
                double magDouble = Double.valueOf(mag);
                return StarUtil.getLuminance(magDouble);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取距离(光年)
     */
    public double getLightYear() {
        try {
            double arcsecDouble = Math.abs(Double.valueOf(arcsec));
            double au = 1 / Math.tan(arcsecDouble / 3600 * (Math.PI / 180)); //天文单位(AU),Math.tan方法使用的为弧度
            return au / 63240; //一光年(ly)约等于63240天文单位
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取星座名
     */
    public String getConstellationName() {
        if (name != null) {
            String showName = Constellation.getConstellationName(name) + GreekLetter.getGreekLetter(name);
            if (TextUtils.isEmpty(showName)) {
                return name.trim();
            } else {
                return showName;
            }
        }
        return "";
    }

    public String toFormatLine() {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                trim(index), trim(name), trim(dm), trim(hd), trim(sao), trim(fk5),
                getLongitude(true), getLatitude(true), getLongitude(false),
                getLatitude(false), trim(mag), getLuminance(), getLightYear(), trim(sptype), getConstellationName());
    }

    public String getStarData() {
        String cn = "";
        String cnName = StarCnName.getCnName(index);
        if (!TextUtils.isEmpty(cnName)) {
            cn = "古名：" + cnName + "\n";
        }
        double luminance = getLuminance();
        double lightYear = getLightYear();
        return String.format("Yale Bright Star Catalog：\nNo.%s：[%s°, %s°]\n名称：%s\n%s距离：%s光年\n" +
                        "亮度：%s\n视星等：%s\n绝对星等：%s\n三角视差：%s弧秒\n光谱类型：%s",
                trimI(index), StarUtil.formatNum(getLongitude(false), 1),
                StarUtil.formatNum(getLatitude(false), 1), trimI(getConstellationName()),
                cn, StarUtil.formatNum(lightYear, 2), StarUtil.formatNum(luminance, 2),
                trimI(mag), StarUtil.formatNum(StarUtil.getAbsMag(trimI(mag), trimI(arcsec)), 2)
                , trimI(arcsec), trimI(sptype));
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("No.%s,[%s,%s],星等:%s,亮度:%s,光年:%s,名称:%s", trim(index), getLongitudeStr(false),
                getLatitudeStr(false), trim(mag), getLuminance(), getLightYear(), getConstellationName());
    }

    private String trimI(String data) {
        if (data != null) {
            String str = data.trim();
            if (str.length() > 0)
                return str;
        }
        return "--";
    }

    private String trim(String data) {
        if (data != null)
            return data.trim();
        return "";
    }
}
