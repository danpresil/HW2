package com.example.dan_p.hw2.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.dan_p.hw2.R;
import com.example.dan_p.hw2.database.PlayerRecord;
import com.example.dan_p.hw2.minesweeper.CellButton;
import com.example.dan_p.hw2.minesweeper.CellButtonListener;
import com.example.dan_p.hw2.minesweeper.DifficultyLevel;
import com.example.dan_p.hw2.minesweeper.GameLogic;
import com.example.dan_p.hw2.minesweeper.GameState;
import com.example.dan_p.hw2.service.SensorService;
import com.example.dan_p.hw2.utils.Utils;

import java.util.Arrays;

import static com.example.dan_p.hw2.utils.Utils.generateRandom;

public class GameActivity extends AppCompatActivity implements CellButtonListener, SensorService.MyServiceListener, LocationListener {
    final int DEFAULT_ROWS = 10;
    final int DEFAULT_COLUMNS = 10;
    final int DEFAULT_MINES = 10;

    public static GameActivity gameActivity;

    private RelativeLayout relativeLayout;
    private GameLogic gameLogic;
    private GridLayout gameGrid;
    private CellButton[][] cellButtons;
    private RadioGroup actionsGroup;
    private TextView textViewNumberOfMinesLeft;
    private Button buttonRestartGame;
    private DifficultyLevel level;
    private TextView textViewBestTime;
    private Thread timer;

    private AnimatorSet animatorSetTranslation = new AnimatorSet();
    private AnimatorSet animatorSetMine = new AnimatorSet();

    private static final String TAG = GameActivity.class.getSimpleName();
    private SensorService myService;

    private LocationManager locationManager;
    boolean locationEnabled;
    private double latitude;
    private double longitude;
    private double rotation;

