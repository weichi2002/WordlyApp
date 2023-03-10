package edu.fandm.wchou.myapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.json.JSONException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashPage extends AppCompatActivity {
    private static final String TAG = "SPLASH_PAGE";

    public interface ReadInJsonMapCallback {
        void onComplete();
    }

    ReadInJsonMapCallback rijmc = new ReadInJsonMapCallback() {
        @Override
        public void onComplete() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(getApplicationContext(), GameConfig.class);
                    startActivity(i);

                    finish();
                }
            });
        }
    };

    public class ReadInJsonMapExecutor {
        void read_in_words_map(final ReadInJsonMapCallback callback) {
            ExecutorService es = Executors.newFixedThreadPool(1);
            es.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Graph g = new Graph(getApplicationContext());
                        g.read_json_map_from_assets();
                        GameConfig.words_json_map = g.getWordsMap();
                    } catch (JSONException jsone) {
                        Log.d(TAG, "Error. Reading JSON map from assets into words graph failed.");
                    }
                    callback.onComplete();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_page);



        // hide button bar
        ImageView logo = findViewById(R.id.imageView);
        View rootView = logo.getRootView();
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

        // Switches to another activity after 1.375s
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ReadInJsonMapExecutor rijme = new ReadInJsonMapExecutor();
                rijme.read_in_words_map(rijmc);
            }
        }, 1375);
    }
}