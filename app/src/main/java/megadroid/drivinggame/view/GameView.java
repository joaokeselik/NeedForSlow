package megadroid.drivinggame.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;
import megadroid.drivinggame.R;
import megadroid.drivinggame.controller.Generator;
import megadroid.drivinggame.model.Background;
import megadroid.drivinggame.model.Boom;
import megadroid.drivinggame.model.Items;
import megadroid.drivinggame.model.Obstacles;
import megadroid.drivinggame.model.Player;
import megadroid.drivinggame.model.SoundHelper;
import megadroid.drivinggame.model.Star;

/**
 * Class used to generate the Game screen with the methods to update and draw the canvas
 */

public class GameView extends SurfaceView implements Runnable,SensorEventListener {

    //Accelerator X value
    public static float xAccel, xVel = 0.0f;

    //Sensor Manager that controls the tilt
    private SensorManager sensorManager;

    //used to count when the crystal item will be released
    private int counter;

    //music player
    private SoundHelper msoundHelper;

    //properties of the background image and instantiation of the background class
    private Items[] item;
    private Items[] item1;
    private Items[] item2;
    //Adding 3 items you
    private int itemCount = 2;
    private int itemCount1 =1;
    private ArrayList<Star> stars = new ArrayList<Star>();

    //boolean variable to track if the game is playing or not
    volatile boolean playing;

    volatile int playingCounter = 0;

    //the game thread
    private Thread gameThread = null;

    //adding the player to this class
    private Player player;

    //These objects will be used for drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    //created a reference of the class Friend
    private Obstacles obstacles;
    private Obstacles obstacles2;
    private Obstacles obstacles3;
    private Obstacles obstacles4;

    //an indicator if the game is Over
    private boolean isGameOver;

    //defining a boom object to display blast
    private Boom boom;

    //properties of the background image and instantiation of the background class
    public static float WIDTH;//640;
    public static float HEIGHT;//1440;
    private Background bg;

    //properties to calculate score
    private int screenX;
    private int screenY;
    private int score;
    private int highScore;
    private int points;

    private Generator generator;
    private int muteFlag;

    //pause button properties
    private Bitmap pauseButton;
    private boolean pausePop;

    private int bgSpeed;
    private boolean highscorebeaten;
    private int prevMusic;
    private String currentTheme;

