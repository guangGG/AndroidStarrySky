package gapp.season.star.util;

import java.text.DecimalFormat;

public class StarUtil {
    /**
     * 格式化数字显示
     */
    public static String formatNum(double num, int digit) {
        String pattern = "0";
        if (digit > 0) {
            pattern = "0.";
            for (int i = 0; i < digit; i++) {
                pattern += "#";
            }
        }
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(num);
    }

    /**
     * 计算指定星等天体的亮度(0等星亮度以100计)
     */
    public static double getLuminance(double mag) {
        return 100 * Math.pow(100, -mag / 5);
    }

    /**
     * 计算指定亮度天体的星等(0等星亮度以100计)
     */
    public static double getMag(double luminance) {
        return -Math.log(luminance / 100) / Math.log(100d) * 5;
    }

    /**
     * 计算天体绝对星等
     *
     * @param mag    视星等
     * @param arcsec 三角视差(单位：弧秒)
     */
    public static double getAbsMag(String mag, String arcsec) {
        // 如果绝对星等用M表示，视星等用m表示恒星的距离化成秒差距数为r，那么M=m+5(1+log10(r))
        try {
            double magDouble = Double.valueOf(mag);
            double arcsecDouble = Math.abs(Double.valueOf(arcsec));
            return magDouble + 5 * (1 + Math.log10(arcsecDouble));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
