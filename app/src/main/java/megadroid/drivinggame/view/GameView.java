package megadroid.drivinggame.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import megadroid.drivinggame.R;
import megadroid.drivinggame.model.Items;
import megadroid.drivinggame.model.Player;
import megadroid.drivinggame.model.Star;

/**
 * Created by megadroids.
 */

public class GameView extends SurfaceView implements Runnable {

    //properties of the background image and instantiation of the background class
    public static float WIDTH;
    public static float HEIGHT;
    private Background bg;
    private GameActivity ga = new GameActivity();
    private int screenX;
    private int screenY;
    private Items[] item;
    //Adding 3 items you
    private int itemCount = 2;
    private ArrayList<Star> stars = new ArrayList<Star>();

    //Controls speed of the background scroll
    // public static final int MOVESPEED = -10;

    //boolean variable to track if the game is playing or not
    volatile boolean playing;

    volatile int playingCounter=0;

    //the game thread
    private Thread gameThread = null;

    //adding the player to this class
    private Player player;

    //These objects will be used for drawing
    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    //Class constructor
    public GameView(Context context, int screenX, int screenY) {
        super(context);

        //initializing player object
        //this time also passing screen size to player constructor
        player = new Player(context, screenX, screenY);

        int starNums = 100;
        for (int i = 0; i < starNums; i++) {
            Star s = new Star(screenX, screenY);
            stars.add(s);

        }

        //initializing drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        this.screenX = screenX;
        this.screenY = screenY;


        //initializing items object array
        item = new Items[itemCount];
        for (int j = 0; j < itemCount; j++) {
            item[j] = new Items(this.getContext(), screenX, screenY);
        }

    }
    public void run() {
            while (playing) {
                //to update the frame
                update();

                //to draw the frame
                draw();

                //to control
                control();
            }
        }



    private void update() {
        //updating player position
        player.update();

        //Update background

        bg.update();

        // update the stars
        for(Star s : stars) {
            s.update(player.getSpeed());
        }

        //updating the item coordinate with respect to player speed
        for(int i=0; i<itemCount; i++){
            item[i].update(player.getSpeed());

            //if collision occurrs with player
            if (Rect.intersects(player.getDetectCollision(), item[i].getDetectCollision())) {


                //moving item outside the left edge
                item[i].setY(-200);
            }

        }

    }

    private void draw() {

        //checking if surface is valid
        if (surfaceHolder.getSurface().isValid()) {

            //locking the canvas
            canvas = surfaceHolder.lockCanvas();
            //drawing a background color for canvas
            canvas.drawColor(Color.BLACK);


            //Scaling the background for different sizes of screens
            float scaleFactorX = (float) screenX / (WIDTH * 1.f);
            float scaleFactorY = (float) screenY / (HEIGHT * 1.f);

            if (canvas != null) {
                //Saving the state of the canvas before scaling
                final int savedState = canvas.save();
                canvas.scale( scaleFactorX,scaleFactorY);
                bg.draw(canvas);
                canvas.restoreToCount(savedState);

                //drawing the items
                    for (int i = 0; i < itemCount; i++) {
                        canvas.drawBitmap(
                                item[i].getBitmap(),
                                item[i].getX(),
                                item[i].getY(),
                                paint
                        );
                    }


                //Draw the stars and set colour to white
                paint.setColor(Color.WHITE);
                for(Star s : stars) {
                    paint.setStrokeWidth(s.getStarWidth());
                    canvas.drawPoint(s.getX(), s.getY(), paint);
                }


                //Drawing the player
                canvas.drawBitmap(
                        player.getBitmap(),
                        player.getX(),
                        player.getY(),
                        paint);


            }

            //Unlocking the canvas
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }


    private void control() {
        try {
            gameThread.sleep(27);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        //when the game is paused
        //setting the variable to false
        playing = false;

        try {
            //stopping the thread
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        //when the game is resumed
        //starting the thread again

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.backgroundcanvas));
        bg.setVector(-10);
        WIDTH = BitmapFactory.decodeResource(getResources(), R.drawable.backgroundcanvas).getWidth();
        HEIGHT= BitmapFactory.decodeResource(getResources(), R.drawable.backgroundcanvas).getHeight();
        gameThread = new Thread(this);
        gameThread.start();
        playing = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int z = 0;
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                //When the user presses on the screen
                //stopping the boosting when screen is released


                //int cellY = (int)motionEvent.getY();

                player.stopBoosting();
                break;


            case MotionEvent.ACTION_DOWN:
                //When the user releases the screen
                //boosting the space jet when screen is pressed

                int w = getWidth();
                int h = getHeight();
                int cellX = (int) motionEvent.getX();

                player.setBoosting(cellX);
                break;

        }
        return true;
    }
}
