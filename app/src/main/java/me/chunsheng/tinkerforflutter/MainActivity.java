package me.chunsheng.tinkerforflutter;

import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import io.flutter.embedding.android.FlutterFragment;

public class MainActivity extends AppCompatActivity {

      @Override
      protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //FlutterFragment fragment=new FlutterFragment();
            FlutterFragment fragment=FlutterFragment.withNewEngine().initialRoute("/").build();
            getSupportFragmentManager().beginTransaction().add(R.id.flutter_container, fragment).commit();
      }
}