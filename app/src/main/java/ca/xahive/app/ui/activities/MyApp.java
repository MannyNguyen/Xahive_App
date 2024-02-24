package ca.xahive.app.ui.activities;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by Hoan on 10/22/2015.
 */
public class MyApp extends MultiDexApplication {

    private static MyApp singleton;

    public MyApp getInstance(){
        return singleton;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

    }
}
