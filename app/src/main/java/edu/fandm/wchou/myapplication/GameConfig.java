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

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameConfig extends AppCompatActivity {
    private static final String TAG = "GAME_CONFIG";
    protected static Graph words_graph;
    public static String start_word = "";
    public static String end_word = "";



    public interface GenerateSolutionPathCallback {
        void onComplete();
    }
    GenerateSolutionPathCallback gspc = new GenerateSolutionPathCallback() {
        @Override
        public void onComplete() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showWorking(false);
                    if (Game.solution_path != null && Game.solution_path.size() > 2) {
                        // go into game activity once solution is generated
                        Intent i = new Intent(getApplicationContext(), Game.class);
                        startActivity(i);
                    }
                }
            });
        }
    };
    public class GenerateSolutionPathExecutor {
        void generateSolutionPath(final GenerateSolutionPathCallback callback) {
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Game.solution_path = words_graph.get_solution_path(start_word, end_word);
                        if (Game.solution_path.size() == 2) {
                            Log.d(TAG, "Wow, looks like you already win!");
                            return;
                        }
                        callback.onComplete();
                    } catch (JSONException jsone) {
                        Log.d(TAG, "Error. Failed to generate solution path for the given start and end words.");
                    } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_config);

        // reading in json words map from assets on a separate thread
        //ReadInJsonMapExecutor rijme = new ReadInJsonMapExecutor();
        //rijme.read_in_words_map(rijmc);


        Button newPuzzleBtn = (Button) findViewById(R.id.new_puzzle_bt);
        newPuzzleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get editText for start and end words here
                EditText start_et = (EditText) findViewById(R.id.start_word_et);
                EditText end_et = (EditText) findViewById(R.id.end_word_et);

                try {
                    // set random start and end words from map when "New Puzzle" button is clicked by user
                    JSONArray word_keys = words_graph.getWordsMap().names();
                    Random rand = new Random();
                    int rand_word_index = rand.nextInt(word_keys.length());
                    int rand_word_index2 = rand.nextInt(word_keys.length());

                    String rand_start_word = words_graph.getWordsMap().names().getString(rand_word_index);
                    String rand_end_word = words_graph.getWordsMap().names().getString(rand_word_index2);

                    // keep getting a new random end word until one with same length as start word is found
                    while (rand_start_word.length() != rand_end_word.length()) {
                        rand_word_index2 = rand.nextInt(word_keys.length());
                        rand_end_word = words_graph.getWordsMap().names().getString(rand_word_index2);
                    }
                    start_et.setText(rand_start_word);
                    end_et.setText(rand_end_word);
                } catch (JSONException jsone) {
                    //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Error. Indexing a random start and end word failed.");
                }
            }
        });

        Button playBtn = (Button) findViewById(R.id.play_btn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get editText for start and end words here
                EditText start_et = (EditText) findViewById(R.id.start_word_et);
                start_word = start_et.getText().toString();
                EditText end_et = (EditText) findViewById(R.id.end_word_et);
                end_word = end_et.getText().toString();

                if (start_word.equals(end_word)) {
                    Toast.makeText(getApplicationContext(), "Start and end words can't be the same.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (start_word.length() != end_word.length()) {
                    Toast.makeText(getApplicationContext(), "Start and end words must be same length.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (Game.solution_path != null && Game.solution_path.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Sorry, no solution path was found for these words. Try entering something else!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    GenerateSolutionPathExecutor gspe = new GenerateSolutionPathExecutor();
                    gspe.generateSolutionPath(gspc);
                }
            }
        });
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