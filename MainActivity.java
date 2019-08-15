/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2,playerWord1,playerWord2;
    private Stack<LetterTile> placedTiles=new Stack<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if(word.length()==WORD_LENGTH) {
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
       // word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        //word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }
                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    if(v.getId()==R.id.word1){
                        playerWord1+= tile.moveToViewGroup((ViewGroup) v);
                    }else{
                        playerWord2+= tile.moveToViewGroup((ViewGroup) v);
                    }
                    if (stackedLayout.empty()) {
                        checkWin();
                    }
                    placedTiles.push(tile);
                    return true;
            }
            return false;
        }
    }

    public boolean onStartGame(View view) {
        LinearLayout word1LinearLayout=(LinearLayout) findViewById(R.id.word1);
        word1LinearLayout.removeAllViews();
        LinearLayout word2LinearLayout=(LinearLayout) findViewById(R.id.word2);
        word2LinearLayout.removeAllViews();
        stackedLayout.clear();

        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");

        Random random=new Random();

        word1=words.get(random.nextInt(words.size()));
        word2=words.get(random.nextInt(words.size()));

        int randomWord;//1st word or 2nd word
        boolean scrambled=false;
        int i=0,j=0;
        String scrambledWord="";
        while(!scrambled){
			randomWord=random.nextInt(2)+1;

            if(randomWord==1){//1st word
                scrambledWord+=word1.charAt(i);//adding letter
                if(word1.length()<=i+1){//if i is now last char,that is word 1 is exhausted
                    //then insert letters of word 2
                    for(;j<word2.length();j++){
                        scrambledWord+=word2.charAt(j);
                    }
                    scrambled=true;//done
                }else{
                    i++;
                }
            }else{//2ndword first selected
                scrambledWord+=word2.charAt(j);//adding letter
                if(word2.length()<=j+1){//if j is now last char,that is word 2 is exhausted
                    //then insert letters of word 1
                    for(;i<word1.length();i++){
                        scrambledWord+=word1.charAt(i);
                    }
                    scrambled=true;//done
                }else{
                    j++;
                }
            }
        }

        //messageBox.setText(scrambledWord);

        for (int count=scrambledWord.length()-1;count>=0;count--){
           stackedLayout.push(new LetterTile(this,scrambledWord.charAt(count)));
        }


        return true;
    }

    public boolean onUndo(View view) {

        if(!placedTiles.isEmpty()){
            LetterTile popped= placedTiles.pop();

            popped.moveToViewGroup(stackedLayout);
        }


        return true;
    }

    private void checkWin(){
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        if(word1.equals(playerWord1) && word2.equals(playerWord2))
            messageBox.setText("You win! " + word1 + " " + word2);
        else if(words.contains(playerWord1) && words.contains(playerWord2)){
            messageBox.setText("You found alternative words! " + playerWord1 + " " + playerWord2);
        }
        else{
            messageBox.setText("Try again");
        }
    }
}
