package me.chunsheng.tinkerforflutter;

import android.app.Application;
import android.content.Context;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import io.flutter.view.FlutterMain;

/**
 * Author: wei_spring
 * Date: 2020/8/13
 * Email:weichsh@kuaipeilian.com
 * Function:
 */
public class MyApplication extends Application {
    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Beta.installTinker();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FlutterMain.startInitialization(this);
        instance = this;
        Bugly.init(this, "a570c5841f", true);
    }
}