    /**
     * Constructor method used to initialise all the game objects
     * @param context
     * @param screenX - the width of the screen
     * @param screenY - the height of the screen
     * @param muteFlag - flag indicating if mute button is pressed
     */
    public GameView(Context context, int screenX, int screenY, int muteFlag) {
        super(context);

        this.muteFlag = muteFlag;

        //background speed
        bgSpeed = -25;
        generator = new Generator(context);
        //setting the score to 0 initially
        score = 0;
        points= generator.getPoints();
        //get JSON values
        highScore = generator.getHighScore();
        int selectedCar = generator.getSelectedCar();
        currentTheme = generator.getThemeVal();
        //play the music
        msoundHelper = new SoundHelper((Activity)this.getContext());
        prevMusic = generator.randomMainMusic(0);
        msoundHelper.prepareMusicPlayer((Activity)this.getContext(),prevMusic);
        if(muteFlag == 0) {
            msoundHelper.playMusic();
        }else
        {
            msoundHelper.pauseMusic();
        }

        //declaring Sensor Manager and sensor type
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        //initializing player object
        //this time also passing screen size to player constructor
        player = new Player(context, screenX, screenY,selectedCar,currentTheme);

        pauseButton = BitmapFactory.decodeResource(context.getResources(), R.drawable.button_pause);
        Bitmap bitmapCoin = BitmapFactory.decodeResource(context.getResources(), R.drawable.coin_gold);
        Bitmap bitmapCrystal = BitmapFactory.decodeResource(context.getResources(), R.drawable.crystal);

        //coins on the left side
        item = new Items[itemCount];
        for (int j = 0; j < itemCount; j++) {

            item[j] = new Items(this.getContext(), screenX * 2 - 450, screenY, bitmapCoin);
        }

        //coins on the right side
        item1 = new Items[itemCount];
        for (int k = 0; k < itemCount; k++) {

            item1[k] = new Items(this.getContext(), screenX * 3 - 150, screenY, bitmapCoin);
        }

        //the crystal item
        //coins on the right side
        item2 = new Items[itemCount1];
        for (int m = 0; m < itemCount1; m++) {
        item2[m] = new Items(this.getContext(), screenX * 2 - 400, screenY, bitmapCrystal);
        }

        //initializing drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        //initializing boom object
        boom = new Boom(context);

        //initializing the Friend class object
        //initializing the obstacles object
        Bitmap bitmap;
        Bitmap bitmapcar;
        Bitmap bitmapSecond;
        Bitmap bitmapThird;
        int starNums;
        float starwidth;

        //choose a different set of obstacles based on theme selected
        if(currentTheme.equals("space_theme")){

            //setting obstacles for space
            bitmap = BitmapFactory.decodeResource(this.getResources(), generator.randomObstacleSpace(0));
            bitmapcar = BitmapFactory.decodeResource(this.getResources(), generator.randomObstacleSpace(1));
            bitmapSecond = BitmapFactory.decodeResource(this.getResources(), generator.randomObstacleSpace(2));
            bitmapThird = BitmapFactory.decodeResource(this.getResources(), generator.randomObstacleSpace(3));
            starNums = 300;
            starwidth = 4.0f;

            obstacles = new Obstacles(this.getContext(), screenX, screenY, bitmap, 0+bitmap.getWidth()/2, 269);
            obstacles2 = new Obstacles(this.getContext(), screenX, screenY, bitmapcar, 720, screenX-(bitmapcar.getWidth()/2));
            obstacles3 = new Obstacles(this.getContext(), screenX, screenY, bitmapSecond, 550, 600);
            obstacles4 = new Obstacles(this.getContext(), screenX, screenY, bitmapThird, 380, 430);

        }else {

            //setting obstacles for road

            bitmap = BitmapFactory.decodeResource(this.getResources(), generator.randomObstacleCars(0));
            bitmapcar = BitmapFactory.decodeResource(this.getResources(), generator.randomObstacleCars(1));
            bitmapSecond = BitmapFactory.decodeResource(this.getResources(), generator.randomObstacleCars(2));
            bitmapThird = BitmapFactory.decodeResource(this.getResources(), generator.randomObstacleCars(3));
            starNums = 1000;
            starwidth = 6.0f;

            obstacles = new Obstacles(this.getContext(), screenX, screenY, bitmap, 220, 269);
            obstacles2 = new Obstacles(this.getContext(), screenX, screenY, bitmapcar, 720, 770);
            obstacles3 = new Obstacles(this.getContext(), screenX, screenY, bitmapSecond, 550, 600);
            obstacles4 = new Obstacles(this.getContext(), screenX, screenY, bitmapThird, 380, 430);
        }
        isGameOver = false;

        this.screenX = screenX;
        this.screenY = screenY;

        pausePop = false;

        //create the elements for the star list
        for (int i = 0; i < starNums; i++) {
            Star s = new Star(screenX, screenY,currentTheme);
            s.setStarWidth(starwidth);
            stars.add(s);

        }

        //use height and width of road as its larger than the space them
        WIDTH = BitmapFactory.decodeResource(getResources(), R.drawable.backgroundcanvas).getWidth();
        HEIGHT= BitmapFactory.decodeResource(getResources(), R.drawable.backgroundcanvas).getHeight();

    }

    /**
     * method invoked by the thread in the resume of activty
     */
    public void run() {
        while (playing) {
            //to update the elements on Canvas
            update();

            //to draw the elements in Canvas
            draw();

            //to control
            control();
        }
    }

