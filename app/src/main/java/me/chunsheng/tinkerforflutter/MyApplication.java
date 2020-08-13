package me.chunsheng.tinkerforflutter;

import android.app.Application;
import io.flutter.view.FlutterMain;

/**
 * Copyright © 2018 www.kuaipeilian.com. All Rights Reserved.
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
      public void onCreate() {
            super.onCreate();
            FlutterMain.Settings settings = new FlutterMain.Settings();
            settings.setLogTag("MyFlutter");
            FlutterMain.startInitialization(this, settings);
            String[] args = { "info", "data" };
            FlutterMain.ensureInitializationComplete(this, args);
            instance = this;
      }
}