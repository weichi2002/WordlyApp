package edu.fandm.wchou.myapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class Game extends AppCompatActivity {


    //Adapted from chatGPT
//    public void promptUserInput(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Enter a string");
//        builder.setMessage("Please enter a string:");
//
//        final EditText editText = new EditText(this);
//        builder.setView(editText);
//
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // Do something with the user input
//                String userInput = editText.getText().toString();
//                // ...
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // Cancel the dialog
//                dialog.cancel();
//            }
//        });
//
//        builder.show();
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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

