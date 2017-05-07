package com.example.dan_p.hw2.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.dan_p.hw2.R;
import com.example.dan_p.hw2.database.PlayerRecord;
import com.example.dan_p.hw2.database.RecordsOpenHelper;
import com.example.dan_p.hw2.minesweeper.DifficultyLevel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private DifficultyLevel level = DifficultyLevel.EASY;

    private static int numberOfDifficulties = DifficultyLevel.values().length;
    private TextView textViewBestTimes[] = new TextView[numberOfDifficulties];
    static int bestTimes[] = new int[numberOfDifficulties];
    static String names[] = new String[numberOfDifficulties];

    RecordsOpenHelper recordsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordsDatabase = new RecordsOpenHelper(this);

        this.radioGroup = (RadioGroup) findViewById(R.id.radio_button_group_difficulty);
        this.textViewBestTimes[0] = (TextView) findViewById(R.id.text_easy_bestTimes);
        this.textViewBestTimes[1] = (TextView) findViewById(R.id.text_normal_bestTimes);
        this.textViewBestTimes[2] = (TextView) findViewById(R.id.text_hard_bestTimes);
        this.radioGroup.check(R.id.radioButtonEasyDifficulty);

        loadPlayerBestTimes();
        for (int i = 0 ; i < numberOfDifficulties ; i++ )
            if (bestTimes[i] != -1)
                textViewBestTimes[i].setText(String.format("Best time : %d - %s",
                        bestTimes[i], names[i]));
            else
                textViewBestTimes[i].setText(R.string.best_time_not_set_text);


        Button buttonNewGame = (Button) findViewById(R.id.START_NEW_GAME_BUTTON);
        if (buttonNewGame != null) {
            buttonNewGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setLevelByRadioButton();
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("level", level.toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }

        Button buttonToAllResults = (Button) findViewById(R.id.BUTTON_TO_LEADERBOARDS);
        if (buttonToAllResults != null) {
            buttonToAllResults.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setLevelByRadioButton();
                    Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("level", level.toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        savePlayersBestTimes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlayerBestTimes();

        for (int i = 0 ; i < numberOfDifficulties ; i++ )
            if (bestTimes[i] != -1)
                textViewBestTimes[i].setText(String.format("Best time : %d - %s",
                        bestTimes[i], names[i]));
            else
                textViewBestTimes[i].setText(R.string.best_time_not_set_text);

        TextView textViewMainTitle = (TextView) findViewById(R.id.textTitleGameName);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(textViewMainTitle, "scaleX", 0f, 1f);
        objectAnimator.start();
    }

    private void loadPlayerBestTimes() {
        RecordsOpenHelper db = new RecordsOpenHelper(getApplicationContext());

        ArrayList<PlayerRecord> playerRecordArrayList = db.getTopRecords(DifficultyLevel.EASY.toString(), 1);
        if (playerRecordArrayList.size() == 1) {
            bestTimes[0] = playerRecordArrayList.get(0).getTime();
            names[0] = playerRecordArrayList.get(0).getName();
        } else {
            bestTimes[0] = -1;
            names[0] = "";
        }

        playerRecordArrayList = db.getTopRecords(DifficultyLevel.NORMAL.toString(), 1);
        if (playerRecordArrayList.size() == 1) {
            bestTimes[1] = playerRecordArrayList.get(0).getTime();
            names[1] = playerRecordArrayList.get(0).getName();
        } else {
            bestTimes[1] = -1;
            names[1] = "";
        }

        playerRecordArrayList = db.getTopRecords(DifficultyLevel.HARD.toString(), 1);
        if (playerRecordArrayList.size() == 1) {
            bestTimes[2] = playerRecordArrayList.get(0).getTime();
            names[2] = playerRecordArrayList.get(0).getName();
        } else {
            bestTimes[2] = -1;
            names[2] = "";
        }

    }

    private void savePlayersBestTimes() {
        SharedPreferences.Editor timesEditor = getSharedPreferences("times", MODE_PRIVATE).edit();
        for (int i = 0 ; i < numberOfDifficulties ; i++) {
            timesEditor.putString("player" + i + "name", names[i]);
            if (names[i] == null)
                timesEditor.putInt("player" + i + "time", -1);
            else
                timesEditor.putInt("player" + i + "time", bestTimes[i]);
        }

        timesEditor.apply();
    }


    private void setLevelByRadioButton() {
        int selectedDifficulty = radioGroup.getCheckedRadioButtonId();

        switch (selectedDifficulty) {
            case R.id.radioButtonEasyDifficulty:
                level = DifficultyLevel.EASY;
                break;
            case R.id.radioButtonNormalDifficulty:
                level = DifficultyLevel.NORMAL;
                break;
            case R.id.radioButtonHardDifficulty:
                level = DifficultyLevel.HARD;
                break;
        }

    }
}