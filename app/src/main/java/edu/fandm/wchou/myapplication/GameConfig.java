package edu.fandm.wchou.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class GameConfig extends AppCompatActivity {
    private static final String TAG = "GAME_CONFIG";




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
                String start_word = start_et.getText().toString();
                EditText end_et = (EditText) findViewById(R.id.end_word_et);
                String end_word = end_et.getText().toString();

                // Move below code into separate thread...

                // pass them into solution path algorithm
                Graph words_graph = new Graph(getApplicationContext(), "words_simple.txt");
                try {
                    Game.solution_path = words_graph.get_solution_path(start_word, end_word);
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace();
                }

                // Move above code into separate thread...
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
        isGenerating = true;
        showWorking(isGenerating);
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