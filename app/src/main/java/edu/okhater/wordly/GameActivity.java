package edu.okhater.wordly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import kotlinx.coroutines.ObsoleteCoroutinesApi;

public class GameActivity extends AppCompatActivity implements RecycleViewAdapter.ItemClickListener{
    ArrayList<String> path;
    ArrayList<String> guess;
    RecycleViewAdapter adapter;
    boolean userWin = false;
    int currentGuessPosition;
    View rootView;
    ArrayList<Bitmap> imageList;
    HintImageExecutor hie = new HintImageExecutor();

    interface HintImageCallback {
        void onComplete();
    }

    interface CycleImageCallback {
        void onComplete();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        rootView = findViewById(R.id.rl_root);

        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

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

        // if rotated this will preserve game state
        if (savedInstanceState != null) {
            guess = savedInstanceState.getStringArrayList("path");

            boolean win = true;
            for (int j = 0; j < guess.size(); j++) {
                if (guess.get(j).equals("")) {
                    win = false;
                    currentGuessPosition = j;
                    break;
                }
            }
            // if user rotated screen after winning end activity (this is what Professor Novak's does)
            if (!win) {
                adapter = new RecycleViewAdapter(this, guess);
                adapter.setClickListener(this);
                recyclerView.setAdapter(adapter);
            }
            else {
                finish();
            }
        }
        else {
            guess = new ArrayList<>(Collections.nCopies(path.size(), ""));
            guess.set(0, path.get(0));
            guess.set(guess.size() - 1, path.get(path.size() - 1));
            adapter = new RecycleViewAdapter(this, guess);
            adapter.setClickListener(this);
            recyclerView.setAdapter(adapter);
            currentGuessPosition = 1;
        }
        // using math for now as a test
        hie.fetch(hic, path.get(currentGuessPosition));


        rootView.setOnClickListener(new View.OnClickListener() {
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
                for (int wordIdx = 1; wordIdx < path.size(); wordIdx++) {
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
                rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        });

        // how to tell if dialog exited by back button https://stackoverflow.com/questions/49809495/detect-back-button-event-when-dialog-is-open
        builder.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.cancel();
                    rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
                return true;
            }
        });

        builder.show();


    }

    HintImageCallback hic = new HintImageCallback() {
        @Override
        public void onComplete() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView iv = (ImageView) findViewById(R.id.hint_image);
                    if (imageList.size() > 0) {
                        iv.setImageBitmap(imageList.get(new Random().nextInt(imageList.size())));
                    }
                    else {
                        iv.setImageResource(R.drawable.unable_to_fetch_image);
                        Toast.makeText(getApplicationContext(), "Failed to download hint image :(", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            try {
                Cycle();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        public void Cycle() throws InterruptedException {
            while(!userWin){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView iv = (ImageView) findViewById(R.id.hint_image);
                        iv.setImageBitmap(imageList.get(new Random().nextInt(imageList.size())));
                    }
                });
                Thread.sleep(5000);
            }
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
                        ImageView iv = (ImageView) findViewById(R.id.hint_image);
                        iv.setImageResource(R.drawable.fetching_image);
                        // help with pixaby https://www.youtube.com/watch?v=iOd86bj41hs
                        Log.d("Search Word", searchWord);
                        URL url = new URL("https://pixabay.com/api/?key=34235580-57f7f2b3914a36555e74d2720&q=" + searchWord + "&image_type=photo&pretty=true");

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
                        imageList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            URL imageUrl = new URL((String) jsonObject.getString("previewURL"));

                            // convert to image
                            InputStream inStream = new BufferedInputStream(imageUrl.openStream());
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            byte[] buf = new byte[1024];
                            int n = 0;
                            while (-1 != (n = inStream.read(buf))) {
                                out.write(buf, 0, n);
                            }
                            out.close();
                            inStream.close();

                            byte[] response = out.toByteArray();
                            img = BitmapFactory.decodeByteArray(response, 0, response.length);
                            imageList.add(img);
                        }

                        } catch(IOException | JSONException e){
                            e.printStackTrace();
                        }
                    hic.onComplete();
                    }

            });
        }
    }


