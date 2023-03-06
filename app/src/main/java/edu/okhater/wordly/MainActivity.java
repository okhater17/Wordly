package edu.okhater.wordly;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    BufferedReader reader;
    Graph gr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("words_test.txt")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            gr = new Graph(reader);
            for(int i = 0; i < gr.g.size(); i++){
                //Log.d("CPS", gr.g.get(i).name + "\n" + gr.g.get(i).successors.toString());
                Log.d("CPS", gr.findPath("weep", "beer").toString());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}