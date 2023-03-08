package edu.okhater.wordly;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements RecycleViewAdapter.ItemClickListener{
    ArrayList<String> path;
    ArrayList<String> guess;
    RecycleViewAdapter adapter;

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
        guess = new ArrayList<String>() {
            {
                add(path.get(0));
                add("");
                add("");
                add(path.get(path.size() - 1));
            }
        };
        adapter = new RecycleViewAdapter(this, guess);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }
    @Override
    public void onItemClick(View view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

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
                    Log.d("CPS", "HEY");
                    guess.set(position, path.get(position));
                    adapter.notifyDataSetChanged();

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
}