//    HintImageCallback hic = new HintImageCallback() {
//        @Override
//        public void onComplete() {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    ImageView hintImage = (ImageView) findViewById(R.id.hint_image);
//                    if (imageList.size() != 0) {
//                        CycleImageExecutor cie = new CycleImageExecutor();
//                        cie.cycleImages(cic);
//                    }
//                    else {
//                        hintImage.setImageResource(R.drawable.unable_to_fetch_image);
//                        Toast.makeText(getApplicationContext(), "Failed to download hint image :(", Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//            });
//        }
//    };

//    CycleImageCallback cic = new CycleImageCallback() {
//        @Override
//        public void onComplete() {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    ImageView hintImage = (ImageView) findViewById(R.id.hint_image);
//                    for (int i = 0; i < imageList.size(); i++) {
//                        hintImage.setImageBitmap(imageList.get(i));
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//            });
//        }
//    };

//    public class HintImageExecutor {
//        public void fetch(HintImageCallback hic, String searchWord) {
//            ExecutorService es = Executors.newFixedThreadPool(1);
//            es.execute(new Runnable() {
//
//                @Override
//                public void run() {
//
//                    HttpsURLConnection con = null;
//                    Bitmap img = null;
//                    try {
//                        ImageView iv = (ImageView) findViewById(R.id.hint_image);
//                        iv.setImageResource(R.drawable.fetching_image);
//                        // help with pixaby https://www.youtube.com/watch?v=iOd86bj41hs
//                        Log.d("Search Word", searchWord);
//                        URL url = new URL("https://pixabay.com/api/?key=34235580-57f7f2b3914a36555e74d2720&q=" + searchWord +"&image_type=photo&pretty=true");
//
//                        // get info from pixaby
//                        con = (HttpsURLConnection) url.openConnection();
//                        con.setRequestMethod("GET");
//                        con.connect();
//                        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//                        StringBuffer data = new StringBuffer();
//                        String curLine;
//                        while ((curLine = in.readLine()) != null) {
//                            data.append(curLine);
//                        }
//
//                        for (int i = 0; i < 5; i++) {
//                            // get image url
//                            JSONObject jsonImages = new JSONObject(data.toString());
//                            JSONArray jsonArray = jsonImages.getJSONArray("hits");
//                            JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            URL imageUrl = new URL((String) jsonObject.getString("previewURL"));
//
//                            // convert to image
//                            InputStream inStream = new BufferedInputStream(imageUrl.openStream());
//                            ByteArrayOutputStream out = new ByteArrayOutputStream();
//                            byte[] buf = new byte[1024];
//                            int n = 0;
//                            while (-1 != (n = inStream.read(buf))) {
//                                out.write(buf, 0, n);
//                            }
//                            out.close();
//                            inStream.close();
//
//                            byte[] response = out.toByteArray();
//                            imageList.add(BitmapFactory.decodeByteArray(response, 0, response.length));
//                        }
//
//                    } catch (IOException | JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    hic.onComplete();
//                }
//            });
//        }
//    }

//    public class CycleImageExecutor {
//        public void cycleImages(CycleImageCallback cic) {
//            ExecutorService es = Executors.newFixedThreadPool(1);
//            es.execute(new Runnable() {
//                @Override
//                public void run() {
//                    cic.onComplete();
//                }
//            });
//        }
//    }
    // save the current guesses when rotated https://stackoverflow.com/questions/16692536/good-solution-to-retain-listview-items-when-user-rotate-phone-and-keep-all-data
    @Override
    public void onSaveInstanceState(@NonNull Bundle savedState) {

        super.onSaveInstanceState(savedState);

        // Note: getValues() is a method in your ArrayAdapter subclass
        ArrayList<String> values = guess;
        savedState.putStringArrayList("path", values);

    }
}