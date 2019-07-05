package gapp.season.star.page;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import gapp.season.star.R;
import gapp.season.star.SkyStar;
import gapp.season.star.bsc5.BscStarSheet;
import gapp.season.star.bsc5.Star;
import gapp.season.star.bsc5.StarCnName;
import gapp.season.star.solar.Solar;
import gapp.season.star.solar.SolarSystem;
import gapp.season.star.util.StarCoordsUtil;
import gapp.season.star.util.StarLog;
import gapp.season.star.util.StarPreferenceUtil;
import gapp.season.star.view.SkyView;

public class SkyBallActivity extends Activity {
    private SkyView mSkyView;
    private TextView mTipsView;
    private Button mRunView;

    private List<Star> mStarList; //所有恒星列表
    private List<PageStarItem> mSkyStarList; //需要展示的天体列表

    private LocationManager mLocationManager;
    private boolean mIsBlackSky;
    private boolean mIsShowAll;
    private int mRunSpeed = 720; //每分钟运行度数
    private boolean mIsShowCnName = true;
    private boolean mIsShowSunMoon = true;
    private boolean mIsShowPlanet = true;

    private Timer mTimer;
    private double mAutoLong;
    private double mAutoLati;
    private long mAutoTime;

    private long mShowDate = Long.MIN_VALUE; //当前展示的时间点(Long.MIN_VALUE 时表示使用系统时间)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //可自定义样式
        setTheme(SkyStar.getPageTheme());
        setContentView(R.layout.ss_activity_sky_ball);
        setUp();
    }

    private void setUp() {
        //动态设置天球View的高度(屏幕适配)
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int size = Math.min(wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight());
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) findViewById(R.id.ss_rl_sky_ball).getLayoutParams();
        linearParams.height = size;
        findViewById(R.id.ss_rl_sky_ball).setLayoutParams(linearParams);

        mSkyView = findViewById(R.id.ss_sky_view);
        mTipsView = findViewById(R.id.ss_tv_skyball_location);
        mRunView = findViewById(R.id.ss_btn_run);
        ((Switch) findViewById(R.id.ss_star_text_switch)).setOnCheckedChangeListener(
                (buttonView, isChecked) -> mSkyView.setGestureRotate(isChecked));
        ((SeekBar) findViewById(R.id.ss_seekbar_line_num)).setOnSeekBarChangeListener(new SeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) findViewById(R.id.ss_progress_seekbar_line_num)).setText(String.valueOf(progress));
                mSkyView.setStandardLineNum(progress, progress * 4);
            }
        });
        ((SeekBar) findViewById(R.id.ss_seekbar_sky_scale)).setOnSeekBarChangeListener(new SeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) findViewById(R.id.ss_progress_seekbar_sky_scale)).setText(String.valueOf(progress));
                double scale = Math.pow(2, (progress - 50) / 15d);
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                int widthScreen = wm.getDefaultDisplay().getWidth();
                LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mSkyView.getLayoutParams();
                linearParams.width = (int) (widthScreen * scale);
                linearParams.height = (int) (widthScreen * scale);
                mSkyView.setLayoutParams(linearParams);
            }
        });
        ((SeekBar) findViewById(R.id.ss_seekbar_scale)).setOnSeekBarChangeListener(new SeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) findViewById(R.id.ss_progress_seekbar_scale)).setText(String.valueOf(progress));
                double scale = Math.pow(2, (progress - 50) / 15d);
                mSkyView.setScale(scale);
            }
        });
        ((SeekBar) findViewById(R.id.ss_seekbar_luminance_factor)).setOnSeekBarChangeListener(new SeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) findViewById(R.id.ss_progress_seekbar_luminance_factor)).setText(String.valueOf(progress));
                if (progress > 50) {
                    mSkyView.setLuminanceFactor(2.5 - 1.5 / 50 * (progress - 50));
                } else {
                    mSkyView.setLuminanceFactor(2.5 - 7.5 / 50 * (progress - 50));
                }
            }
        });
        ((SeekBar) findViewById(R.id.ss_seekbar_name_factor)).setOnSeekBarChangeListener(new SeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) findViewById(R.id.ss_progress_seekbar_name_factor)).setText(String.valueOf(progress));
                if (progress == 0) {
                    mSkyView.setNameFactor(0);
                } else if (progress > 50) {
                    mSkyView.setNameFactor((2 - 1.8f / 50 * (progress - 50)));
                } else {
                    mSkyView.setNameFactor((2 - 18f / 50 * (progress - 50)));
                }
            }
        });
        ((SeekBar) findViewById(R.id.ss_seekbar_run_speed)).setOnSeekBarChangeListener(new SeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) findViewById(R.id.ss_progress_seekbar_run_speed)).setText(String.valueOf(progress));
                mRunSpeed = (int) (720 * Math.pow(2, (progress - 50) / 10d));
            }
        });
        findViewById(R.id.ss_btn_select_date).setOnLongClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.ss_restore_date_tips));
            builder.setPositiveButton(R.string.ss_ok, (dialog, which) -> {
                mShowDate = Long.MIN_VALUE;
                initStars();
            });
            builder.setNegativeButton(R.string.ss_cancel, null);
            builder.show();
            return true;
        });
        mSkyView.setOnStarClickListener((star, isLongClick) -> {
            StarLog.v("SkyView", "OnStarClick:" + star + "; LongClick:"
                    + isLongClick + "; ts:" + System.currentTimeMillis());
            if (star != null && isLongClick) {
                PageStarItem cacheStar = null;
                if (mSkyStarList != null) {
                    for (PageStarItem starItem : mSkyStarList) {
                        if (starItem != null && TextUtils.equals(starItem.getId(), star.getId())) {
                            cacheStar = starItem; //star的坐标可能是拖拽成其它数值的，需要用id匹配到实际星体
                            break;
                        }
                    }
                }
                PageStarItem starData = cacheStar;
                if (starData != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.ss_star_info);
                    builder.setMessage(starData.getStarData());
                    builder.setPositiveButton(R.string.ss_location_top, (dialog, which) ->
                            showSkyStars(starData.getLongitude(), starData.getLatitude()));
                    builder.setNegativeButton(R.string.ss_location_bottom, (dialog, which) ->
                            showSkyStars((starData.getLongitude() + 180) % 360, -starData.getLatitude()));
                    builder.show();
                }
            }
        });

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        initStars();
    }

    private void initStars() {
        double minLuminance = mIsShowAll ? 0 : SkyStar.getDefaultLuminance();
        new Thread(() -> {
            try {
                if (mStarList == null) {
                    AssetManager am = getResources().getAssets();
                    InputStream is = am.open("bsc5.dat");
                    mStarList = BscStarSheet.fromData(is);
                }
                List<PageStarItem> skyStars = new ArrayList<>();
                for (Star star : mStarList) {
                    double luminance = star.getLuminance();
                    if (luminance >= minLuminance) { //在一定亮度以上(5等星为1)
                        PageStarItem skyStar = new PageStarItem();
                        skyStar.setId(star.index);
                        if (mIsShowCnName) {
                            String cnName = StarCnName.getCnName(skyStar.getId());
                            skyStar.setName(TextUtils.isEmpty(cnName) ? star.getConstellationName() : cnName);
                        } else {
                            skyStar.setName(star.getConstellationName());
                        }
                        skyStar.setLuminance(luminance);
                        skyStar.setLongitude(star.getLongitude(false));
                        skyStar.setLatitude(star.getLatitude(false));
                        skyStar.setStar(star);
                        skyStars.add(skyStar);
                    }
                }
                Collections.sort(skyStars, (o1, o2) -> { //亮度从小到大，亮星会覆盖在上面，优先响应点击
                    double d = o1.getLuminance() - o2.getLuminance();
                    if (d > 0)
                        return 1;
                    else if (d < 0)
                        return -1;
                    else
                        return 0;
                });
                Date planetDate = new Date(getShowTime());
                if (mIsShowSunMoon) {
                    skyStars.add(SolarSystem.getStar(Solar.Sun, planetDate));
                    skyStars.add(SolarSystem.getStar(Solar.Moon, planetDate));
                }
                if (mIsShowPlanet) {
                    skyStars.add(SolarSystem.getStar(Solar.Venus, planetDate));
                    skyStars.add(SolarSystem.getStar(Solar.Jupiter, planetDate));
                    skyStars.add(SolarSystem.getStar(Solar.Mercury, planetDate));
                    skyStars.add(SolarSystem.getStar(Solar.Mars, planetDate));
                    skyStars.add(SolarSystem.getStar(Solar.Saturn, planetDate));
                    skyStars.add(SolarSystem.getStar(Solar.Uranus, planetDate));
                    skyStars.add(SolarSystem.getStar(Solar.Neptune, planetDate));
                }
                mSkyStarList = skyStars;
                toMyLocation(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showSkyStars(double longitude, double latitude) {
        showSkyStars(mSkyStarList, longitude, latitude, false);
    }

    private void showSkyStars(List<PageStarItem> skyStarList, double longitude, double latitude, boolean isAuto) {
        if (!isAuto) {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            mAutoLong = longitude;
            mAutoLati = latitude;
            mAutoTime = getShowTime();
            showTips(new Date(getShowTime()), longitude, latitude, false);
        } else {
            showTips(new Date(mAutoTime), mAutoLong, mAutoLati, true);
        }
        new Thread(() -> {
            try {
                List<SkyView.StarItem> list = new ArrayList<>();
                for (PageStarItem star : skyStarList) {
                    SkyView.StarItem skyStar = new SkyView.StarItem();
                    skyStar.setType(star.getType());
                    skyStar.setId(star.getId());
                    skyStar.setName(star.getName());
                    skyStar.setLuminance(star.getLuminance());
                    skyStar.setLongitude(StarCoordsUtil.getLongitudeAngle(longitude, latitude, star.getLongitude(), star.getLatitude()));
                    skyStar.setLatitude(StarCoordsUtil.getLatitudeAngle(longitude, latitude, star.getLongitude(), star.getLatitude()));
                    skyStar.setMoonShape(star.getMoonShape());
                    list.add(skyStar);
                }
                runOnUiThread(() -> mSkyView.setData(list));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showTips(Date date, double longitude, double latitude, boolean isAuto) {
        runOnUiThread(() -> {
            boolean isRunning = (mTimer != null);
            if (isRunning || !isAuto) {
                mTipsView.setVisibility(View.VISIBLE);
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d H:mm"); //yyyy-MM-dd HH:mm:ss"
                DecimalFormat df = new DecimalFormat("0.0");
                mTipsView.setText(String.format("%s\n[%s°,%s°]", sdf.format(date), df.format(longitude), df.format(latitude)));
            }
            mRunView.setText(isRunning ? R.string.ss_stop : R.string.ss_run);
        });
    }

    //从系统读取并更新一次当前地理定位
    private void requestLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        if (providers != null) {
            for (String provider : providers) {
                @SuppressLint("MissingPermission")
                Location location = mLocationManager.getLastKnownLocation(provider);
                if (location != null) {
                    double localLongitude = location.getLongitude();
                    double localLatitude = location.getLatitude();
                    StarLog.i("LocationManager", String.format("%s:%s,%s", provider, localLongitude, localLatitude));
                    StarPreferenceUtil.saveLocation(getApplicationContext(), localLongitude, localLatitude);
                    break;
                }
            }
        }
    }

    public void runSkyBall(View view) {
        requestLocation();
        if (mTimer != null) {
            mTimer.cancel(); //关闭旧的Timer
            mTimer = null;
            showTips(new Date(mAutoTime), mAutoLong, mAutoLati, false);
        } else {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    double range = mRunSpeed / 60000d * 40; //每分钟n°
                    mAutoLong = (mAutoLong + range) % 360;
                    mAutoTime = (long) (getShowTime() + (mAutoTime + range * 240000 - getShowTime()) % 86400000);
                    showSkyStars(mSkyStarList, mAutoLong, mAutoLati, true);
                }
            }, 0, 40);
        }
    }

    public void toMyLocation(View view) {
        requestLocation();
        double localLongitude = StarPreferenceUtil.getLocation(this, false);
        double localLatitude = StarPreferenceUtil.getLocation(this, true);
        double longitude = StarCoordsUtil.getLongitudeOfDate(localLongitude, new Date(getShowTime()));
        showSkyStars(longitude, localLatitude);
    }

    public void toNorthPole(View view) {
        showSkyStars(0, 90);
    }

    public void toSouthPole(View view) {
        showSkyStars(180, -90);
    }

    public void toSpringPole(View view) {
        showSkyStars(180, 0);
    }

    public void toSummerPole(View view) {
        showSkyStars(270, 0);
    }

    public void toAutumnPole(View view) {
        showSkyStars(0, 0);
    }

    public void toWinterPole(View view) {
        showSkyStars(90, 0);
    }

    public void changeSkyColor(View view) {
        if (mIsBlackSky) {
            mSkyView.setColor(Color.WHITE, 0XFF3670A0, Color.BLACK, 0X66FFFFFF,
                    0XF0FFFFFF, 0XFFFFD700, 0XFF333333);
        } else {
            mSkyView.setColor(0XFFBBBBBB, Color.BLACK, 0xFF3399DD, 0x993399DD,
                    Color.WHITE, 0XFFFFC125, 0xFF3399DD);
        }
        mIsBlackSky = !mIsBlackSky;
    }

    public void doSkyOption(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] items = new String[6];
        items[0] = getString(R.string.ss_screenshot_skyball);
        items[1] = getString(R.string.ss_show_all_star) + (mIsShowAll ? getString(R.string.ss_yes) : getString(R.string.ss_no));
        items[2] = getString(R.string.ss_show_star_cn_name) + (mIsShowCnName ? getString(R.string.ss_yes) : getString(R.string.ss_no));
        items[3] = getString(R.string.ss_show_sun_moon) + (mIsShowSunMoon ? getString(R.string.ss_yes) : getString(R.string.ss_no));
        items[4] = getString(R.string.ss_show_solar_planet) + (mIsShowPlanet ? getString(R.string.ss_yes) : getString(R.string.ss_no));
        items[5] = getString(R.string.ss_select_sky_time);
        builder.setItems(items, (dialog, which) -> {
            switch (which) {
                case 0:
                    screenShot();
                    break;
                case 1:
                    mIsShowAll = !mIsShowAll;
                    initStars();
                    break;
                case 2:
                    mIsShowCnName = !mIsShowCnName;
                    initStars();
                    break;
                case 3:
                    mIsShowSunMoon = !mIsShowSunMoon;
                    initStars();
                    break;
                case 4:
                    mIsShowPlanet = !mIsShowPlanet;
                    initStars();
                    break;
                case 5:
                    pickTime();
                    break;
            }
        });
        builder.show();
    }

    public void screenShot() {
        try {
            mSkyView.setDrawingCacheEnabled(true); //开启缓存
            Bitmap bitmapTemp = mSkyView.getDrawingCache(); //获取bitmap(view尺寸过大时获取的bitmap可能为空)
            if (bitmapTemp != null) {
                FileOutputStream fos = new FileOutputStream(new File(getExternalFilesDir(null),
                        String.format(getString(R.string.ss_screenshot_file_name), System.currentTimeMillis())));
                bitmapTemp.compress(Bitmap.CompressFormat.JPEG, 75, fos);

                fos.close();
                mSkyView.setDrawingCacheEnabled(false); //关闭缓存
                Toast.makeText(this, getString(R.string.ss_screenshot_s), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, getString(R.string.ss_screenshot_f), Toast.LENGTH_SHORT).show();
    }

    public void restoreSkyBall(View view) {
        if (mIsBlackSky) changeSkyColor(view);
        ((Switch) findViewById(R.id.ss_star_text_switch)).setChecked(true);
        ((SeekBar) findViewById(R.id.ss_seekbar_line_num)).setProgress(0);
        ((SeekBar) findViewById(R.id.ss_seekbar_sky_scale)).setProgress(50);
        ((SeekBar) findViewById(R.id.ss_seekbar_scale)).setProgress(50);
        ((SeekBar) findViewById(R.id.ss_seekbar_luminance_factor)).setProgress(50);
        ((SeekBar) findViewById(R.id.ss_seekbar_name_factor)).setProgress(50);
        ((SeekBar) findViewById(R.id.ss_seekbar_run_speed)).setProgress(50);
    }

    public long getShowTime() {
        if (mShowDate == Long.MIN_VALUE) {
            return System.currentTimeMillis();
        }
        return mShowDate;
    }

    public void pickDate(View view) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getShowTime());
        DatePickerDialog pickerDialog = new DatePickerDialog(this, (v, year, month, dayOfMonth) -> {
            Calendar calendarPick = Calendar.getInstance();
            calendarPick.setTimeInMillis(getShowTime());
            calendarPick.set(year, month, dayOfMonth);
            mShowDate = calendarPick.getTimeInMillis();
            initStars();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show();
    }

    private void pickTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getShowTime());
        TimePickerDialog pickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            Calendar calendarPick = Calendar.getInstance();
            calendarPick.setTimeInMillis(getShowTime());
            calendarPick.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendarPick.set(Calendar.MINUTE, minute);
            mShowDate = calendarPick.getTimeInMillis();
            initStars();
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        pickerDialog.show();
    }

    public void closePage(View view) {
        finish();
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
