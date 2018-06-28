package com.example.bardur.zygnelogger;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.zlogger.Zlogger;

import java.security.PublicKey;

/**
 * Created by Bardur Thomsen on 6/28/18.
 * <p>
 * bardur.thomsen@avantica.net
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);

        Zlogger.getInstance().init(this);

        Zlogger.getInstance().setLogLevel(Zlogger.LogLevel.DEBBUG);

        Zlogger.getInstance().writeLog("Just a test");

        Zlogger.getInstance().writeLog("000000000011111111112222222222333333333344444444445555555555666666666677777777778888888888999999999XQZ");

        Zlogger.getInstance().info(TAG, "testing infosdasd asdsadd adsada adsad adsa asd asdsa asdasd adasd asdad asdadsa adsad adasd adsad adasd asdad asda asdad a");

        Zlogger.getInstance().debug(TAG, "testing debug");

        Zlogger.getInstance().error(TAG, "testing error");
    }
}
