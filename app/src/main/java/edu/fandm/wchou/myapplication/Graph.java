package edu.fandm.wchou.myapplication;


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class Graph {
    private static final String TAG = "GRAPH";
    private JSONObject words;
    private Context context = null; // get and store app context
    private String words_source_file = "source_words.txt";
    private String json_map_file_name = "words_json_dictionary.txt";

    //private JSONArray words_adjacency_matrix = new JSONArray();

    public JSONObject getWordsMap() throws NullPointerException {
        return this.words;
    }
    public Context getContext() throws NullPointerException {
        return this.context;
    }
    public String getFileName() {
        return this.json_map_file_name;
    }

    public Graph(Context c) {
        this.words = new JSONObject();
        this.context = c;
    }
    public Graph(Context c, JSONObject map) {
        this.words = map;
        this.context = c;
    }

    public void build_words_map() throws JSONException {
        // build words dictionary
        this.get_word_keys(); // put word keys into dict
        this.get_word_values(); // put words' neighbors as their values
    }

    public void read_json_map_from_assets() throws JSONException {
        try {
            AssetManager am = context.getAssets();
            InputStream is = am.open(json_map_file_name);
            Scanner s = new Scanner(is);

            String json_data_as_string = "";
            while (s.hasNextLine()) {
                json_data_as_string += s.nextLine();
            }
            s.close();

            this.words = new JSONObject(json_data_as_string);
            Log.d(TAG, "JSON map length:\n" + words.length());

        } catch (IOException ioe) {
            Log.d(TAG, "Error. Opening map file from assets failed.");
        }
    }

    public void write_to_internal_file() {
        File rootDirOfApp = context.getFilesDir();
        File targetFile = new File(rootDirOfApp, json_map_file_name);

        try {
            FileWriter fw = new FileWriter(targetFile);
            fw.write(words.toString());
            fw.close();
        } catch (IOException ioe) {
            Log.d(TAG, "Error. Failed to write to internal file.");
            ioe.printStackTrace();
        }
    }

    // put the initial words from txt file into dictionary as keys
    public void get_word_keys() throws JSONException {
        try {
            AssetManager am = context.getAssets();
            InputStream words_file = am.open(this.words_source_file);

            //File words_file = new File(file_name);
            Scanner words_scanner = new Scanner(words_file);

            String word = words_scanner.nextLine();
            while (words_scanner.hasNextLine()) {
                if (word.length() == 4) {
                    Log.d(TAG, "Putting word key: " + word);
                    this.words.put(word, new JSONArray());
                }
                word = words_scanner.nextLine();
            }

        } catch (IOException ioe) {
            Toast.makeText(context, "Error. Getting word keys for map failed.", Toast.LENGTH_SHORT).show();
            ioe.printStackTrace();
        }
    }

    @NonNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        JSONArray word_keys = words.names();
        for (int i = 0; i < words.length(); i++) {
            String w = null;
            try {
                w = word_keys.getString(i);
                sb.append(w + ": " + words.optJSONArray(w) + '\n');
            } catch (JSONException e) {
                Log.d(TAG, "Error. Making graph string failed.");
            }
        }
        return sb.toString();
    }

    // neighbors => same length with exactly one letter different between them
    private boolean isNeighbor(String word1, String word2) {
        if (word1.length() != word2.length()) return false;
        if (word1.equals(word2)) return false;

        int differ_count = 0;
        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                differ_count++;
            }
        }

        return (differ_count == 1);
    }

    // generate the words' neighboring words (one-letter difference between them) into their array-list values
    public void get_word_values() throws JSONException {
        JSONArray word_keys = words.names();

        for (int i = 0; i < Objects.requireNonNull(word_keys).length(); i++) {
            String word = word_keys.getString(i);
            Log.d(TAG, "Putting neighbors for word: " + word);

            // find word's neighbors in key set
            JSONArray word_neighbors = new JSONArray();

            for (int j = 0; j < word_keys.length(); j++) {
                String other_word = word_keys.getString(j);

                if (this.isNeighbor(word, other_word)) {
                    word_neighbors.put(other_word);
                }
            }
            this.words.put(word, word_neighbors);
        }
    }

    // breadth-first search
    public ArrayList<String> get_solution_path(String start_word, String end_word) throws JSONException {
        ArrayList<String> path = new ArrayList<String>();

        LinkedList<String> words_queue = new LinkedList<String>();
        Map<String, String> words_visited_from = new HashMap<String, String>();
        ArrayList<String> visited = new ArrayList<String>();

        words_queue.addLast(start_word);
        while (!words_queue.isEmpty()) {
            String next_word = words_queue.removeFirst();

            if (!next_word.equals(end_word) && !visited.contains(next_word)) {
                //Log.d(TAG, "Next word: " + next_word);
                JSONArray neighbors = words.optJSONArray(next_word);

                if (neighbors != null) {
                    for (int i = 0; i < neighbors.length(); i++) {
                        String neighbor = neighbors.getString(i);
//                        Log.d(TAG, "Checking neighbor: " + neighbor);

                        if ((!visited.contains(neighbor))) {
                            words_queue.addLast(neighbor);
                            words_visited_from.put(neighbor, next_word);
                        }

                    }
                }
            }
            visited.add(next_word);

            // get solution path found
            if (next_word.equals(end_word)) {
                Log.d(TAG, "Path found!");
                //Toast.makeText(context.getApplicationContext(), "Solution path found!\n Game is now ready to play", Toast.LENGTH_LONG).show();

                String next_path_word = next_word;
                path.add(next_path_word);
                while (!next_path_word.equals(start_word)) {
                    next_path_word = words_visited_from.get(next_path_word);
                    path.add(next_path_word);

                }
                Collections.reverse(path);
                
                break;
            }
        }
        Log.d(TAG, "Solution path: " + path.toString());

        //if (path.isEmpty()) throw new IllegalArgumentException("Error. No solution path found.");
        return path;
    }
}