    // Helps to notify about the connection (has the connected / disconnected events)
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            // Step 3
            if (serviceBinder instanceof SensorService.ServiceBinder) {
                // We got THIS serviceBinder from the event 'onBind' in our service.
                setService(((SensorService.ServiceBinder) serviceBinder).getService());
            }
            Log.d(TAG, "onServiceConnected: " + name);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Step N
            setService(null);
            Log.d(TAG, "onServiceDisconnected: " + name);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationEnabled = false;
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            locationEnabled = true;
        }

        // Step 1
        boolean bindingSucceeded = bindService(new Intent(this, SensorService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "onCreate: " + (bindingSucceeded ? "the binding succeeded..." : "the binding failed!"));

        gameActivity = this;
        timer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(gameLogic.getGameState() != GameState.WON ||
                            gameLogic.getGameState() != GameState.LOST ){
                        timer.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewBestTime.setText(String.format("Time: %d",gameLogic.getTime()));
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        this.textViewBestTime = (TextView) findViewById(R.id.text_current_time);
        textViewBestTime.setText("Time: 0");

        this.gameLogic = createGameLogic();
        timer.start();
        this.gameGrid = createGameGrid();
        this.actionsGroup = (RadioGroup) findViewById(R.id.radio_group_actions);
        this.actionsGroup.check(R.id.mine_button);

        this.buttonRestartGame = (Button) findViewById(R.id.button_restart_game);
        this.buttonRestartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameActivity.this.finish();
                GameActivity.this.startActivity(getIntent());
            }
        });

        this.relativeLayout = (RelativeLayout)findViewById(R.id.relativelayout_activity_game);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.gameLogic.setRunTimer(true);

        if (myService != null)
            myService.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.gameLogic.setRunTimer(false);
        overridePendingTransition(0, 0);

        if (myService != null) {
            // Step 9 (if needed, depends on your implementation)
            myService.stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Because 'onDestroy' will kill also our 'serviceConnection', we must unbind to prevent 'ServiceConnectionLeaked' errors
        unbindService(serviceConnection);
    }

    private GameLogic createGameLogic() {
        GameLogic gameLogic= null;

        Bundle b = getIntent().getExtras();
        String levelName = null;
        if (b != null) {
            levelName = b.getString("level");
            try {
                this.level = DifficultyLevel.valueOf(levelName);
                gameLogic = new GameLogic(this.level);
            } catch (IllegalArgumentException e ) {
                gameLogic = new GameLogic(DEFAULT_ROWS, DEFAULT_COLUMNS, DEFAULT_MINES);
            }
        }
        else
            gameLogic = new GameLogic(DEFAULT_ROWS, DEFAULT_COLUMNS, DEFAULT_MINES);

        this.cellButtons = new CellButton[gameLogic.getRows()][gameLogic.getColumns()];

        return gameLogic;
    }

    private GridLayout createGameGrid() {
        this.gameGrid = (GridLayout)findViewById(R.id.game_grid);
        this.gameGrid.setColumnCount(this.gameLogic.getColumns());
        this.gameGrid.setRowCount(this.gameLogic.getRows());
        this.gameGrid.setOrientation(GridLayout.HORIZONTAL);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int buttonSize;
        if ( size.x < size.y )
            buttonSize = size.x / (this.gameLogic.getColumns() + 1);
        else
            buttonSize = size.y / (this.gameLogic.getRows() + 1);

        for (int row = 0 ; row < this.gameLogic.getRows() ; row++ ) {
            for (int column = 0 ; column < this.gameLogic.getColumns() ; column++ ) {
                CellButton cellButton = new CellButton(getApplicationContext());
                cellButton.setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));
                cellButton.setPosition(row, column);
                cellButton.setListener(this);
                cellButton.setPadding(1, 1, 1 , 1);

                if (level == DifficultyLevel.HARD)
                    cellButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35.f);
                else
                    cellButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20.f);

                cellButtons[row][column] = cellButton;
                gameGrid.addView(cellButton);
            }
        }
        updateGameActivity();
        return gameGrid;
    }

    private void updateGameActivity() {
        for (int row = 0 ; row < this.gameLogic.getRows() ; row++ ) {
            for (int column = 0 ; column < this.gameLogic.getColumns() ; column++ ) {
                CellButton cellButton = cellButtons[row][column];
                GameLogic.Cell cell = this.gameLogic.getGameGrid()[row][column];

                if (cell.isRevealed()) {
                    cellButton.setBackgroundResource(R.drawable.pressedbutton);
                    if (cell.isMine()) {
                        cellButton.setMine(true);
                        cellButton.setBackgroundResource(R.drawable.cellminebackground);
                    }
                    else if (cell.getValue() > 0) {
                        cellButton.setMine(false);
                        cellButton.setText(String.format("%d", cell.getValue()));
                        cellButton.setTextColor(pickColorByValue(cell.getValue()));
                    }
                    else
                        cellButton.setText("");
                    cellButton.setClickable(false);
                }
                else {
                    if (cell.isFlagged())
                        cellButton.setBackgroundResource(R.drawable.flaggedcell);
                    else
                        cellButton.setBackgroundResource(R.drawable.clickablecell);
                    cellButton.setText("");
                    cellButton.setClickable(true);
                }
            }
        }
        
        textViewNumberOfMinesLeft = (TextView) findViewById(R.id.text_mines_left);
        if (textViewNumberOfMinesLeft != null)
            textViewNumberOfMinesLeft.setText(String.format("Mines left: %d",
                    gameLogic.numberOfMinesLeft()));

    }

    @Override
    public void buttonClick(CellButton cellButton) {
        switch (actionsGroup.getCheckedRadioButtonId()) {
            case R.id.mine_button:
                gameLogic.makeMove(cellButton.getRow(), cellButton.getColumn());
                break;
            case R.id.flag_button:
                gameLogic.flag(cellButton.getRow(), cellButton.getColumn());
                break;
        }

        updateGameActivity();
        checkWin();
    }

    private void checkWin() {
        if (gameLogic.getGameState() == GameState.WON) {
            PlayEndGameAnimation(GameState.WON);

            for (int row = 0 ; row < this.gameLogic.getRows() ; row++ )
                for (int column = 0 ; column < this.gameLogic.getColumns() ; column++ )
                    cellButtons[row][column].setEnabled(false);

            sendResultToResultActivity();
        }
        else if (gameLogic.getGameState() == GameState.LOST) {
            PlayEndGameAnimation(GameState.LOST);

            for (int row = 0 ; row < this.gameLogic.getRows() ; row++ )
                for (int column = 0 ; column < this.gameLogic.getColumns() ; column++ )
                    cellButtons[row][column].setEnabled(false);

            Utils.shortVibrate(this);
        }
    }

    private void PlayEndGameAnimation(GameState gameState) {
        if (gameState == GameState.WON) {
            buttonRestartGame.setText("; )");
            relativeLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorValue1));
            buttonRestartGame.setEnabled(false);

            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(buttonRestartGame ,
                    "rotation", 0f, 360f);
            objectAnimator.start();
        } else if (gameState == GameState.LOST) {
            buttonRestartGame.setText(": (");
            relativeLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorValue3));

            for (int row = 0 ; row < this.gameLogic.getRows() ; row++ )
                for (int column = 0 ; column < this.gameLogic.getColumns() ; column++ ) {
                    CellButton cellButton = cellButtons[row][column];
                    ObjectAnimator objectAnimatorX;
                    ObjectAnimator objectAnimatorY;

                    if (!cellButton.isMine()) {
                        float x = (float)generateRandom(-1500, 1500);
                        float y = (float)generateRandom(-1500, 1500);
                        objectAnimatorX = ObjectAnimator.ofFloat(cellButton,"translationX", x);
                        objectAnimatorY = ObjectAnimator.ofFloat(cellButton,"translationY", y);

                        objectAnimatorX.setDuration(1000);
                        objectAnimatorY.setDuration(1000);

                        animatorSetTranslation.playTogether(objectAnimatorY, objectAnimatorX);
                    }

                    else {
                        cellButton.setBackgroundResource(R.drawable.minepostgame);
                        objectAnimatorX = ObjectAnimator.ofFloat(cellButton, "scaleX", 0f, 1f);
                        objectAnimatorY = ObjectAnimator.ofFloat(cellButton, "scaleY", 0f, 1f);

                        animatorSetMine.playTogether(objectAnimatorX, objectAnimatorY);
                    }
                }

            animatorSetMine.start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animatorSetTranslation.start();

                }
            }, 350);
        }
    }

    private void sendResultToResultActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(GameActivity.this, ResultActivity.class);


                PlayerRecord record = new PlayerRecord();
                record.setTime(gameLogic.getTime());
                record.setDifficulty(level.name());
                if (locationEnabled) {
                    record.setLatitude(latitude);
                    record.setLongitude(longitude);
                }
                else {
                    record.setLatitude(0);
                    record.setLongitude(0);
                }
                Log.d(TAG, "SET ROTATION: rotation: " + rotation);
                record.setRotation(rotation);
                intent.putExtra("record", record);
                startActivity(intent);
            }
        }, 3000);

    }


    @Override
    public void onSensorEvent(float[] values) {
        // Step 8... 8... 8
        if (gameLogic != null && gameLogic.getGameState() == GameState.IN_PROGRESS) {
            rotation = values[3];

            if (values[1] > 7f ) {
                relativeLayout.setBackgroundColor(Color.rgb(128, 0, 0));
                gameLogic.addMine();
                updateGameActivity();
            }
            else
                relativeLayout.setBackgroundColor(Color.rgb(109, 123, 132));
        }
    }

    public void setService(SensorService service) {
        if (service != null) {
            // Step 4
            this.myService = service;
            service.setListener(this);
            service.startListening();
        } else {
            // Step N + 1
            if (this.myService != null) {
                // Let's remove the listener
                this.myService.setListener(null);
            }
            this.myService = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location!= null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d(TAG, "LOCATION:"+ latitude + "," + longitude + "," + rotation);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        latitude = 0;
        longitude = 0;
    }

    private int pickColorByValue(int value) {
        switch(value) {
            case 1:
                return ContextCompat.getColor(this, R.color.colorValue1);
            case 2:
                return ContextCompat.getColor(this, R.color.colorValue2);
            case 3:
                return ContextCompat.getColor(this, R.color.colorValue3);
            case 4:
                return ContextCompat.getColor(this, R.color.colorValue4);
            case 5:
                return ContextCompat.getColor(this, R.color.colorValue5);
            case 6:
                return ContextCompat.getColor(this, R.color.colorValue6);
            case 7:
                return ContextCompat.getColor(this, R.color.colorValue7);
            case 8:
                return ContextCompat.getColor(this, R.color.colorValue8);
        }

        return 0;
    }
}