    /**
     * Method used to update the elements in the canvas
     */
    private void update() {

        //increament counter for the release of the crystal
        if(playingCounter%50==0){
            counter++;
        }

        //incrementing score as time passes
        if(playingCounter%22==0) {
            score++;
        }

        //updating player position
        player.update();

        //update the background to move
        bg.update(playingCounter);

        // update the stars speed and position
        for(Star s : stars) {
            s.update(player.getSpeed());

        }

        if (playingCounter > 40) {
            for (int i = 0; i < itemCount; i++) {

                item[i].update(player.getSpeed());

                //if collision occurrs with player
                if (Rect.intersects(player.getDetectCollision(), item[i].getDetectCollision())) {
                    //moving item outside the topedge
                    item[i].setY(-200);
                    points++;
                    if (muteFlag == 0) {
                        msoundHelper.CoinCollection();
                    } else {
                        msoundHelper.pauseMusic();
                    }


                }

            }
        }

        if (playingCounter > 150) {

            for (int j = 0; j < itemCount; j++) {

                item1[j].update(player.getSpeed());

                //if collision occurrs with player
                if (Rect.intersects(player.getDetectCollision(), item1[j].getDetectCollision())) {
                    //moving item outside the topedge
                    item1[j].setY(-200);
                    points++;
                    if (muteFlag == 0) {
                        msoundHelper.CoinCollection();
                    } else {
                        msoundHelper.pauseMusic();
                    }

                }
            }
        }

        if (playingCounter > 500 ) {
            for (int m = 0; m < itemCount1; m++) {

                item2[m].update(player.getSpeed());

                //if collision occurrs with player
                if (Rect.intersects(player.getDetectCollision(), item2[m].getDetectCollision())) {
                    //moving item outside the topedge
                    item2[m].setY(-200);
                    points += 5;
                    msoundHelper.CoinCollection();
                }
            }
        }

        //setting boom outside the screen
        boom.setX(-250);
        boom.setY(-250);


        //updating the friend ships coordinates


        //checking for a collision between player and a racecar.  /**&& playingCounter < 1000*/
        Random generator = new Random();
        int increaseObstacleSpeed = generator.nextInt(5) + 15;
        if (playingCounter > 20) {
            obstacles2.update(player.getSpeed()+increaseObstacleSpeed);
            if (Rect.intersects(player.getDetectCollision(), obstacles2.getDetectCollision())) {
                gameOver(obstacles2);
            }
        }

        //checking for a collision between player and a car
        if (playingCounter > 260) {
            obstacles4.update(player.getSpeed()+increaseObstacleSpeed);
            if (Rect.intersects(player.getDetectCollision(), obstacles4.getDetectCollision())) {

                gameOver(obstacles4);
            }
        }


        //checking for a collision between player and a car
        if (playingCounter > 180) {
            obstacles.update(player.getSpeed()+increaseObstacleSpeed);
            if (Rect.intersects(player.getDetectCollision(), obstacles.getDetectCollision())) {

                gameOver(obstacles);
            }
        }

        //checking for a collision between player and a enemy
        if (playingCounter > 1000) {

            obstacles3.update(player.getSpeed()+increaseObstacleSpeed);
            if (Rect.intersects(player.getDetectCollision(), obstacles3.getDetectCollision())) {
                gameOver(obstacles3);
            }
        }

    }

    //Method for logic to be implemented when the player collides with the obstacle
    private void gameOver(Obstacles obstacles){

        //displaying the boom at the collision
        boom.setX(obstacles.getX());
        boom.setY(obstacles.getY());
        //setting playing false to stop the game
        playing = false;
        //setting the isGameOver true as the game is over
        isGameOver = true;
        //get the highscore
        if(highScore < score){
            highScore = score;
            highscorebeaten = true;
        }
        else {
            highscorebeaten = false;
        }


    }

