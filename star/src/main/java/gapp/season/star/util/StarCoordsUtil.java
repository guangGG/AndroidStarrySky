package gapp.season.star.util;

import java.util.Calendar;
import java.util.Date;

import gapp.season.star.bsc5.Star;
import gapp.season.star.solar.SolarSystem;

/**
 * 坐标转换工具类
 */
public class StarCoordsUtil {
    private static final double HD = (Math.PI / 180); //弧度/角度的值

    public static double dealNaN(double num, double def, String formatLog, Object... args) {
        if (Double.isNaN(num)) {
            if (args == null || args.length <= 0) {
                StarLog.w("SkyStar", "isNaN Exception: " + formatLog);
            } else {
                StarLog.w("SkyStar", "isNaN Exception: " + String.format(formatLog, args));
            }
            return def;
        } else {
            return num;
        }
    }

    /**
     * @param base 基点星星
     * @param star 计算点星星
     */
    public static double getLatitudeAngle(Star base, Star star) {
        return getLatitudeAngle(base.getLongitude(false), base.getLatitude(false),
                star.getLongitude(false), star.getLatitude(false));
    }

    /**
     * 计算球面上任意两点的夹角(相对于球心)，原理：
     * 设a,b是两个不为0的向量，它们的夹角为<a,b> (或用α ,β, θ ,..,字母表示)
     * 1. 由向量公式：cos<a,b>=a*b/|a||b|. ---(公式Ⅰ)
     * 2. 若向量用坐标表示，a=(x1,y1,z1), b=(x2,y2,z2),
     * 则,a*b=(x1x2+y1y2+z1z2).
     * |a|=√(x1^2+y1^2+z1^2), |b|=√(x2^2+y2^2+z2^2).
     * 将这些代人公式(Ⅰ),得到：
     * cos<a,b>=(x1x2+y1y2+z1z2)/[√(x1^2+y1^2+z1^2)*√(x2^2+y2^2+z2^2)] ---(公式Ⅱ).
     * 上述公式是以空间三维坐标给出的，令坐标中的z=0,则得平面向量的计算公式。
     * 两个向量夹角的取值范围是：[0,π].
     * 夹角为锐角时，cosθ>0；夹角为钝角时,cosθ<0.
     */
    public static double getLatitudeAngle(double longitude1, double latitude1, double longitude2, double latitude2) {
        double z1 = Math.sin(latitude1 * HD);
        double xy1 = Math.cos(latitude1 * HD);
        double x1 = xy1 * Math.cos(longitude1 * HD);
        double y1 = xy1 * Math.sin(longitude1 * HD);

        double z2 = Math.sin(latitude2 * HD);
        double xy2 = Math.cos(latitude2 * HD);
        double x2 = xy2 * Math.cos(longitude2 * HD);
        double y2 = xy2 * Math.sin(longitude2 * HD);

        double cos = (x1 * x2 + y1 * y2 + z1 * z2)
                / (Math.sqrt(Math.pow(x1, 2) + Math.pow(y1, 2) + Math.pow(z1, 2))
                * Math.sqrt(Math.pow(x2, 2) + Math.pow(y2, 2) + Math.pow(z2, 2)));
        //cos值在[-1,1]之外时，Math.acos方法得出NaN
        if (cos > 1) {
            cos = 1;
        } else if (cos < -1) {
            cos = -1;
        }
        double angle = 90 - (Math.acos(cos) / HD);
        return dealNaN(angle, 90, "getLatitudeAngle:%s,%s,%s,%s", longitude1, latitude1, longitude2, latitude2);
    }

    /**
     * @param base 基点星星
     * @param star 计算点星星
     */
    public static double getLongitudeAngle(Star base, Star star) {
        return getLongitudeAngle(base.getLongitude(false), base.getLatitude(false),
                star.getLongitude(false), star.getLatitude(false));
    }

