package edu.okhater.wordly;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartActivity extends AppCompatActivity {
    BufferedReader reader;
    Graph gr;
    ArrayList<String> path;
    interface GraphCallBack{
        void onComplete(ArrayList<String> s);
    }
    GraphCallBack gcb = new GraphCallBack() {
        @Override
        public void onComplete(ArrayList<String> s) {
            path = s;
        }
    };

    public class GraphExecutor{
        public void findPath(GraphCallBack gcb, String word1, String word2){
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(new Runnable() {
                @Override
                public void run() {
                    gcb.onComplete(gr.findPath(word1, word2));
                }
            });
        }
        public void findRandomPath(GraphCallBack gcb){
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(new Runnable() {
                @Override
                public void run() {
                    gcb.onComplete(gr.findRandomWordsPath());
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
                    new InputStreamReader(getAssets().open("words_test.txt")));
            gr = new Graph(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        findViewById(R.id.play).setOnClickListener(view -> {
            EditText word1 = findViewById(R.id.word1);
            EditText word2 = findViewById(R.id.word2);
            String word1Str = word1.toString();
            String word2Str = word2.toString();
            if(gr.find(word1Str) == null || gr.find(word2Str) == null){
                Toast.makeText(getApplicationContext(), "Words are not supported! Sorry!", Toast.LENGTH_SHORT).show();
                return;
            }
            if(word1Str.length() != word2Str.length()){
                Toast.makeText(getApplicationContext(), "Enter two words of the same length!", Toast.LENGTH_SHORT).show();
            }
            else{
                new GraphExecutor().findPath(gcb, word1Str, word2Str);
                //Send them to the game
            }
        });

        findViewById(R.id.random).setOnClickListener(view -> {
            new GraphExecutor().findRandomPath(gcb);
            //Send them to the game
        });

    }
}