    /**
     * Method used to draw the elements of the canvas
     */
    private void draw() {

        playingCounter++;

        //checking if surface is valid
        if (surfaceHolder.getSurface().isValid()) {

            //locking the canvas
            canvas = surfaceHolder.lockCanvas();
            //drawing a background color for canvas
            canvas.drawColor(Color.BLACK);


            //Scaling the background for different sizes of screens
            float scaleFactorX = (float) screenX / (WIDTH * 1.f);
            float scaleFactorY = (float) screenY / (HEIGHT * 0.8f);

            if (canvas != null) {
                //Saving the state of the canvas before scaling
                final int savedState = canvas.save();
                canvas.scale(scaleFactorX, scaleFactorY);
                bg.draw(canvas);
                canvas.restoreToCount(savedState);

            }

            if (playingCounter > 40) {
                //drawing the items
                for (int i = 0; i < itemCount; i++) {
                    canvas.drawBitmap(
                            item[i].getBitmap(),
                            item[i].getX(),
                            item[i].getY(),
                            paint
                    );
                }
            }

            if (playingCounter > 150) {
                //drawing the items
                for (int i = 0; i < itemCount; i++) {
                    canvas.drawBitmap(
                            item1[i].getBitmap(),
                            item1[i].getX(),
                            item1[i].getY(),
                            paint
                    );
                }
            }
            if (playingCounter > 500 ) {
                //drawing the items
                for (int i = 0; i < itemCount1; i++) {
                    canvas.drawBitmap(
                            item2[i].getBitmap(),
                            item2[i].getX(),
                            item2[i].getY(),
                            paint
                    );
                }
            }


            //Draw the stars and set colour to white
            paint.setColor(Color.WHITE);
            for (Star s : stars) {
                paint.setStrokeWidth(s.getStarWidth());
                canvas.drawPoint(s.getX(), s.getY(), paint);
            }


            //Drawing the player
            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),
                    player.getY(),
                    paint);

            //drawing boom image
            canvas.drawBitmap(
                    boom.getBitmap(),
                    boom.getX(),
                    boom.getY(),
                    paint
            );

            //drawing obstacles image
            //draw white car
            if (playingCounter > 180) {

                canvas.drawBitmap(

                        obstacles.getBitmap(),
                        obstacles.getX(),
                        obstacles.getY(),
                        paint
                );
            }
            //draw race car
            if (playingCounter > 20) {

                canvas.drawBitmap(

                        obstacles2.getBitmap(),
                        obstacles2.getX(),
                        obstacles2.getY(),
                        paint
                );

            }

            //draw red car
            if (playingCounter > 1000) {

                canvas.drawBitmap(

                        obstacles3.getBitmap(),
                        obstacles3.getX(),
                        obstacles3.getY(),
                        paint
                );

            }
            //draw 4th obstacle car
            if (playingCounter > 260) {

                canvas.drawBitmap(

                        obstacles4.getBitmap(),
                        obstacles4.getX(),
                        obstacles4.getY(),
                        paint
                );
            }

            // create a rectangle that we'll draw later
            RectF rectangle = new RectF(0, 0, screenX, screenY/15);
            paint.setColor(Color.BLACK);
            canvas.drawRect(rectangle, paint);

            //drawing the score on the game screen
            paint.setColor(Color.WHITE);
            paint.setTextSize(65);
            canvas.drawText("Score: " + score, screenX/2+50, 70, paint);

            //drawing the points on the game screen
            paint.setColor(Color.WHITE);
            paint.setTextSize(65);
            canvas.drawBitmap(item1[0].getBitmap(),20,0,paint);
            canvas.drawText(": " + points, item1[0].getBitmap().getWidth()+20, 70, paint);

            //pause button
            canvas.drawBitmap(

                    pauseButton,
                    screenX-pauseButton.getWidth(),
                    -5,
                    paint
            );

