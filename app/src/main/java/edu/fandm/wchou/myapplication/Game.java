package edu.fandm.wchou.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import java.util.Random;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class Game extends AppCompatActivity {
    private static final String TAG = "GAME";
    public static ArrayList<String> solution_path; // GameConfig will set solution path here in Game
    private static String curr_word_in_solution_to_guess = "";

    private ArrayList<String> guessed_list;
    private static int guess_index = 1;

    private ImageView imageView;
    private  String[] imageUrls = new String[3];

    private int currentImageIndex = 0;

    private String apiKey = "34252989-dc19ee59010aba0c0da5b8937";
    private Handler handler;

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


    private void endGame(){
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
        ImageView starImg = (ImageView)findViewById(R.id.clue_pic);
        starImg.setImageResource(R.drawable.baseline_star_24);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        animation.setDuration(5000);
        starImg.startAnimation(animation);



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

    private void checkGuess(TextView textView, EditText input, int i){
        String guess = input.getText().toString().toLowerCase();
        String answer = solution_path.get(i);
        // Set the new text to the TextView
        Log.d("The index  is", String.valueOf(i));
        Log.d("The solution is", solution_path.get(i));
        Log.d("The guess is", guess);
        if(guess.length()!=GameConfig.start_word.length()){
            Toast.makeText(Game.this, "The word is not " + (String.valueOf(GameConfig.start_word.length()) +" letters long"), Toast.LENGTH_SHORT).show();
        }else if(guess.equals(answer)){
            Toast.makeText(Game.this, "Correct", Toast.LENGTH_SHORT).show();
            textView.setTextSize(20);
            textView.setGravity(Gravity.CENTER);
            textView.setText(answer);
            guess_index++;
            guessed_list.add(answer);
            Log.d("GUeSSED LIST ADDEDED", answer);
            //The user got to the last word
            if (guess_index == solution_path.size()-1) {
                Toast.makeText(getApplicationContext(), "You win!", Toast.LENGTH_LONG).show();
                endGame();
            } else {
                updateHintAndImage(); // update hint and img with next word to guess
            }

        }else if(!hasOneLetterDifference(answer, guess)){
            Toast.makeText(Game.this, "That is more than one letter difference", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(Game.this, "That is not the word I am thinking of", Toast.LENGTH_SHORT).show();
        }

    }

    //adapted from chatGPT
    private void onClick(TextView textView, int i){
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

    private void generateImages(){
        imageView = (ImageView)findViewById(R.id.clue_pic);
        OkHttpClient client = new OkHttpClient();
        String url = "https://pixabay.com/api/?key=" + apiKey + "&q=" + curr_word_in_solution_to_guess + "&image_type=photo&per_page=3";
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


                        // Cycle through the retrieved images every 5 seconds
                        handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                currentImageIndex = (currentImageIndex + 1) % imageUrls.length;
                                Picasso.get().load(imageUrls[currentImageIndex]).fit().centerCrop().into(imageView);
                                View rootView = imageView.getRootView();
                                rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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


    //Populate the horizontal list of textviews of solution path
    //adapted from chatgpt
    private void populateList(){
        LinearLayout linearLayout = findViewById(R.id.linear_layout);
        for(int i = 0; i < solution_path.size(); i++){

            TextView textView = new TextView(this);
            if ( (guessed_list != null && guessed_list.contains(solution_path.get(i))) || i == 0 || i == solution_path.size()-1 ){
                textView.setText(solution_path.get(i));
                textView.setTextSize(20);
                textView.setGravity(Gravity.CENTER);
            }else{
                //allow the user to click and enter guess
                onClick(textView, i);
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

    // neighbors => same length with exactly one letter different between them
    private boolean hasOneLetterDifference(String word1, String word2) {

        int differ_count = 0;
        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                differ_count++;
            }
        }

        return (differ_count == 1);
    }

    private char getLetterDifference(String previous, String current){
        for (int i = 0; i < previous.length(); i++) {
            if (previous.charAt(i) != current.charAt(i)) {
                Log.d("get letter diff", String.valueOf(current.charAt(i)));
                return current.charAt(i);
            }
        }
        //This wont go here because there is guaranteed one letter difference according to the dictionary
        return 'z';
    };

    protected void updateHintAndImage() {
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
        }
        //hint button shows the letter difference of the known word and the next word
//        handler.removeCallbacks();
        curr_word_in_solution_to_guess = solution_path.get(guess_index);
        Log.d(TAG, "Next word to guess: " + curr_word_in_solution_to_guess);
        char diff = getLetterDifference(solution_path.get(guess_index-1), curr_word_in_solution_to_guess);

        ExtendedFloatingActionButton hintBtn = (ExtendedFloatingActionButton) findViewById(R.id.fab);
        hintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), String.valueOf(diff), Toast.LENGTH_SHORT).show();
            }
        });

        //adapted from chatgpt
        generateImages();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        if (savedInstanceState != null) {
            guessed_list = savedInstanceState.getStringArrayList("guessed_list");
            for(String item: guessed_list){
                Log.d("GUESSED LIST HAS", item);
            }
        }else{
            guessed_list = new ArrayList<>();
        }

        fullScreen();
        populateList();
        updateHintAndImage();

    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("guessed_list", guessed_list);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove any pending callbacks from the handler to prevent memory leaks
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
    }


}

