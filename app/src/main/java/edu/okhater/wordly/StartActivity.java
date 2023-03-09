package edu.okhater.wordly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.os.SystemClock;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartActivity extends AppCompatActivity {
    BufferedReader reader;
    long lastClickTime = 0;
    Graph gr;
    interface GraphCallBack{
        void onComplete(ArrayList<String> s);
    }
    GraphCallBack gcb = new GraphCallBack() {
        @Override
        public void onComplete(ArrayList<String> s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // there is a path and it is not just the start and end word
                    if(s != null && s.size() > 2) {
                        Intent i = new Intent(getApplicationContext(), GameActivity.class);
                        i.putExtra("path", s);
                        startActivity(i);
                    }
                    else if(s != null && s.size() == 2) {
                        Toast.makeText(getApplicationContext(), "Words too similar!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Words not supported!", Toast.LENGTH_SHORT).show();
                    }
                    showWorking(false);
                }
            });

        }
    };

    public class GraphExecutor{
        public void findPath(GraphCallBack gcb, String word1, String word2){
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(new Runnable() {
                @Override
                public void run() {
                    ArrayList<String>s = gr.findPath(word1, word2);
                    gcb.onComplete(s);
                }
            });
        }
        public void findRandomPath(GraphCallBack gcb){
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(new Runnable() {
                @Override
                public void run() {
                    ArrayList<String> s = gr.findRandomWordsPath();
                    gcb.onComplete(s);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("words_unix.txt")));
            gr = new Graph(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // prevent double clicking play game button https://stackoverflow.com/questions/5608720/android-preventing-double-click-on-a-button
        findViewById(R.id.play_custom).setOnClickListener(view -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();


            EditText word1 = findViewById(R.id.word1);
            EditText word2 = findViewById(R.id.word2);
            String word1Str = word1.getText().toString();
            String word2Str = word2.getText().toString();
            if(gr.find(word1Str) == null || gr.find(word2Str) == null){
                Toast.makeText(getApplicationContext(), "Words not supported!", Toast.LENGTH_SHORT).show();
            }
            else if(word1Str.length() != word2Str.length()){
                Toast.makeText(getApplicationContext(), "Enter two words of the same length!", Toast.LENGTH_SHORT).show();
            }
            else{
                showWorking(true);
                new GraphExecutor().findPath(gcb, word1Str, word2Str);
            }
        });

        findViewById(R.id.play_random).setOnClickListener(view -> {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                return;
            }
            showWorking(true);
            lastClickTime = SystemClock.elapsedRealtime();
            new GraphExecutor().findRandomPath(gcb);
        });
    }

    private void showWorking(boolean on) {
        View v = findViewById(R.id.path_tv_working);

        if (on) {
            v.setVisibility(View.VISIBLE);
            Animation a = AnimationUtils.loadAnimation(this, R.anim.blink_anim);
            v.setAnimation(a);
            v.animate();
        } else {
            v.setVisibility(View.INVISIBLE);
            v.clearAnimation();
        }
    }
}