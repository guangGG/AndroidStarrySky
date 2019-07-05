## SkyStar星空（天球）
### 权限(可选)：
```
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
### 混淆配置(可选)：
```
    -keep class gapp.season.star.** {*;}
    -dontwarn gapp.season.star.**
```
### 接入方法
```
    1.先将SDK的aar包导入工程；(打aar包：执行AS的Gradle工具中找到对应Module-Tasks-build-assembleRelease命令)
    2.Application的onCreate方法中初始化一些配置项： SkyStar.config(isdev, defaultLuminance, pageTheme); (可选)
    打开页面前校验并申请定位权限(无定位权限时显示位置会不准确，但不影响使用)
    打开星空页面：SkyStar.openSkyBall(context);
    3.额外配置：
    SkyStar.putDatumDate(solar, datumDate); //自定义配置太阳系天体基准日期(SDK中默认使用的2019年左右的基准日期) (可选)
```

### 说明
```
数据源：Yale Bright Star Catalog，使用2000年坐标展示 (http://tdc-www.harvard.edu/catalogs/bsc5.html)
天球展示方位：上北、下南、左东、右西
```
