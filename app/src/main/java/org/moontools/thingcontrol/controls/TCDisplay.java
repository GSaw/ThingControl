package org.moontools.thingcontrol.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.moontools.thingcontrol.TCController;

/**
 * Created by georg on 12.11.16.
 */

public class TCDisplay extends LinearLayout implements TCControl {

    private final String TAG = TCDisplay.class.getName();

    private String type;
    private String name;
    private String units;
    private TextView title;
    private TextView display;
    private TCController controller;

    public TCDisplay(Context context) {
        super(context);
    }

    public TCDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TCDisplay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void configure(JSONObject config) throws JSONException {
        name = config.getString("name");
        type = config.getString("type");
        units = config.optString("units", "");
        this.title = (TextView) getChildAt(0);
        View v = (View) getChildAt(1);
        if(v instanceof ScrollView) {
            this.display = (TextView) ((ScrollView) v).getChildAt(0);
        } else {
            this.display = (TextView) getChildAt(1);
        }
        title.setText(config.getString("title"));
        display.setText("");
    }

    @Override
    public void addToGui(TCController controller, GridLayout mContentView) {
        this.controller = controller;
        Log.d(TCDisplay.class.getName(), "Count children " + getChildCount());
        mContentView.addView(this);
    }

    @Override
    public String getName() {
        return name;
    }

    public void appendText(String s) {
        CharSequence text = display.getText();
        display.setText(text + "\n" + s);
    }
    public void setText(String s) {
        display.setText(s);
    }

    @Override
    public void processMessage(JSONObject msg) throws JSONException {
        Log.d(TAG, "process message " + msg + " of type " + type);
        switch(type) {
            case "display":
            case "display_l":
                display.setText(msg.getString("payload") + " " + units);
                break;
        }
    }
}
