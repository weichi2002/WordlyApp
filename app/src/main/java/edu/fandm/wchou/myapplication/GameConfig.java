package edu.fandm.wchou.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameConfig extends AppCompatActivity {
    private static final String TAG = "GAME_CONFIG";
    protected Graph words_graph;
    public static String start_word = "";
    public static String end_word = "";

    boolean isGenerating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_config);

        Button newPuzzleBtn = (Button) findViewById(R.id.new_puzzle_bt);
        newPuzzleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get editText for start and end words here
                EditText start_et = (EditText) findViewById(R.id.start_word_et);
                start_word = start_et.getText().toString();
                EditText end_et = (EditText) findViewById(R.id.end_word_et);
                end_word = end_et.getText().toString();

                if (start_word.equals(end_word)) {
                    Toast.makeText(getApplicationContext(), "Start and end words can't be the same.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (start_word.length() != end_word.length()) {
                    Toast.makeText(getApplicationContext(), "Start and end words must be same length.", Toast.LENGTH_LONG).show();
                    return;
                }


                //SolutionPathExecutor spe = new SolutionPathExecutor();
                //spe.build_words_dictionary(start_word, end_word, spc);

                // move this to a separate thread
                if (words_graph == null) {
                    words_graph = new Graph(getApplicationContext());
                    try {
                        words_graph.read_json_map_from_assets();
                    } catch (JSONException jsone) {
                        Log.d(TAG, "Error. Reading JSON map data from assets file failed.");
                    }
                }
                // move above to a separate thread


                try {
                    Game.solution_path = words_graph.get_solution_path(start_word, end_word);
                } catch (JSONException jsone) {
                    Log.d(TAG, "Error. Failed to generate solution path for the given start and end words.");
                }
            }
        });

        Button playBtn = (Button) findViewById(R.id.play_btn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Game.class);
                startActivity(i);
            }
        });
        //isGenerating = true;
        //showWorking(isGenerating);
    }

    private void showWorking(boolean on) {
        Log.d("GAME config", "working...");
        View v = findViewById(R.id.thinking_tv);
        if (on) {
            v.setVisibility(View.VISIBLE);
            Animation a = AnimationUtils.loadAnimation(this, R.anim.blink);
            v.setAnimation(a);
            v.animate();
        } else {
            v.setVisibility(View.INVISIBLE);
            v.clearAnimation();
        }
    }
}