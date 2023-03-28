package edu.fandm.wchou.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

//star animation
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

//fetching images
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import android.os.Handler;

public class Game extends AppCompatActivity {

    //word puzzle solution
    public static ArrayList<String> solutionPath; // GameConfig will set solution path here in Game
    private static String currWordToGuess = "";
    private ArrayList<String> guessedList;
    private static int guessIndex = 1;

    //fetching images
    private ImageView imageView;
    private  String[] imageUrls = new String[3];
    private int currentImageIndex = 0;
    private String APIKEY = "34252989-dc19ee59010aba0c0da5b8937";

    //use handler for threading to display images
    private Handler handler;

    private void fullScreen(){
        hideActionBar();
        hideButtonBar();
    }

    private void hideButtonBar(){
        View imgView = findViewById(R.id.clue_pic);
        View rootView = imgView.getRootView();
        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void hideActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void guessedCorrect(TextView textView, String answer){

        // Set the correct answer to the empty TextView
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER);
        textView.setText(answer);

        // Increment the index and update the list and the new solution
        guessIndex++;
        guessedList.add(answer);
        currWordToGuess = solutionPath.get(guessIndex);


        //Check if user solved the puzzle
        if (guessIndex == solutionPath.size()-1) {
            Toast.makeText(getApplicationContext(), "You win!", Toast.LENGTH_LONG).show();
            endGame();
        } else {
            updateHintAndImage(); // update hint and img with next word to guess
        }
    }


    private void checkGuess(TextView textView, EditText input, int i){
        String guess = input.getText().toString().toLowerCase();
        String answer = solutionPath.get(i);

        //user guessed incorrect word length
        if(guess.length()!=GameConfig.start_word.length()){
            Toast.makeText(Game.this, "The word is not " + (String.valueOf(GameConfig.start_word.length()) +" letters long"), Toast.LENGTH_SHORT).show();

        //user guessed correct
        }else if(guess.equals(answer)){
            Toast.makeText(Game.this, "Correct", Toast.LENGTH_SHORT).show();
            guessedCorrect(textView, answer);

        //user's guessed is off by 2+ letters
        }else if(!hasOneLetterDifference(answer, guess)){
            Toast.makeText(Game.this, "That is more than one letter difference", Toast.LENGTH_SHORT).show();

        //user guessed close, by off by one letter
        }else{
            Toast.makeText(Game.this, "That is not the word I am thinking of", Toast.LENGTH_SHORT).show();
        }
    }

    // Remove any pending callbacks from the handler to prevent memory leaks
    private void checkHandlerNull(){
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void showStarAnimation(){
        ImageView starImg = (ImageView)findViewById(R.id.clue_pic);
        starImg.setImageResource(R.drawable.baseline_star_24);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        animation.setDuration(5000);
        starImg.startAnimation(animation);
    }

    private void endGame(){
        //reset the count index because it is a static variable, which does get destroyed when the activity is finished
        guessIndex = 1;
        showStarAnimation();

        //remove pending callback first to prevent leaks
        checkHandlerNull();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getApplicationContext(), GameConfig.class);
                startActivity(i);
                finish();
            }
        }, 5000);
    }

    private void createDialogInput(TextView textView, int i){
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
                        checkGuess(textView, input, i);
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

    //adapted from stackoverflow and chatgpt
    private void generateImages(){
        imageView = (ImageView)findViewById(R.id.clue_pic);
        OkHttpClient client = new OkHttpClient();
        String url = "https://pixabay.com/api/?key=" + APIKEY + "&q=" + currWordToGuess + "&image_type=photo&per_page=3";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        // Parse the JSON response to retrieve the image URLs
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray jsonArray = jsonObject.getJSONArray("hits");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject hit = jsonArray.getJSONObject(i);
                            String imageUrl = hit.getString("webformatURL");
                            imageUrls[i] = imageUrl;
                        }

                        // Cycle through the retrieved three images every 5 seconds
                        handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                //this makes the image cycle through infinitely
                                currentImageIndex = (currentImageIndex + 1) % imageUrls.length;
                                //Picasso is a library that uses thread to fetch images
                                Picasso.get().load(imageUrls[currentImageIndex]).fit().centerCrop().into(imageView);
                                handler.postDelayed(this, 5000);

                            }
                        }, 5000);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //Populate the horizontal list of textview of solution path
    //adapted from chatgpt and stackoverflow: "How to create a dynamic scrollable layout?"
    private void populateList(){
        LinearLayout linearLayout = findViewById(R.id.linear_layout);
        for(int i = 0; i < solutionPath.size(); i++){

            TextView textView = new TextView(this);
            if ( (guessedList != null && guessedList.contains(solutionPath.get(i))) || i == 0 || i == solutionPath.size()-1 ){
                textView.setText(solutionPath.get(i));
                textView.setTextSize(20);
                textView.setGravity(Gravity.CENTER);
            }else{
                //allow the user to click and enter guess
                createDialogInput(textView, i);
            }
            textView.setBackgroundResource(R.drawable.border);
            textView.setWidth(60* solutionPath.get(0).length());
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

    //check if two words has one letter difference
    private boolean hasOneLetterDifference(String word1, String word2) {
        int diff = 0;
        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                diff++;
            }
        }
        return (diff == 1);
    }

    private char getLetterDifference(String previous, String current){
        for (int i = 0; i < previous.length(); i++) {
            if (previous.charAt(i) != current.charAt(i)) {
                Log.d("get letter diff", String.valueOf(current.charAt(i)));
                return current.charAt(i);
            }
        }
        /*This is just dummy content to bypass the return requirement, it is guaranteed that
        there will be a difference between every letters in the dictionary.
        */
        return 'z';
    };

    protected void updateHintAndImage() {
        checkHandlerNull();
        updateHintButton();
        generateImages();
    }

    private void updateHintButton(){
        //hint button shows the letter difference of the known word and the next word
        char diff = getLetterDifference(solutionPath.get(guessIndex -1), currWordToGuess);
        ExtendedFloatingActionButton hintBtn = (ExtendedFloatingActionButton) findViewById(R.id.fab);
        hintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), String.valueOf(diff), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //check if there is an instance saved before creating a new arraylist
        if (savedInstanceState != null) {
            guessedList = savedInstanceState.getStringArrayList("guessed_list");
        }else{
            guessedList = new ArrayList<>();
        }

        //initialize the first word to guess so that hint button and images works
        currWordToGuess = solutionPath.get(guessIndex);

        fullScreen();
        populateList();
        updateHintAndImage();

    }

    //save the guessed list to preserve the user progress.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("guessed_list", guessedList);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //clear the handler to prevent leaks and crash
        checkHandlerNull();
    }
}

