package edu.fandm.wchou.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameConfig extends AppCompatActivity {
    private static final String TAG = "GAME_CONFIG";
    protected Graph words_graph;
    protected static JSONObject words_json_map;

    public static String start_word = "";
    public static String end_word = "";

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String LAST_APP_VERSION = "last_app_version";

    private Button play_button;
    private Button new_puzzle_button;
    private JSONArray word_keys;


    public interface GenerateSolutionPathCallback {
        void onComplete(String start_word, String end_word);
    }
    GenerateSolutionPathCallback gspc = new GenerateSolutionPathCallback() {
        @Override
        public void onComplete(String start, String end) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // get editText for start and end words here
                    EditText start_et = (EditText) findViewById(R.id.start_word_et);
                    EditText end_et = (EditText) findViewById(R.id.end_word_et);
                    start_et.setText(start);
                    end_et.setText(end);

                    if (Game.solutionPath.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Sorry, no solution path was found for these words. Try entering something else.", Toast.LENGTH_SHORT).show();
                    } else if (Game.solutionPath.size() == 2) {
                        Toast.makeText(getApplicationContext(), "Wow, looks like you already win! Try entering something else.", Toast.LENGTH_SHORT).show();
                    }

                    play_button.setEnabled(true);
                    new_puzzle_button.setEnabled(true);
                    showWorking(false);
                }
            });
        }
    };
    public class GenerateSolutionPathExecutor {
        void generateSolutionPath(String start, String end, final GenerateSolutionPathCallback callback) {
            ExecutorService es = Executors.newFixedThreadPool(5);
            es.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Game.solutionPath = words_graph.get_solution_path(start_word, end_word);
                        if (Game.solutionPath.size() == 2) {
                            Log.d(TAG, "Wow, looks like you already win!");
                        }
                    } catch (JSONException jsone) {
                        Log.d(TAG, "Error. Failed to generate solution path for the given start and end words.");
                    }
                    callback.onComplete(start_word, end_word);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_config);

        this.play_button = (Button) findViewById(R.id.play_btn);
        this.new_puzzle_button = (Button) findViewById(R.id.new_puzzle_bt);

        this.words_graph = new Graph(getApplicationContext(), words_json_map);

        try {
            this.word_keys = words_graph.getWordsMap().names();
        } catch (NullPointerException npe) {
            Log.d(TAG, "Error. Retrieving word keys from graph failed.");
            return;
        }

        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get the last app version code from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastVersionCode = prefs.getInt(LAST_APP_VERSION, 0);
        if (currentVersionCode > lastVersionCode) {
            showPopup();

            // Update the last app version in SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(LAST_APP_VERSION, currentVersionCode);
            editor.apply();
        }

        //Button newPuzzleBtn = (Button) findViewById(R.id.new_puzzle_bt);
        new_puzzle_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get editText for start and end words here
                EditText start_et = (EditText) findViewById(R.id.start_word_et);
                EditText end_et = (EditText) findViewById(R.id.end_word_et);

                try {
                    // set random start and end words from map when "New Puzzle" button is clicked by user
                    Random rand = new Random();
                    assert word_keys != null;
                    int rand_word_index = rand.nextInt(word_keys.length());
                    int rand_word_index2 = rand.nextInt(word_keys.length());
                    while (rand_word_index == rand_word_index2) {
                        rand_word_index2 = rand.nextInt(word_keys.length());
                    }

                    String rand_start_word = Objects.requireNonNull(words_graph.getWordsMap().names()).getString(rand_word_index);
                    String rand_end_word = Objects.requireNonNull(words_graph.getWordsMap().names()).getString(rand_word_index2);

                    if (rand_start_word.equals(rand_end_word)) {
                        Toast.makeText(getApplicationContext(), "Start and end words can't be the same.", Toast.LENGTH_LONG).show();
                    } else {
                        start_word = rand_start_word;
                        end_word = rand_end_word;

                        showWorking(true);
                        play_button.setEnabled(false);
                        new_puzzle_button.setEnabled(false);
                        GenerateSolutionPathExecutor gspe = new GenerateSolutionPathExecutor();
                        gspe.generateSolutionPath(rand_start_word, rand_end_word, gspc);
                    }
                } catch (JSONException jsone) {
                    //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Error. Indexing a random start and end word failed.");
                }
            }
        });

        //Button playBtn = (Button) findViewById(R.id.play_btn);
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get editText for start and end words here
                EditText start_et = (EditText) findViewById(R.id.start_word_et);
                String user_start_word = start_et.getText().toString();
                EditText end_et = (EditText) findViewById(R.id.end_word_et);
                String user_end_word = end_et.getText().toString();

                if (user_start_word.equals(user_end_word)) {
                    Toast.makeText(getApplicationContext(), "Start and end words can't be the same.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (user_start_word.length() != user_end_word.length()) {
                    Toast.makeText(getApplicationContext(), "Start and end words must be same length.", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (user_start_word.length() != 4) {
                    Toast.makeText(getApplicationContext(), "Sorry, words must be 4 letters long.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (start_word.equals(user_start_word) && end_word.equals(user_end_word)) {
                    if (Game.solutionPath != null && Game.solutionPath.size() > 2) {
                        Intent i = new Intent(getApplicationContext(), Game.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "Sorry, these words don't have a solution path. Try entering something else!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else {
                    showWorking(true);
                    play_button.setEnabled(false);
                    new_puzzle_button.setEnabled(false);
                    GenerateSolutionPathExecutor gspe = new GenerateSolutionPathExecutor();
                    gspe.generateSolutionPath(user_start_word, user_end_word, gspc);

                    if (Game.solutionPath != null && Game.solutionPath.size() > 2) {
                        start_word = user_start_word;
                        end_word = user_end_word;

                        Intent i = new Intent(getApplicationContext(), Game.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "Sorry, no solution was found for these words. Try entering something else!", Toast.LENGTH_SHORT).show();
                        return;
                    }
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

    //Adapted from chatGPT
    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater) GameConfig.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.instruction_pop_up, null);

        PopupWindow popupWindow = new PopupWindow(GameConfig.this);
        popupWindow.setContentView(popupView);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);

        // Set the background color to fill the entire window
        ColorDrawable colorDrawable = new ColorDrawable(Color.BLACK);
        colorDrawable.setAlpha(255); // adjust the transparency as needed
        popupWindow.setBackgroundDrawable(colorDrawable);

        View rootView = findViewById(android.R.id.content);
        ViewTreeObserver vto = rootView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener so it only gets called once
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Show the popup window
                popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
            }
        });
        Button closeButton = popupView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }
}