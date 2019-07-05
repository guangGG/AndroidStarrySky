package gapp.season.star;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import gapp.season.star.page.SkyBallActivity;
import gapp.season.star.solar.Solar;
import gapp.season.star.solar.SolarSystem;

public class SkyStar {
    private static boolean sIsDev; //开发模式会打印一些日志
    private static double sDefaultLuminance; //默认显示的最低亮度值
    private static int sPageTheme; //页面样式

    /**
     * 一些配置项设置(非必须，如需要建议在应用初始化时配置)
     *
     * @param isdev            是否开发者版本
     * @param defaultLuminance 设置展示的最低亮度，推荐值范围[0.8,2]，传0表示使用默认值
     * @param pageTheme        设置页面样式(传0使用应用默认样式)
     */
    public static void config(boolean isdev, double defaultLuminance, int pageTheme) {
        sIsDev = isdev;
        sDefaultLuminance = defaultLuminance;
        sPageTheme = pageTheme;
    }

    public static boolean isDev() {
        return sIsDev;
    }

    public static double getDefaultLuminance() {
        if (sDefaultLuminance <= 0)
            return 1;
        return sDefaultLuminance;
    }

    public static int getPageTheme() {
        if (sPageTheme <= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                sPageTheme = android.R.style.Theme_Material_Light_NoActionBar;
            } else {
                sPageTheme = android.R.style.Theme_Holo_Light_NoActionBar;
            }
        }
        return sPageTheme;
    }

    /**
     * 配置太阳系天体(上)合日基准日期(非必须)
     *
     * @param solar     太阳系天体
     * @param datumDate 天体(上)合日时的时间戳(ms)
     */
    public static void putDatumDate(Solar solar, long datumDate) {
        SolarSystem.putDatumDate(solar, datumDate);
    }

    /**
     * 打开星空页
     */
    public static void openSkyBall(Context context) {
        Intent intent = new Intent(context, SkyBallActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
