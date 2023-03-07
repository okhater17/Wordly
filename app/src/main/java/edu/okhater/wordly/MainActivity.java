package edu.okhater.wordly;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View rootView = findViewById(R.id.root_cl_main);

        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }



        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // help with shared preferences https://www.digitalocean.com/community/tutorials/android-shared-preferences-example-tutorial
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("firstLaunchPreference", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // if first launch
                if (!sharedPreferences.contains("firstLaunch")) {
                    editor.putBoolean("firstLaunch", true);
                    editor.commit();
                    Intent i = new Intent(getApplicationContext(), FirstLaunchActivity.class);
                    startActivity(i);
                }
                // if not first launch go to game
                else {
                    Intent i = new Intent(getApplicationContext(), StartActivity.class);
                    startActivity(i);
                }
            }
        }, 1375);
    }
}