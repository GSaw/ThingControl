package org.moontools.thingcontrol.controls;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.moontools.thingcontrol.R;
import org.moontools.thingcontrol.TCController;

/**
 * Created by georg on 11.11.16.
 */

public class TCButton extends FrameLayout implements View.OnTouchListener, TCControl, GestureDetector.OnGestureListener {
    private static final String TAG = TCButton.class.getName();
    private String title;
    private String name;
    private String payloadOn;
    private String payloadOff;
    private String state = "";
    private boolean toggle;
    private TCController controller;
    Drawable normal;
    Drawable click;

    Button button;
    GestureDetector gestureDetector;

    TCButton(Context context) {
        super(context);
    }
    public TCButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public TCButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addToGui(TCController controller, GridLayout mContentView) {
        this.controller = controller;
        if(mContentView instanceof GridLayout) {
            Log.d(TCButton.class.getName(), "columns = " + ((GridLayout)mContentView).getColumnCount());
        }
        normal = getResources().getDrawable(R.drawable.buttonshape);
        click = getResources().getDrawable(R.drawable.buttonshape_click);

        gestureDetector = new GestureDetector(this.getContext(), this);

        this.button.setOnTouchListener(this);

        mContentView.addView(this);

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d(TAG, "On touch " + name );
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.button.setTextColor(getResources().getColor(R.color.black));
                this.button.setBackground(click);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.button.setTextColor(getResources().getColor(R.color.blue));
                this.button.setBackground(normal);
                break;
        }
        gestureDetector.onTouchEvent(motionEvent);
        return false;
    }

    @Override
    public void configure(JSONObject config) throws JSONException {

        this.title = config.getString("title");
        this.name = config.getString("name");
        this.payloadOn = config.getString("payload_on");
        this.payloadOff = config.optString("payload_off", "off");
        this.toggle = config.optBoolean("toggle", false);

        this.button = (Button)this.getChildAt(0);
        this.button.setText(this.title);
    }

    @Override
    public void processMessage(JSONObject msg) throws JSONException {
        Log.d(TAG, "bt message for " + name);
        this.state = msg.getString("payload");
        //TODO: show state change
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        Log.d(TAG, "On Click " + name );
        String payload = payloadOn;
        if(toggle) {
            if (state.equals(payloadOff)) {
                state = payload = payloadOn;

            } else {
                state = payload = payloadOff;
            }
        }
        try {
            Log.d(TAG, "Send payload " + payload);
            this.controller.sendMessage(new JSONObject("{'control':'" + name + "','payload':'" + payload + "'}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
}
