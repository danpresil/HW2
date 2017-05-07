package com.example.dan_p.hw2.activities;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dan_p.hw2.R;
import com.example.dan_p.hw2.database.PlayerRecord;
import com.example.dan_p.hw2.database.RecordsOpenHelper;
import com.example.dan_p.hw2.minesweeper.DifficultyLevel;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {
    private static final String TAG = ResultActivity.class.getSimpleName();

    private EditText editTextPlayerName;
    private Button buttonEnterName;
    private PlayerRecord playerRecord;
    private int time;
    private DifficultyLevel level;
    private RecordsOpenHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        db = new RecordsOpenHelper(getApplicationContext());

        playerRecord = getIntent().getExtras().getParcelable("record");
        if (playerRecord != null) {
            this.time = playerRecord.getTime();
            this.level = DifficultyLevel.valueOf(playerRecord.getDifficulty());
        }

        TextView textViewPlayerTime = (TextView)findViewById(R.id.player_bestTime_text);
        textViewPlayerTime.setText(String.format("Your time : %d", time));

        ArrayList<PlayerRecord> playerRecordArrayList = db.getTopRecords(level.toString(), 10);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.table_placeholder_leaderboard_results,
                new TableFragment().newInstance(playerRecordArrayList, level.toString()));
        ft.commit();

        int numberOfRecords = db.getNumberOfRecords(level.toString());
        int lowestTime  = db.getLowestRecordInTheTable(level.toString());

        if (numberOfRecords < 10 || (time < lowestTime)) {
            editTextPlayerName = (EditText)findViewById(R.id.edittext_player_name);
            buttonEnterName = (Button)findViewById(R.id.play_name_ok);
            buttonEnterName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = editTextPlayerName.getText().toString();
                    if (name.compareTo("") != 0) {
                        playerRecord.setName(name);

                        if (db.insertRecord(playerRecord))
                            Log.d(TAG, "INSERTED RECORD OF: " + playerRecord);
                        else
                            Log.d(TAG, "INSERT RECORD FAILED!: " + playerRecord);
                        finish();

                    }

                }
            });

            editTextPlayerName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        buttonEnterName.performClick();
                        handled = true;
                    }
                    return handled;
                }
            });
        }
        else {
            LinearLayout new_bestTimes_layout = (LinearLayout) findViewById(R.id.new_bestTime_layout);
            ((LinearLayout) new_bestTimes_layout.getParent()).removeView(new_bestTimes_layout);
        }

        GameActivity.gameActivity.finish();
    }
}