            //draw game Over when the game is over
            if (isGameOver) {
                Intent gameover = new Intent(getContext(), GameOverActivity.class);
                gameover.putExtra("highscorebeaten",highscorebeaten);
                gameover.putExtra("muteFlag",muteFlag);
                getContext().startActivity(gameover);
            }


            //Unlocking the canvas
            surfaceHolder.unlockCanvasAndPost(canvas);


        }
    }

    /**
     * Method used to control the thread for a period of time
     */
    private void control() {
        try {
            gameThread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to pause the activity
     */
    public void pause() {
        sensorManager.unregisterListener(this);
        //when the game is paused
        //setting the variable to false
        playing = false;

        bgSpeed = bg.getVector();
        try {
            //stopping the thread
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //write the score and points to JSON
        generator.writeJson(this.getContext(),highScore,points);

        if(isGameOver){
            if(muteFlag == 0) {
                //stop music when game is over
                //msoundHelper.CrashSound();
                msoundHelper.pauseMusic();
                msoundHelper.stopMusic();
                msoundHelper.prepareMusicPlayer3(this.getContext(),R.raw.car_crash);
                msoundHelper.playMusic();

                //stop the music
                msoundHelper.getmMusicPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                // Code to start the next audio in the sequence
                msoundHelper.pauseMusic();
                msoundHelper.stopMusic();
                msoundHelper = null;

                    }
                });

            }else
            {
                //stop music when mute is ON
                msoundHelper.pauseMusic();
                msoundHelper.stopMusic();
                msoundHelper = null;
            }

        }
        else {
            //stop music when going to Pause screen
            msoundHelper.pauseMusic();
            msoundHelper.stopMusic();
            msoundHelper = null;
        }

    }

    //method invoked on resume of activity
    public void resume() {
        pausePop = false;

        //when the game is resumed
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

        if(msoundHelper == null){
            msoundHelper = new SoundHelper((Activity)this.getContext());
            msoundHelper.prepareMusicPlayer((Activity)this.getContext(),prevMusic);
        }

        if(muteFlag == 0) {
            msoundHelper.playMusic();
        }else
        {
            msoundHelper.pauseMusic();
        }

        int selectedTheme =generator.getSelectedTheme();
        //starting the thread again
        bg = new Background(BitmapFactory.decodeResource(getResources(), selectedTheme));

        //updating the item coordinate with respect to player speed
        bg.setVector(bgSpeed);

        gameThread = new Thread(this);
        gameThread.start();
        playing = true;
    }

    /**
     * Method invoked when user touches the screen
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        if((motionEvent.getX(0)>=screenX - pauseButton.getWidth()) &&
                (motionEvent.getY(0)>=0) &&
                ( motionEvent.getX(0)<=screenX) &&
                (motionEvent.getY(0)<=pauseButton.getHeight()))
        {
            //pause button selected
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if(!pausePop) {

                    pausePop = true;
                    //write the score and points to JSON
                    //get the highscore
                    if(highScore< score){
                        highScore = score;
                    }
                    generator.writeJson(this.getContext(),highScore,points);

                    Intent pauseIntent = new Intent(getContext(), PauseActivity.class);
                    pauseIntent.putExtra("muteFlag",muteFlag);
                    getContext().startActivity(pauseIntent);

                }
            }
        }
        else {

            int z = 0;
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    //When the user presses on the screen
                    //stopping the boosting when screen is released

                    player.stopBoosting();
                    break;


                case MotionEvent.ACTION_DOWN:
                    //When the user releases the screen
                    //boosting the space jet when screen is pressed
                    int w = getWidth();
                    int h = getHeight();
                    int cellX = (int) motionEvent.getX();

                    player.setBoosting(cellX, true);
                    break;

            }
        }
        return true;
    }

    /**
     * Method invoked when the phone is tilted
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            xAccel = event.values[0];
            player.updatetilt();
        }
    }

    public void onAccuracyChanged (Sensor sensor,int i){

    }

}
