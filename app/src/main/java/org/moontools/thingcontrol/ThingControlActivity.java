package org.moontools.thingcontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.moontools.thingcontrol.controls.TCControl;
import org.moontools.thingcontrol.controls.TCDisplay;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ThingControlActivity extends AppCompatActivity implements Communicator.CommunicationClient, TCController {

    private static final String TAG = ThingControlActivity.class.getName();

    private static void debug(String msg) {
        Log.d(TAG, msg);
    }

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    private static final int HANDLE_CONNTECTIONSTATE = 0;
    private static final int HANDLE_MESSAGE = 1;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;


    private Communicator comm;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private GridLayout mContentView;
    private ImageView mConnectionIcon;
    private Animation mAlertAnimation;

    ControlConfigurator mConfigurator;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what) {
                case HANDLE_CONNTECTIONSTATE:
                    switch ((Communicator.ConnectionState) msg.obj) {
                        case open:
                            mConnectionIcon.clearAnimation();
                            mConnectionIcon.setImageResource(R.drawable.ic_connected);
                            break;
                        case closed:
                            mConnectionIcon.setImageResource(R.drawable.ic_disconnected);
                            mConnectionIcon.startAnimation(mAlertAnimation);
                            break;
                    }
                    break;
                case HANDLE_MESSAGE:
                    try {
                        mConfigurator.processMessage(new JSONObject((String)msg.obj));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

            }
        }
    };


    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            //mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    TCDisplay display;



    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_thing_control);

        mVisible = true;
       // mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = (GridLayout)findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        mConnectionIcon = (ImageView)findViewById(R.id.connectionIcon);

        mAlertAnimation = new AlphaAnimation(1, 0);
        mAlertAnimation.setDuration(500);
        mAlertAnimation.setInterpolator(new LinearInterpolator());
        mAlertAnimation.setRepeatCount(Animation.INFINITE);
        mAlertAnimation.setRepeatMode(Animation.REVERSE);
        createStatusTerminal();

        comm = new Communicator(this);
        comm.queryConfiguration();

    }

    private void createStatusTerminal() {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        display = (TCDisplay)inflater.inflate(R.layout.tc_terminal_large, mContentView, false);
        try {
            display.configure(new JSONObject("{'name':'log_console','title':'protokoll','type':'terminal_l'}"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        mContentView.addView(display);
        LogMessages.registerDisplay(display);
        LogMessages.log("Loading configuration...");

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getApplicationContext();
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void buildDashboard(JSONObject config) throws JSONException {


        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        debug("dpHeight = " + dpHeight);
        debug("dpWidth = " + dpWidth);
        mConfigurator = new ControlConfigurator(mContentView, config);
        if(!mConfigurator.validate()) {
            Log.e(TAG, "configuration failed!");
            return;
        }
        mContentView.removeAllViews();
        List<TCControl> controls = mConfigurator.getControls();
        List<List<String>> layout = null;
        int orientation = getResources().getConfiguration().orientation;
        debug("Orientation " + getResources().getConfiguration().orientation + "(" +Configuration.ORIENTATION_LANDSCAPE + "," + Configuration.ORIENTATION_PORTRAIT + "," + Configuration.ORIENTATION_UNDEFINED);

        switch(mConfigurator.getOrientation()) {
            case portrait:
                //TODO: apply portrait layout here
                break;
            case landscape:
                //TODO: apply landscape layout here
                break;
        }

        for(TCControl control : controls) {
            control.addToGui(this, mContentView);
        }
        comm.createWebsocketConenction();
    }

    @Override
    public Context getContext() {
        return this.getApplicationContext();
    }

    @Override
    public String getConfigurationUrl() {
        return "http://192.168.178.34:1880/dashboard/thermostat";
    }

    @Override
    public String getWebSocketUrl() {
        return "ws://192.168.178.34:1880/dashboard/thermostat/ws";
    }

    @Override
    public void onMessage(String message) {
        debug("received message " + message);
        Message msg = mHandler.obtainMessage(HANDLE_MESSAGE, message);
        msg.sendToTarget();
    }

    @Override
    public void processConfiguration(JSONObject config) {
        debug("Loaded response! " + config.toString());
        try {
            buildDashboard(config);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateConnectionState(Communicator.ConnectionState state) {
        debug("connection " + state.name());
        Message connectionStateMsg = mHandler.obtainMessage(HANDLE_CONNTECTIONSTATE, state);
        connectionStateMsg.sendToTarget();
    }

    @Override
    public void sendMessage(JSONObject msg) {
        debug("send message " + msg);
        comm.sendMessage(msg);
    }
}
