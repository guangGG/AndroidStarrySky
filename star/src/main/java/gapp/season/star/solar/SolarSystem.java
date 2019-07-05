package gapp.season.star.solar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import gapp.season.star.page.PageStarItem;
import gapp.season.star.util.StarCoordsUtil;
import gapp.season.star.util.StarLog;

/*太阳位置精确值：
2000年起，太阳每365.2422日(回归年，不确定值，取近百年左右的平均值)在2000年天球坐标系
运行[360°—50.260角秒](因为岁差春分点每年西移，每年西移值不确定，取近年平均的西移值)
基准点取2000年春分时间是 2000年03月20日 15:35:15 [东八区时间][其他时区误差在(1/365)°内]

星空观测岁差影响：
星空数据为以2000年春分为基点的坐标，由于岁差，每年春分点西移(目前大约百年会西移1°)
实际星空坐标在一直变化，但变化幅度很小，所以在星图中忽略处理。*/
public class SolarSystem {
    //太阳运行基础参数
    public static final double TROPICAL_YEAR = 365.2422; //1回归年天数
    public static final double TROPICAL_YEAR_DEGREE = 360 - 50.26 / 3600; //1回归年太阳运行度数
    public static final long SPRING_EQUINOX = 953537715000L; //2000天球坐标系春分点时间："2000-03-20 15:35:15" [东八区时间]
    //各天体(上)合日基准日期 [东八区时间]
    private static final String FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String CONJUNCTION_DATE_MOON = "2019-06-03 18:02:00";
    private static final String CONJUNCTION_DATE_MERCURY = "2019-05-21 21:07:00";
    private static final String CONJUNCTION_DATE_VENUS = "2019-08-14 14:00:00";
    private static final String CONJUNCTION_DATE_MARS = "2019-09-02 18:00:00";
    private static final String CONJUNCTION_DATE_JUPITER = "2018-11-26 14:25:00";
    private static final String CONJUNCTION_DATE_SATURN = "2019-01-02 13:50:00";
    private static final String CONJUNCTION_DATE_URANUS = "2019-04-26 16:30:00";
    private static final String CONJUNCTION_DATE_NEPTUNE = "2019-03-07 09:00:00";
    private static Map<Solar, Long> mDatumDates;

    /**
     * 初始化时可设置太阳系天体(上)合日基准日期
     */
    public static void putDatumDate(Solar solar, long datumDate) {
        if (mDatumDates == null)
            mDatumDates = new HashMap<>();
        mDatumDates.put(solar, datumDate);
    }