    /**
     * 计算球面上任意一点(Point2)对另外一点(Point1)的经度角(以北极点为180°)
     * 原理1-计算三个点的平面方程：
     * 将已知三个点的坐标分别用P1(x1,y1,z1)，P2(x2,y2,z2)，P3(x3,y3,z3)表示。（P1，P2，P3不在同一条直线上。）
     * 设通过P1，P2，P3三点的平面方程为A(x - x1) + B(y - y1) + C(z - z1) = 0 。
     * 化简为一般式：Ax + By + Cz + D = 0。
     * 将P1(x1,y1,z1)点数值代入方程Ax + By + Cz + D = 0。
     * 即可得到：Ax1 + By 1+ Cz1 + D = 0。
     * 化简得D = -(A * x1 + B * y1 + C * z1)。
     * 则可以根据P1(x1,y1,z1)，P2(x2,y2,z2)，P3(x3,y3,z3)三点坐标分别求得A、B、C的值，如下：
     * A = (y2 - y1)*(z3 - z1) - (z2 -z1)*(y3 - y1);
     * B = (x3 - x1)*(z2 - z1) - (x2 - x1)*(z3 - z1);
     * C = (x2 - x1)*(y3 - y1) - (x3 - x1)*(y2 - y1);
     * 又D = -(A * x1 + B * y1 + C * z1)，所以可以求得D的值。
     * 将求得的A、B、C、D值代入一般式方程就可得过P1，P2，P3的平面方程:
     * Ax + By + Cz + D = 0 (一般式)
     * 原理2-平面方程的法向量：
     * 用方程ax+by+cz=d表示的平面，向量(a,b,c)就是其法线。
     * 原理3-两个平面法向量的夹角和这两个平面的夹角相等。
     * 步骤：
     * 1.计算出[球心，Point1，北极点]和[球心，Point1，Point2]两个平面的法向量
     * 2.计算出两个平面法向量的夹角(以北极点为0°,范围是：[0,π])
     * 3.判断夹角的正负号和校准基线
     */
    public static double getLongitudeAngle(double longitude1, double latitude1, double longitude2, double latitude2) {
        double z1 = Math.sin(latitude1 * HD);
        double xy1 = Math.cos(latitude1 * HD);
        double x1 = xy1 * Math.cos(longitude1 * HD);
        double y1 = xy1 * Math.sin(longitude1 * HD);

        double z2 = Math.sin(latitude2 * HD);
        double xy2 = Math.cos(latitude2 * HD);
        double x2 = xy2 * Math.cos(longitude2 * HD);
        double y2 = xy2 * Math.sin(longitude2 * HD);

        double x0 = 0, y0 = 0, z0 = 0;
        double xn = 0, yn = 0, zn = 1;

        //求 0，1，n的平面方程法向量F1(A1,B1,C1)
        double A1 = (y1 - y0) * (zn - z0) - (z1 - z0) * (yn - y0);
        double B1 = (x1 - x0) * (zn - z0) - (z1 - z0) * (xn - x0);
        double C1 = (y1 - y0) * (xn - x0) - (x1 - x0) * (yn - y0);

        //求 0，1，2的平面方程法向量F2(A2,B2,C2)
        double A2 = (y1 - y0) * (z2 - z0) - (z1 - z0) * (y2 - y0);
        double B2 = (x1 - x0) * (z2 - z0) - (z1 - z0) * (x2 - x0);
        double C2 = (y1 - y0) * (x2 - x0) - (x1 - x0) * (y2 - y0);

        //求两个平面法向量的夹角(F1、F2)
        double labc = (Math.sqrt(Math.pow(A1, 2) + Math.pow(B1, 2) + Math.pow(C1, 2))
                * Math.sqrt(Math.pow(A2, 2) + Math.pow(B2, 2) + Math.pow(C2, 2)));
        if (labc == 0) {
            return 0; //0f/0f得出NaN
        }
        double cos = (A1 * A2 + B1 * B2 + C1 * C2) / labc;
        //cos值在[-1,1]之外时，Math.acos方法得出NaN
        if (cos > 1) {
            cos = 1;
        } else if (cos < -1) {
            cos = -1;
        }
        double theta = Math.acos(cos) / HD;

        //计算经度的正负号和校准基线
        int sign;
        if (longitude1 > longitude2) {
            sign = (360 + longitude2 - longitude1) >= 180 ? 1 : -1;
        } else {
            sign = (longitude2 - longitude1) >= 180 ? 1 : -1;
        }
        double longitudex = (sign > 0 ? theta : (360 - theta)) + 180;
        longitudex = longitudex % 360;
        return dealNaN(longitudex, 0, "getLongitudeAngle:%s,%s,%s,%s", longitude1, latitude1, longitude2, latitude2);
    }

    /**
     * 计算某个地理经度在特定时间点时，天球顶部对应的天球经度
     *
     * @param geographyLongitude 地理经度(东经为正，西经为负)
     * @param date               当地时区的时间
     */
    public static double getLongitudeOfDate(double geographyLongitude, Date date) {
        double longitudeSun = getLongitudeOfSun(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        double hour = calendar.get(Calendar.HOUR_OF_DAY);
        double minute = calendar.get(Calendar.MINUTE);
        double second = calendar.get(Calendar.SECOND);
        double longitudeOfDay = hour * 15 + minute / 4 + second / 240;
        double longitudeTimeZone = calendar.getTimeZone().getRawOffset() / 240000d; //当前所在时区对应的经度
        double longitude = longitudeSun + 180 + longitudeOfDay + (geographyLongitude - longitudeTimeZone) + 360;
        return longitude % 360;
    }

    /**
     * 计算指定日期太阳所在的天球经度
     */
    public static double getLongitudeOfSun(Date date) {
        long time = date.getTime() - SolarSystem.SPRING_EQUINOX; //采用[东八区时间]，其他时区的误差在(1/365)内
        double degree = time / SolarSystem.TROPICAL_YEAR / 86400000d * SolarSystem.TROPICAL_YEAR_DEGREE;
        return (degree % 360 + 360) % 360;
    }
}
