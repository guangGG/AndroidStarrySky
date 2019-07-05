package gapp.season.star.demo;

import android.app.Application;

import gapp.season.star.SkyStar;

public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SkyStar.config(BuildConfig.DEBUG, 1.5, 0);
    }
}
