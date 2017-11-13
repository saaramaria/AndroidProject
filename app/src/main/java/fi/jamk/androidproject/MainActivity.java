package fi.jamk.androidproject;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

/*
Super Awesome Space Game
Demo for Android Application Development-course

Saara Virtanen & Konsta Hallinen 2017
 */

public class MainActivity extends AppCompatActivity {
    // The monkeys are placed in a "grid": 5 LinearLayouts that each have 3 ImageViews in them (see grid.xml)
    ImageView iv_11, iv_12, iv_13,
            iv_21, iv_22, iv_23,
            iv_31, iv_32, iv_33,
            iv_41, iv_42, iv_43,
            iv_51, iv_52, iv_53;

    int tileLocationRow1, tileLocationRow2, tileLocationRow3, tileLocationRow4, tileLocationRow5;
    int monkeyComing, monkeyClickable, emptyTile;

    // Play-button is in the bottom_bar.xml
    Button b_play;

    // Timer and scores are in the top_bar.xml
    TextView tv_time, tv_score, tv_best;
    int currentScore = 0;
    int bestScore = 0;
    CountDownTimer timer;

    Random r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // activity_main.xml
        setContentView(R.layout.activity_main);

        // Get the strings for current score, best score and timer (easier to modify languages)
        String scoreText = getApplicationContext().getResources().getString(R.string.score_text);
        final String bestText = getApplicationContext().getResources().getString(R.string.best_text);
        final String timeText = getApplicationContext().getResources().getString(R.string.time_text);

        // Create and show a dialog when opening the app (instructions)
        final Dialog dialog = new Dialog(this);
        // Transparent bg so all the cool styles in the xml-files will show properly
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        // We don't need the default title bar
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Set custom layout for the dialog (see dialog.xml in layout)
        dialog.setContentView(R.layout.dialog);
        // If the dialog's OK-button is clicked, close the dialog
        Button dialogButton = (Button) dialog.findViewById(R.id.b_dialog);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

        // We can use SharedPreferences to save and restore the high score, since it's just a simple int
        // Restore preferences (saved high score)
        SharedPreferences savedScores = getSharedPreferences("PREFS", 0);
        bestScore = savedScores.getInt("highscore", 0);

        // Init the "tiles" (=monkeys)
        // 1 is the top row, 5 bottom row
        iv_11 = (ImageView) findViewById(R.id.iv_11);
        iv_12 = (ImageView) findViewById(R.id.iv_12);
        iv_13 = (ImageView) findViewById(R.id.iv_13);

        iv_21 = (ImageView) findViewById(R.id.iv_21);
        iv_22 = (ImageView) findViewById(R.id.iv_22);
        iv_23 = (ImageView) findViewById(R.id.iv_23);

        iv_31 = (ImageView) findViewById(R.id.iv_31);
        iv_32 = (ImageView) findViewById(R.id.iv_32);
        iv_33 = (ImageView) findViewById(R.id.iv_33);

        iv_41 = (ImageView) findViewById(R.id.iv_41);
        iv_42 = (ImageView) findViewById(R.id.iv_42);
        iv_43 = (ImageView) findViewById(R.id.iv_43);

        iv_51 = (ImageView) findViewById(R.id.iv_51);
        iv_52 = (ImageView) findViewById(R.id.iv_52);
        iv_53 = (ImageView) findViewById(R.id.iv_53);

        // Play button
        b_play = (Button) findViewById(R.id.b_play);

        // Score
        tv_score = (TextView) findViewById(R.id.tv_score);
        tv_score.setText(scoreText + " " + currentScore);

        // High score
        tv_best = (TextView) findViewById(R.id.tv_best);
        tv_best.setText(bestText + " " + bestScore);

        // Timer
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_time.setText(timeText + " " + millisToSecs(20000));

        // New random, used later
        r = new Random();

        // Load images
        loadImages();

        // Create a new timer that will start at 20s and go down 1 per second, when it hits 0 the game will be over
        // Calls millisToSecs() to convert the milliseconds to seconds
        timer = new CountDownTimer(20000, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_time.setText(timeText + " " + millisToSecs(millisUntilFinished));
            }

