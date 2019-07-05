package gapp.season.star.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.Nullable;
import gapp.season.star.util.StarCoordsUtil;

public class SkyView extends View {
    private static final double HD = (Math.PI / 180); //弧度/角度的值
    public static final int SCALE_STANDARD_NORMAL = 720;
    public static final double LUMINANCE_FACTOR_NORMAL = 2.5;
    public static final float NAME_FACTOR_NORMAL = 2;
    private static final float STAR_TEXT_SIZE = 8; //文字大小(dp)
    private static final int STAR_TEXT_LENGTH = 10; //最长显示字数

    private Paint mPaint;
    private double mScaleStandard = SCALE_STANDARD_NORMAL;
    private double mLuminanceFactor = LUMINANCE_FACTOR_NORMAL;
    private float mNameFactor = NAME_FACTOR_NORMAL;
    private int mStandardLineLat;
    private int mStandardLineLon;
    private int mBgColor = Color.WHITE;
    private int mStarBgColor = 0XFF3670A0;
    private int mOutLineColor = Color.BLACK;
    private int mLineColor = 0X66FFFFFF;
    private int mStarColor = 0XF0FFFFFF;
    private int mSunColor = 0XFFFFC125;
    private int mStarTextColor = 0XFF333333;
    private List<StarItem> mStars;

    private Path mPathMoon;
    private Path mPathRect;
    private Path mPathOval;

    private GestureDetector mGestureDetector;
    private boolean mGestureRotate = true; //是否允许拖拽转动球体
    private OnStarClickListener mStarClickListener; //天体点击监听器

    public SkyView(Context context) {
        this(context, null);
    }

