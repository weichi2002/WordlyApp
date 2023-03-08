package edu.fandm.wchou.myapplication;


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Graph {
    private static final String TAG = "GRAPH";
    public Map<String, ArrayList<String>> words;
    private Context context; // get and store app context

    public Graph(Context c, String file_name) {
        this.words = new HashMap<String, ArrayList<String>>();
        this.context = c;

        Log.d(TAG, "Building word paths, one moment...");

        // build words dictionary
        this.get_word_keys(file_name); // put word keys into dict
        this.get_word_values(words); // put words' neighbors as their values

        // writing built words map to a file as a string-converted JSONObject
        write_to_internal_file();
    }

    // **Still need to work on this part**

    private void write_to_internal_file() {
        // write dictionary to an assets file to hold it in app's internal storage
        File rootDirOfApp = context.getFilesDir();
        File targetFile = new File(rootDirOfApp, "words_json_dictionary.txt");

        JSONObject word_dict_as_json = new JSONObject(words);
        String json_map_as_string = word_dict_as_json.toString();


        try {
            FileWriter fw = new FileWriter(targetFile);
            fw.write(json_map_as_string);
            fw.close();
        } catch (IOException ioe) {
            Toast.makeText(context.getApplicationContext(), "Failed to write to file!", Toast.LENGTH_LONG).show();
            ioe.printStackTrace();
        }
        Log.d(TAG, "Wrote to: " + targetFile.getAbsolutePath());
    }

    // put the initial words from txt file into dictionary as keys
    public void get_word_keys(String file_name) {
        try {
            AssetManager am = context.getAssets();
            InputStream words_file = am.open(file_name);

            //File words_file = new File(file_name);
            Scanner words_scanner = new Scanner(words_file);

            String word = words_scanner.nextLine();
            while (words_scanner.hasNextLine()) {
                this.words.put(word, new ArrayList<String>());
                word = words_scanner.nextLine();
            }

        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String w : words.keySet()) {
            sb.append(w + ": " + words.get(w) + '\n');
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
    public void get_word_values(Map<String, ArrayList<String>> words_dict) {
        Set<String> word_keys = words.keySet();
        for (String word : word_keys) {
            // find word's neighbors in key set
            ArrayList<String> word_neighbors = new ArrayList<String>();
            for (String other_word : word_keys) {
                if (this.isNeighbor(word, other_word)) {
                    word_neighbors.add(other_word);
                }
            }
            words_dict.put(word, word_neighbors);
        }

        // send built words dictionary to GameConfig's static field "words_dict"
        //GameConfig.words_graph = this.words;
    }

    // breadth-first search
    public ArrayList<String> get_solution_path(String start_word, String end_word) {
        ArrayList<String> path = new ArrayList<String>();

        LinkedList<String> words_queue = new LinkedList<String>();
        Map<String, String> words_visited_from = new HashMap<String, String>();
        ArrayList<String> visited = new ArrayList<String>();

        words_queue.addLast(start_word);
        while (!words_queue.isEmpty()) {
            String next_word = words_queue.removeFirst();
            visited.add(next_word);

            if (!next_word.equals(end_word)) {
                Log.d(TAG, "Next word: " + next_word);
                ArrayList<String> neighbors = words.get(next_word);

                for (String neighbor : neighbors) {
                    if ((!visited.contains(neighbor))) {
                        words_queue.addLast(neighbor);
                        words_visited_from.put(neighbor, next_word);
                    }
                }
            }
            // get solution path found
            if (next_word.equals(end_word)) {
                Log.d(TAG, "Path found!");
                Toast.makeText(context.getApplicationContext(), "Solution path found!\n Game is now ready to play", Toast.LENGTH_LONG).show();

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

        if (path.isEmpty()) throw new IllegalArgumentException("Error. No solution path found.");
        return path;
    }
}