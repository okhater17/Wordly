package edu.okhater.wordly;

import androidx.appcompat.app.ActionBar;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class GameActivity extends AppCompatActivity implements RecycleViewAdapter.ItemClickListener{
    ArrayList<String> path;
    ArrayList<String> guess;
    RecycleViewAdapter adapter;
    Boolean userWin = false;
    HintImageExecutor hie = new HintImageExecutor();

    interface HintImageCallback {
        void onComplete(Bitmap img);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        View rootView = findViewById(R.id.rl_root);

        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

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

        // using math for now as a test
        hie.fetch(hic, path.get(1));

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
                    // update image
                    hie.fetch(hic, path.get(position + 1));

                    guess.set(position, path.get(position));
                    adapter.notifyDataSetChanged();
                    if (path.equals(guess)) {
                        userWin = true;
                        ImageView hintImage = (ImageView) findViewById(R.id.hint_image);
                        hintImage.setVisibility(View.INVISIBLE);
                        
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
        public void fetch(HintImageCallback hic, String searchWord) {
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(new Runnable() {

                @Override
                public void run() {

                    HttpsURLConnection con = null;
                    Bitmap img = null;
                    try {
                        // help with pixabyhttps://www.youtube.com/watch?v=iOd86bj41hs
                        URL url = new URL("https://pixabay.com/api/?key=34235580-57f7f2b3914a36555e74d2720&q=" + searchWord +"&image_type=photo&pretty=true");

                        // get info from pixaby
                        con = (HttpsURLConnection) url.openConnection();
                        con.setRequestMethod("GET");
                        con.connect();
                        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                        StringBuffer data = new StringBuffer();
                        String curLine;
                        while ((curLine = in.readLine()) != null) {
                            data.append(curLine);
                        }

                        // get image url
                        JSONObject jsonImages = new JSONObject(data.toString());
                        JSONArray jsonArray = jsonImages.getJSONArray("hits");
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        URL imageUrl = new URL((String) jsonObject.getString("previewURL"));

                        // convert to image
                        InputStream inStream = new BufferedInputStream(imageUrl.openStream());
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int n = 0;
                        while (-1 != (n= inStream.read(buf))) {
                            out.write(buf, 0, n);
                        }
                        out.close();
                        inStream.close();

                        byte[] response = out.toByteArray();
                        img = BitmapFactory.decodeByteArray(response, 0, response.length);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    hic.onComplete(img);
                }
            });
        }
    }
}