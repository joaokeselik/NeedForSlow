package megadroid.drivinggame.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import megadroid.drivinggame.R;
import megadroid.drivinggame.controller.ScoreMonitor;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    private ScoreMonitor monitor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        monitor =new ScoreMonitor();

        //Crete image buttons
        ImageButton playButton;
        ImageButton shopButton;
        ImageButton muteSoundButton;
        ImageButton exitButton;

        //set the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //getting the buttons
        playButton = (ImageButton) findViewById(R.id.buttonPlay);
        shopButton = (ImageButton) findViewById(R.id.buttonShop);
        muteSoundButton = (ImageButton) findViewById(R.id.muteSound);
        exitButton = (ImageButton) findViewById(R.id.exit);

        //adding a click listener to buttons
        playButton.setOnClickListener(this);
        shopButton.setOnClickListener(this);
        muteSoundButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);


        //read scores from JSON file
        readJson();
    }

    // the onclick methods to handle clicking different buttons
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //the transition from MenuActivity to GameActivity
            case R.id.buttonPlay:
                startActivity(new Intent(MenuActivity.this, GameActivity.class));
                break;

            //the transition from MenuActivity to ShopActivity
            case R.id.buttonShop:
                startActivity(new Intent(MenuActivity.this, ShopActivity.class));
                break;

            case R.id.muteSound:
                startActivity(new Intent(MenuActivity.this, PauseActivity.class));
                break;

            case R.id.exit:
                //Intent startMain = new Intent(Intent.ACTION_MAIN);
                //startMain.addCategory(Intent.CATEGORY_HOME);
                //startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(startMain);
                //break;

                startActivity(new Intent(MenuActivity.this,exitButton.class));

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
      readJson();

    }

    private void readJson(){
        try {

            String values = monitor.readJSON(this.getApplicationContext(),"Menu");

            if(values.isEmpty()){
                Toast.makeText(this,"No Scores",Toast.LENGTH_LONG).show();
            }else
            {

                TextView txtHighscore = (TextView) findViewById(R.id.txtHighScore);
                txtHighscore.setText("High Score : "+Integer.toString(monitor.getHighScore()));
            }

            //Toast.makeText(this,textToPrint,Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

}
