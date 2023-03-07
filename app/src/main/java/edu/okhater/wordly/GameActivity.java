package edu.okhater.wordly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    ArrayList<String> path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent i = getIntent();
        if(i != null) {
            path = (ArrayList<String>) i.getSerializableExtra("path");
        }
        Log.d("CPS", path.toString());
    }
}