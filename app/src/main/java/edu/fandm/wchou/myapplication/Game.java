package edu.fandm.wchou.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;

public class Game extends AppCompatActivity {
    private static final String TAG = "GAME";
    public static ArrayList<String> solution_path; // GameConfig will set solution path here in Game


    private void fullScreen(){
        // hide button bar
        View imgView = findViewById(R.id.clue_pic);
        View rootView = imgView.getRootView();
        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // hide action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void onClick(TextView textView){
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            // Handle click event here
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Game.this);
                builder.setTitle("Guess")
                        .setMessage("Please enter your guess:");

                final EditText input = new EditText(getApplicationContext());
                input.setText(textView.getText().toString());

                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Set the new text to the TextView
                        textView.setText(input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel the dialog
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

    }



    //Populate the horizontal list of textviews of solution path
    //adapted from chatgpt
    private void populateList(){
        LinearLayout linearLayout = findViewById(R.id.linear_layout);
        for(int i = 0; i < solution_path.size(); i++){

            TextView textView = new TextView(this);
            if (i == 0 || i == solution_path.size()-1){
                textView.setText(solution_path.get(i));
                textView.setTextSize(20);
            }else{
                //allow the user to click and enter guess
                onClick(textView);
            }
            textView.setBackgroundResource(R.drawable.border);
            textView.setWidth(60*solution_path.get(0).length());
            textView.setHeight(75);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(50, 0, 50, 0); // Set margins (left, top, right, bottom)
            textView.setLayoutParams(layoutParams);
            linearLayout.addView(textView);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Log.d(TAG, "Solution path is: " + solution_path.toString());

        fullScreen();
        populateList();

        //Adapted from https://www.youtube.com/watch?v=4UFNT6MhIlA
        ImageView cluePic = (ImageView)findViewById(R.id.clue_pic);
        Glide.with(this).load("https://source.unsplash.com/1600x900/?New%20York?Landscape").apply(new RequestOptions().centerCrop()).into(cluePic);

        //Floating button
        ExtendedFloatingActionButton hintBtn = (ExtendedFloatingActionButton) findViewById(R.id.fab);
        hintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "HINT", Toast.LENGTH_LONG ).show();
            }
        });


    }



}