            // Game over, empty game and set new high score if the score was better than the last high score
            @Override
            public void onFinish() {
                tv_time.setText(timeText + " " + millisToSecs(0));

                emptyGame();

                // Show toast that the game ended (center of screen)
                Toast toastGameOver = Toast.makeText(MainActivity.this, R.string.gameover_text, Toast.LENGTH_SHORT);
                toastGameOver.setGravity(Gravity.CENTER, 0, 0);
                toastGameOver.show();

                if(currentScore > bestScore) {
                    bestScore = currentScore;
                    tv_best.setText(bestText + bestScore);

                    // Show toast about new high score (center of screen)
                    Toast toastBest = Toast.makeText(MainActivity.this, R.string.highscore_text, Toast.LENGTH_SHORT);
                    toastBest.setGravity(Gravity.CENTER, 0, 0);
                    toastBest.show();

                    // Save possible new high score to shared preferences
                    SharedPreferences savedScores = getSharedPreferences("PREFS", 0);
                    SharedPreferences.Editor editor = savedScores.edit();
                    editor.putInt("highscore", bestScore);
                    // Commit new high score
                    editor.apply();
                }
            }
        };
        // Play again, call initGame() that initalizes the game
        b_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initGame();
            }
        });
    }

    // Initialize game
    private void initGame() {
        String scoreText = getApplicationContext().getResources().getString(R.string.score_text);

        // Make the bottom row "clickable" and the play-button invisible
        iv_51.setEnabled(true);
        iv_52.setEnabled(true);
        iv_53.setEnabled(true);
        b_play.setVisibility(View.INVISIBLE);

        // Set click listeners to the bottom row columns (see if player hits or misses the monkey)
        iv_51.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If player taps the monkey, game continues (call continueGame())
                if(tileLocationRow5 == 1) {
                    continueGame();
                    // If player missclicks, game ends (call endGame())
                }else {
                    endGame();
                }
            }
        });

        iv_52.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tileLocationRow5 == 2) {
                    continueGame();
                }else {
                    endGame();
                }
            }
        });

        iv_53.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tileLocationRow5 == 3) {
                    continueGame();
                }else {
                    endGame();
                }
            }
        });

        // Set current score to 0 and start the timer
        currentScore = 0;
        tv_score.setText(scoreText + " " + currentScore);
        timer.start();

        // Create the first monkeys, the middle one on the bottom row (5) is clickable on start
        // Row5
        tileLocationRow5 = 2;
        iv_52.setImageResource(monkeyClickable);

        // The monkeys on rows 4-1 are randomly placed (calls setTileLocation with a random place number)
        // r.nextInt logic:
        // Range is 1-3 since we want a number that is 1, 2, or 3 (3 columns in a row)
        // => nextInt(max-min + 1) + 1 => nextInt(3-1 + 1) + 1 => nextInt(3) + 1

        // Row 4
        tileLocationRow4 = r.nextInt(3) + 1;
        setTileLocation(tileLocationRow4, 4);

        // Row 3
        tileLocationRow3 = r.nextInt(3) + 1;
        setTileLocation(tileLocationRow3, 3);

        // Row 2
        tileLocationRow2 = r.nextInt(3) + 1;
        setTileLocation(tileLocationRow2, 2);

        // Row 1
        tileLocationRow1 = r.nextInt(3) + 1;
        setTileLocation(tileLocationRow1, 1);
    }

    // What happens when the game is in process and player hasn't missclicked
    private void continueGame() {
        String scoreText = getApplicationContext().getResources().getString(R.string.score_text);

        // In the game monkeys "go down" by one, but remain in their current column
        // This is created on each row by switching places between the next row monkey
        // For example: if the monkey on row 4 is in column 2, the monkey in row 5 will be placed to the same column after the next move, and so on

        // Row 5
        tileLocationRow5 = tileLocationRow4;
        setTileLocation(tileLocationRow5, 5);

        // Row 4
        tileLocationRow4 = tileLocationRow3;
        setTileLocation(tileLocationRow4, 4);

        // Row 3
        tileLocationRow3 = tileLocationRow2;
        setTileLocation(tileLocationRow3, 3);

        // Row 2
        tileLocationRow2 = tileLocationRow1;
        setTileLocation(tileLocationRow2, 2);

        // Row 1 (TOP ROW): Generates the new monkey in the top row to a random position
        tileLocationRow1 = r.nextInt(3) + 1;
        setTileLocation(tileLocationRow1, 1);

        // Score increases by 1 when a monkey is clicked
        currentScore++;
        tv_score.setText(scoreText + " " + currentScore);
    }

    // End the game when player missclicks
    private void endGame() {
        // Stop timer
        timer.cancel();

        // Empty monkeys from the screen
        emptyGame();

        // Show toast telling the player they failed (center of screen)
        Toast toastFailed = Toast.makeText(MainActivity.this, R.string.failed_text, Toast.LENGTH_SHORT);
        toastFailed.setGravity(Gravity.CENTER, 0, 0);
        toastFailed.show();
    }

    // Empty the screen
    private void emptyGame() {
        //Can't click on the bottom row
        iv_51.setEnabled(false);
        iv_52.setEnabled(false);
        iv_53.setEnabled(false);

        // Make the play-button visible again
        b_play.setVisibility(View.VISIBLE);

        // Set empty images to all the tiles
        iv_11.setImageResource(emptyTile);
        iv_12.setImageResource(emptyTile);
        iv_13.setImageResource(emptyTile);

        iv_21.setImageResource(emptyTile);
        iv_22.setImageResource(emptyTile);
        iv_23.setImageResource(emptyTile);

        iv_31.setImageResource(emptyTile);
        iv_32.setImageResource(emptyTile);
        iv_33.setImageResource(emptyTile);

        iv_41.setImageResource(emptyTile);
        iv_42.setImageResource(emptyTile);
        iv_43.setImageResource(emptyTile);

        iv_51.setImageResource(emptyTile);
        iv_52.setImageResource(emptyTile);
        iv_53.setImageResource(emptyTile);
    }

    // Set monkeys to the tile-grid
    // First checks which row is called, then where to place the monkey-image (3 possible columns), other columns are left empty
    private void setTileLocation(int place, int row) {
        // Rows 1-4 always have the "coming" monkey image
        if(row == 1) {
            iv_11.setImageResource(emptyTile);
            iv_12.setImageResource(emptyTile);
            iv_13.setImageResource(emptyTile);

            switch(place){
                case 1:
                    iv_11.setImageResource(monkeyComing);
                    break;
                case 2:
                    iv_12.setImageResource(monkeyComing);
                    break;
                case 3:
                    iv_13.setImageResource(monkeyComing);
                    break;
            }
        }

        if(row == 2) {
            iv_21.setImageResource(emptyTile);
            iv_22.setImageResource(emptyTile);
            iv_23.setImageResource(emptyTile);

            switch(place){
                case 1:
                    iv_21.setImageResource(monkeyComing);
                    break;
                case 2:
                    iv_22.setImageResource(monkeyComing);
                    break;
                case 3:
                    iv_23.setImageResource(monkeyComing);
                    break;
            }
        }

        if(row == 3) {
            iv_31.setImageResource(emptyTile);
            iv_32.setImageResource(emptyTile);
            iv_33.setImageResource(emptyTile);

            switch(place){
                case 1:
                    iv_31.setImageResource(monkeyComing);
                    break;
                case 2:
                    iv_32.setImageResource(monkeyComing);
                    break;
                case 3:
                    iv_33.setImageResource(monkeyComing);
                    break;
            }
        }

        if(row == 4) {
            iv_41.setImageResource(emptyTile);
            iv_42.setImageResource(emptyTile);
            iv_43.setImageResource(emptyTile);

            switch(place){
                case 1:
                    iv_41.setImageResource(monkeyComing);
                    break;
                case 2:
                    iv_42.setImageResource(monkeyComing);
                    break;
                case 3:
                    iv_43.setImageResource(monkeyComing);
                    break;
            }
        }

        // Bottom row (5), so always the clickable monkey image
        if(row == 5) {
            iv_51.setImageResource(emptyTile);
            iv_52.setImageResource(emptyTile);
            iv_53.setImageResource(emptyTile);

            switch(place){
                case 1:
                    iv_51.setImageResource(monkeyClickable);
                    break;
                case 2:
                    iv_52.setImageResource(monkeyClickable);
                    break;
                case 3:
                    iv_53.setImageResource(monkeyClickable);
                    break;
            }
        }
    }

    // Convert milliseconds to seconds (used in the timer)
    private int millisToSecs(long millis) {
        return (int) millis / 1000;
    }

    // Load the monkey images to the tiles, images are stored in drawable
    private void loadImages() {
        monkeyComing = R.drawable.monkey;
        monkeyClickable = R.drawable.monkey_happy;
        emptyTile = R.drawable.empty;
    }
}