    /**
     * 获取太阳系天体(上)合日基准日期
     */
    public static Date getDatumDate(Solar solar) {
        try {
            if (mDatumDates != null) {
                Long time = mDatumDates.get(solar);
                if (time != null) {
                    return new Date(time);
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_PATTERN, Locale.CHINA);
            switch (solar) {
                case Moon:
                    return sdf.parse(CONJUNCTION_DATE_MOON);
                case Mercury:
                    return sdf.parse(CONJUNCTION_DATE_MERCURY);
                case Venus:
                    return sdf.parse(CONJUNCTION_DATE_VENUS);
                case Mars:
                    return sdf.parse(CONJUNCTION_DATE_MARS);
                case Jupiter:
                    return sdf.parse(CONJUNCTION_DATE_JUPITER);
                case Saturn:
                    return sdf.parse(CONJUNCTION_DATE_SATURN);
                case Uranus:
                    return sdf.parse(CONJUNCTION_DATE_URANUS);
                case Neptune:
                    return sdf.parse(CONJUNCTION_DATE_NEPTUNE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static PageStarItem getStar(Solar solar) {
        return getStar(solar, new Date());
    }

    public static PageStarItem getStar(Solar solar, Date date) {
        PageStarItem skyStar = new PageStarItem();
        skyStar.setType(solar.getType());
        skyStar.setSolar(solar);
        skyStar.setId(solar.getId());
        skyStar.setName(solar.getName());
        skyStar.setLuminance(solar.getShowLuminance());
        double longitude = getgetEclipticLongitude(solar, date);
        double latitude = getEclipticLatitude(longitude);
        skyStar.setLongitude(longitude);
        skyStar.setLatitude(latitude);
        //亮度及形状调整
        if (solar == Solar.Moon) {
            double longitudeSun = StarCoordsUtil.getLongitudeOfSun(date);
            double factor = (longitude - longitudeSun + 360) % 360 / 180;
            skyStar.setMoonShape((float) (factor > 1 ? (factor - 2) : factor)); //月芽的形状，取值范围[-1,1]，负数表示下半月
        } else if (solar.getType() == 1) {
            //行星的亮度是反射阳光的，与太阳夹角相关，这里动态模拟展示亮度
            double luminance = solar.getShowLuminance();
            double minLuminance = Math.sqrt(luminance); //视觉模拟值
            double maxAngle = solar.getMaxAngle();
            double longitudeSun = StarCoordsUtil.getLongitudeOfSun(date);
            double angle = Math.abs(longitude - longitudeSun);
            angle = angle > 180 ? (360 - angle) : angle;
            if (angle < maxAngle) { //正常情况当前夹角比最大夹角小，这里的判断防止极端情况
                double showLuminance = minLuminance + (luminance - minLuminance) * angle / maxAngle;
                skyStar.setLuminance(showLuminance);
            }
            skyStar.setMoonShape((float) angle / 180); //取值范围[0,1]，不用于展示(地球上看不出行星光的形状)
        }
        StarLog.d("SolarSystem", skyStar.toString() + " ~ angle:"
                + Math.round(skyStar.getMoonShape() * 180) + "° ~ " + date.toLocaleString());
        return skyStar;
    }

    public static double getgetEclipticLongitude(Solar solar, Date date) {
        try {
            switch (solar) {
                case Sun:
                    return StarCoordsUtil.getLongitudeOfSun(date);
                case Moon: {
                    return getCircularMotionLong(solar, getDatumDate(solar), date);
                }
                //内行星轨道变化
                case Mercury:
                case Venus:
                    return getInnerPlanetLong(solar, getDatumDate(solar), date);
                //外行星轨道变化
                case Mars:
                    return getOuterPlanetLong(solar, getDatumDate(solar), date);
                //木星外行星轨道半径比地球大得多，也可近似按绕地处理[getCircularMotionLong](由于对地轨道偏心率，结果不怎么精确)
                case Jupiter:
                case Saturn:
                case Uranus:
                case Neptune:
                    return getOuterPlanetLong(solar, getDatumDate(solar), date);
                //return getCircularMotionLong(solar, getDatumDate(solar), date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 计算内行星位置
     */
    private static double getInnerPlanetLong(Solar solar, Date baseDate, Date date) {
        long interval = date.getTime() - baseDate.getTime(); //秒
        double p1 = solar.getRevolutionPeriod(); //天
        double p0 = Solar.Earth.getRevolutionPeriod(); //天
        double angle1 = 360d * interval / p1 / 24 / 3600000;
        double angle0 = 360d * interval / p0 / 24 / 3600000;
        double anglex = angle1 - angle0; //相差角度
        //构造直角坐标系，太阳[0,0]，地球[-r0,0]，内行星[r1*cos(anglex),r1*sin(anglex)]
        //使用向量法：求内行星和太阳对地球的角度:平面向量夹角公式：cos=(ab的内积)/(|a||b|)
        //地日向量(1,0)即x轴，地星向量(r1*cos(anglex)+r0, r1*sin(anglex))
        //这里直接用勾股定理求出夹角
        double angle = Math.atan((solar.getOrbitRadius() * Math.sin(anglex * Math.PI / 180)) /
                (1 + solar.getOrbitRadius() * Math.cos(anglex * Math.PI / 180))) * 180 / Math.PI; //星地日夹角(范围-90~90°)
        double longitudeSunNow = StarCoordsUtil.getLongitudeOfSun(date);
        return (longitudeSunNow + angle + 360) % 360;
    }

    /**
     * 计算外行星位置
     */
    private static double getOuterPlanetLong(Solar solar, Date baseDate, Date date) {
        long interval = date.getTime() - baseDate.getTime(); //秒
        double p1 = solar.getRevolutionPeriod(); //天
        double p0 = Solar.Earth.getRevolutionPeriod(); //天
        double angle1 = 360d * interval / p1 / 24 / 3600000;
        double angle0 = 360d * interval / p0 / 24 / 3600000;
        double anglex = angle1 - angle0; //相差角度
        //构造直角坐标系，太阳[0,0]，地球[-r0,0]，外行星[r1*cos(anglex),r1*sin(anglex)]
        //地日向量(1,0)即x轴，地星向量(r1*cos(anglex)+r0, r1*sin(anglex))
        //这里直接用勾股定理求出夹角
        double fy = (solar.getOrbitRadius() * Math.sin(anglex * Math.PI / 180));
        double fx = (1 + solar.getOrbitRadius() * Math.cos(anglex * Math.PI / 180));
        double angle;
        if (fx == 0) {
            angle = fy > 0 ? 90 : -90;
        } else {
            double angley = Math.atan(fy / fx) * 180 / Math.PI; //星地日夹角(范围-90~90°)
            angle = fx > 0 ? angley : (angley + 180);
        }
        double longitudeSunNow = StarCoordsUtil.getLongitudeOfSun(date);
        return (longitudeSunNow + angle + 360) % 360;
    }

    /**
     * 获取绕地星体的当前经度
     *
     * @param solar    绕地类正圆运动的星体(月、木、土)
     * @param baseDate 基准校对日期(合日点)
     */
    private static double getCircularMotionLong(Solar solar, Date baseDate, Date date) {
        long interval = date.getTime() - baseDate.getTime();
        long period = (long) (solar.getSurroundPeriod() * 24 * 3600000);
        long time = (interval % period + period) % period; //变成非负数
        double angle = 360d * time / period;
        if (solar.getType() == 1) {
            //行星对地球是相对太阳的period，所以要算上太阳的运动角度(以地球为基准外行星相当于太阳是反方向绕行的)
            double longitudeSunNow = StarCoordsUtil.getLongitudeOfSun(date);
            return (longitudeSunNow - angle + 360) % 360;
        } else {
            //月球绕地球正圆运动，直接在基点基础上增加角度
            double longitudeSun = StarCoordsUtil.getLongitudeOfSun(baseDate);
            return (longitudeSun + angle + 360) % 360;
        }
    }

    //黄道范围为正负23°26′[23.433°]，经度0-180°时在北纬，180-360°时在南纬
    public static double getEclipticLatitude(double longitude) {
        return Math.sin(longitude * Math.PI / 180) * 23.433;
    }
}
