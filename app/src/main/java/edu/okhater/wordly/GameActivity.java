package edu.okhater.wordly;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameActivity extends AppCompatActivity implements RecycleViewAdapter.ItemClickListener{
    ArrayList<String> path;
    ArrayList<String> guess;
    RecycleViewAdapter adapter;
    Boolean userWin = false;

    interface HintImageCallback {
        void onComplete(Bitmap img);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent i = getIntent();
        if(i != null) {
            path = (ArrayList<String>) i.getSerializableExtra("path");
        }
        Log.d("CPS", path.toString());
        RecyclerView recyclerView = findViewById(R.id.list);
        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(GameActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);

        guess = new ArrayList<>(Collections.nCopies(path.size(), ""));
        guess.set(0, path.get(0));
        guess.set(guess.size() - 1, path.get(path.size() - 1));
        adapter = new RecycleViewAdapter(this, guess);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        HintImageExecutor hie = new HintImageExecutor();
        // using math for now as a test
        hie.fetch(hic, "math");

        View rl = findViewById(R.id.rl_root);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userWin) {
                    finish();
                }
            }
        });

        Button hintButton = (Button) findViewById(R.id.hint_button);
        hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userWin) {
                    finish();
                }
                // find first box not answered
                for (int wordIdx = 0; wordIdx < path.size(); wordIdx++) {
                    if (!path.get(wordIdx).equals(guess.get(wordIdx).toLowerCase())) {
                        // find the different character
                        for (int charIdx = 0; charIdx < path.get(wordIdx).length(); charIdx++) {
                            // compare current word to word before it
                            if (path.get(wordIdx).charAt(charIdx) != path.get(wordIdx - 1).charAt(charIdx)) {
                                Toast.makeText(getApplicationContext(), String.valueOf(path.get(wordIdx).charAt(charIdx)), Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        break;
                    }
                }

            }
        });

    }
    @Override
    public void onItemClick(View view, int position) {
        if (userWin) {
            finish();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Guess!");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(path.get(position).equals(input.getText().toString().toLowerCase())){
                    guess.set(position, path.get(position));
                    adapter.notifyDataSetChanged();
                    if (path.equals(guess)) {
                        userWin = true;
                        ImageView winStar = (ImageView) findViewById(R.id.win_star);
                        winStar.setVisibility(View.VISIBLE);
                        Animation fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_anim);
                        winStar.setAnimation(fade);
                        winStar.animate();

                        Toast.makeText(getApplicationContext(), "You Win!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Wrong word!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    HintImageCallback hic = new HintImageCallback() {
        @Override
        public void onComplete(Bitmap img) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (img != null) {
                        ImageView iv = (ImageView) findViewById(R.id.hint_image);
                        iv.setImageBitmap(img);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Failed to download hint image :(", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    public class HintImageExecutor {
        public void fetch(HintImageCallback hic, String word) {
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(new Runnable() {

                @Override
                public void run() {

                    Bitmap img = null;
                    try {
                        URL url = new URL("https://images5.alphacoders.com/408/408941.jpg");
                        InputStream in = new BufferedInputStream(url.openStream());
                        ByteArrayOutputStream out = new ByteArrayOutputStream();

                        byte[] buf = new byte[1024];
                        int n = 0;
                        while (-1 != (n= in.read(buf))) {
                            out.write(buf, 0, n);
                        }
                        out.close();
                        in.close();

                        byte[] response = out.toByteArray();
                        img = BitmapFactory.decodeByteArray(response, 0, response.length);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    hic.onComplete(img);

                }
            });
        }
    }
}