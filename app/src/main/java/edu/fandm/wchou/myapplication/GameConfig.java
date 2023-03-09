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

    public interface SolutionPathCallback {
        void generate_solution(String start_word, String end_word);
    }

    SolutionPathCallback spc = new SolutionPathCallback() {
        @Override
        public void generate_solution(String start_word, String end_word) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //showWorking(false);
                    //boolean start_word_is_in_graph = words_graph.getWordsMap().optString(start_word).equals("");
                    //boolean end_word_is_in_graph = words_graph.getWordsMap().optString(end_word).equals("");


                    // generate solution path only if start, end words in dict
                    if (words_graph.getWordsMap().has(start_word) && words_graph.getWordsMap().has(end_word)) {
                        try {
                            Game.solution_path = words_graph.get_solution_path(start_word, end_word);
                        } catch (IllegalArgumentException | JSONException iae) {
                            Toast.makeText(getApplicationContext(), "Sorry, no solution path was found.", Toast.LENGTH_SHORT).show();
                            //iae.printStackTrace();
                        } finally {
                            Toast.makeText(getApplicationContext(), "Path found! You may now play.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Sorry, a word you typed is not available.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    };

    public class SolutionPathExecutor {
        public void build_words_dictionary(String start_word, String end_word, SolutionPathCallback callback) {
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(new Runnable() {
                @Override
                public void run() {
                    //showWorking(true);

                    // constructor builds words dictionary with txt file stored in assets folder
                    if (words_graph == null) {
                        words_graph = new Graph(getApplicationContext());
                    } else {
                        // TODO: could get words map from json file here?
                        //File rootDirOfApp = getFilesDir();
                        //File targetFile = new File(rootDirOfApp, words_graph.getFileName());

                    }
                    // get solution path AFTER words dictionary is built in this separate thread
                    callback.generate_solution(start_word, end_word);
                }
            });
        }
    }

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


                SolutionPathExecutor spe = new SolutionPathExecutor();
                spe.build_words_dictionary(start_word, end_word, spc);
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