    public SkyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true; //返回true时才会回调onScroll/onFling事件
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!mGestureRotate) {
                    return false;
                }
                try {
                    int width = getWidth();
                    if (width != 0 && (distanceX != 0 || distanceY != 0)) {
                        double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
                        double theta;
                        if (distanceY == 0) {
                            theta = distanceX > 0 ? 90 : 270;
                        } else {
                            theta = Math.atan(-distanceX / distanceY) / HD; //-90~90°
                            if (distanceY > 0) {
                                theta = theta + 180;
                            } else if (distanceX < 0) {
                                theta = theta + 360;
                            }
                        }
                        distance = StarCoordsUtil.dealNaN(distance, 0, "onScroll:%s,%s", distanceX, distanceY);
                        theta = StarCoordsUtil.dealNaN(theta, 0, "onScroll:%s,%s", distanceX, distanceY);
                        rotate(180 * distance / width, theta);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) { //单击
                onStarClick(e, false);
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) { //长按
                onStarClick(e, true);
            }

            private void onStarClick(MotionEvent e, boolean isLongClick) {
                if (mStarClickListener != null) {
                    float x = e.getX();
                    float y = e.getY();
                    StarItem optStar = null;
                    if (mStars != null) {
                        for (int i = mStars.size() - 1; i >= 0; i--) {
                            StarItem star = mStars.get(i);
                            if (star != null && Math.pow(x - star.x, 2) + Math.pow(y - star.y, 2) <= Math.pow(star.r, 2)) {
                                optStar = star;
                                break;
                            }
                        }
                    }
                    mStarClickListener.onStarClick(optStar, isLongClick);
                }
            }
        });
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetector != null) {
                    return mGestureDetector.onTouchEvent(event);
                }
                return false;
            }
        });
    }

    /**
     * 设置星图颜色参数
     *
     * @param bgColor       整个画布背景色
     * @param starBgColor   星空背景色
     * @param outLineColor  星空轮廓线颜色
     * @param lineColor     星空分割线颜色
     * @param starColor     星星颜色
     * @param sunColor      日月五行颜色
     * @param starTextColor 星星名称文字颜色
     */
    public void setColor(int bgColor, int starBgColor, int outLineColor, int lineColor, int starColor, int sunColor, int starTextColor) {
        mBgColor = bgColor;
        mStarBgColor = starBgColor;
        mOutLineColor = outLineColor;
        mLineColor = lineColor;
        mStarColor = starColor;
        mSunColor = sunColor;
        mStarTextColor = starTextColor;
        postInvalidate(); //invalidate();
    }

    /**
     * 设置星星缩放比例(比例越大，星星显示的越大)
     */
    public void setScale(double scale) {
        if (scale > 0) {
            mScaleStandard = SCALE_STANDARD_NORMAL / scale;
            postInvalidate(); //invalidate();
        }
    }

    /**
     * 设置亮度系数(默认为:LUMINANCE_FACTOR_NORMAL)
     *
     * @param luminanceFactor 系数越大，不同亮度星星显示大小越接近
     */
    public void setLuminanceFactor(double luminanceFactor) {
        if (luminanceFactor > 0) {
            mLuminanceFactor = luminanceFactor;
            postInvalidate(); //invalidate();
        }
    }

    /**
     * 设置显示名称系数(默认为:NAME_FACTOR_NORMAL)
     *
     * @param nameFactor 系数越大，显示名称的星星数量越少 (小于等于0时不展示名称)
     */
    public void setNameFactor(float nameFactor) {
        mNameFactor = nameFactor;
        postInvalidate(); //invalidate();
    }

    /**
     * 设置纬度基准线数量
     *
     * @param latitudeLineNum  纬度圈数量(以30°分割则为3)
     * @param longitudeLineNum 经度线数量(以30°分割则为12)
     */
    public void setStandardLineNum(int latitudeLineNum, int longitudeLineNum) {
        mStandardLineLat = latitudeLineNum;
        mStandardLineLon = longitudeLineNum;
        postInvalidate(); //invalidate();
    }

    /**
     * 设置星星数据
     */
    public void setData(List<StarItem> starList) {
        this.mStars = starList;
        postInvalidate(); //invalidate();
    }

    /**
     * 设置是否开启手势控制球体转动(默认开启)
     */
    public void setGestureRotate(boolean gestureRotate) {
        mGestureRotate = gestureRotate;
    }

    /**
     * 极点向指定方向旋转(效果看起来是极点向该方向平移)
     *
     * @param angle     旋转的度数(0~180°)
     * @param direction 旋转的方向[0下,90左,180上,270右](0~360°)
     */
    public void rotate(double angle, double direction) {
        if (mStars != null) {
            double longitude = direction + 180;
            longitude = longitude > 360 ? (longitude - 360) : longitude;
            double latitude = 90 - angle;
            for (StarItem star : mStars) {
                double longitudeN = StarCoordsUtil.getLongitudeAngle(longitude, latitude, star.longitude, star.latitude) + longitude;
                longitudeN = longitudeN > 360 ? (longitudeN - 360) : longitudeN;
                double latitudeN = StarCoordsUtil.getLatitudeAngle(longitude, latitude, star.longitude, star.latitude);
                star.longitude = longitudeN;
                star.latitude = latitudeN;
            }
            postInvalidate(); //invalidate();
        }
    }

    public void setOnStarClickListener(OnStarClickListener starClickListener) {
        mStarClickListener = starClickListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        float size = Math.min(width, height);
        if (size > 0) {
            float r = size / 2;
            if (mPaint == null) {
                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //设置画布图像无锯齿
            }
            //画布背景
            canvas.drawColor(mBgColor);
            //星空背景
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mStarBgColor);
            canvas.drawCircle(r, r, r, mPaint);
            //画天球的基准线
            mPaint.setStyle(Paint.Style.STROKE); //mPaint.setStrokeWidth(1);
            mPaint.setColor(mOutLineColor);
            canvas.drawCircle(r, r, r, mPaint);
            mPaint.setColor(mLineColor);
            for (int i = 0; i < mStandardLineLat; i++) {
                if (i > 0) {
                    float radius = r / mStandardLineLat * i;
                    canvas.drawCircle(r, r, radius, mPaint);
                }
            }
            if (mStandardLineLon > 0) {
                for (int i = 0; i < mStandardLineLon; i++) {
                    double theta = 2 * Math.PI / mStandardLineLon * i;
                    canvas.drawLine(r, r, (float) (r + r * Math.sin(theta)), (float) (r - r * Math.cos(theta)), mPaint);
                }
            }
            //画星星
            float density = getContext().getResources().getDisplayMetrics().density;
            float textsize = density * STAR_TEXT_SIZE;
            mPaint.setTextSize(textsize);
            if (mStars != null && mStars.size() > 0) {
                mPaint.setStyle(Paint.Style.FILL);
                for (StarItem star : mStars) {
                    if (star.getLatitude() >= 0) {
                        double starCl = r * ((90 - star.getLatitude()) / 90);
                        double starCx = starCl * Math.sin(star.getLongitude() * HD);
                        double starCy = starCl * Math.cos(star.getLongitude() * HD);
                        float starX = (float) (r - starCx);
                        float starY = (float) (r + starCy);
                        double scaleStandard = mScaleStandard > 0 ? mScaleStandard : SCALE_STANDARD_NORMAL;
                        double luminanceFactor = mLuminanceFactor > 0 ? mLuminanceFactor : LUMINANCE_FACTOR_NORMAL;
                        float radius = (float) (Math.pow(star.getLuminance(), 1 / luminanceFactor) * size / scaleStandard);
                        //计算starCx、starCy的结果可能为:NaN，需要处理一下
                        starX = (float) StarCoordsUtil.dealNaN(starX, r, "onDraw:%s", star);
                        starY = (float) StarCoordsUtil.dealNaN(starY, r, "onDraw:%s", star);
                        radius = (float) StarCoordsUtil.dealNaN(radius, 0, "onDraw:%s", star);
                        star.x = starX;
                        star.y = starY;
                        star.r = radius;
                        mPaint.setColor(star.getType() == 0 ? mStarColor : mSunColor);
                        if (star.getType() == 2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            float shape = star.getMoonShape(); //取值范围[-1,1]
                            if (mPathMoon == null) mPathMoon = new Path();
                            mPathMoon.reset();
                            mPathMoon.addCircle(starX, starY, radius, Path.Direction.CW);
                            if (shape > -1 && shape < 1) {
                                if (shape == 0) {
                                    mPaint.setStyle(Paint.Style.STROKE);
                                } else {
                                    if (mPathRect == null) mPathRect = new Path();
                                    mPathRect.reset();
                                    if (shape > 0) {
                                        mPathRect.addRect(starX - radius, starY + radius, starX,
                                                starY - radius, Path.Direction.CW);
                                    } else {
                                        mPathRect.addRect(starX, starY + radius, starX + radius,
                                                starY - radius, Path.Direction.CW);
                                    }
                                    if (shape == 0.5f || shape == -0.5f) {
                                        mPathMoon.op(mPathRect, Path.Op.DIFFERENCE);
                                    } else {
                                        mPathMoon.op(mPathRect, Path.Op.DIFFERENCE);
                                        if (mPathOval == null) mPathOval = new Path();
                                        mPathOval.reset();
                                        float positive = Math.abs(shape);
                                        float multiple = (positive - 0.5f) * 2;
                                        mPathOval.addOval(starX - multiple * radius, starY + radius,
                                                starX + multiple * radius, starY - radius, Path.Direction.CW);
                                        if (positive > 0.5f) {
                                            mPathMoon.op(mPathOval, Path.Op.UNION);
                                        } else {
                                            mPathMoon.op(mPathOval, Path.Op.DIFFERENCE);
                                        }
                                    }
                                }
                            }
                            canvas.drawPath(mPathMoon, mPaint);
                            mPaint.setStyle(Paint.Style.FILL);
                        } else {
                            canvas.drawCircle(starX, starY, radius, mPaint);
                        }
                        //画星星名字
                        if (mNameFactor > 0 && !TextUtils.isEmpty(star.getName())) {
                            if (radius > mNameFactor * density) {
                                String name = star.getName().length() > STAR_TEXT_LENGTH ?
                                        (star.getName().substring(0, STAR_TEXT_LENGTH) + "…") : star.getName();
                                float textW = textsize * name.length();
                                float textH = textsize * 1.1f; //预留0.1倍文字大小的间隙
                                mPaint.setColor(mStarTextColor);
                                canvas.drawText(name, starX - textW / 2, starY + radius + textH, mPaint);
                            }
                        }
                    }
                }
            }
        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取view设置的宽高
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //设置wrap_content的默认宽 / 高值
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int widthScreen = wm.getDefaultDisplay().getWidth();
        int heightScreen = wm.getDefaultDisplay().getHeight();
        int size = Math.min(widthScreen, heightScreen);
        //当布局参数设置为wrap_content时，设置默认值
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(size, size);
        } else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(size, heightSize);
        } else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, size);
        }
    }

    public static class StarItem {
        private String id;
        private String name;
        private double longitude;
        private double latitude;
        private double luminance;
        private int type; //0恒星，1行星，2月球，3太阳
        private float moonShape; //月芽的形状，取值范围[-1,1]，负数表示下半月
        // 下面的字段为内部使用，记录star在View上的坐标和范围
        private float x;
        private float y;
        private float r;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLuminance() {
            return luminance;
        }

        public void setLuminance(double luminance) {
            this.luminance = luminance;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setMoonShape(float moonShape) {
            this.moonShape = moonShape;
        }

        public float getMoonShape() {
            return moonShape;
        }

        @Override
        public String toString() {
            DecimalFormat format = new DecimalFormat("0.00");
            return String.format("%s.%s[%s,%s]:%s", id, name, format.format(longitude),
                    format.format(latitude), format.format(luminance));
        }
    }

    public interface OnStarClickListener {
        void onStarClick(StarItem star, boolean isLongClick);
    }
}
