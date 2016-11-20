package org.moontools.thingcontrol.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.moontools.thingcontrol.TCController;

/**
 * Created by georg on 12.11.16.
 */

public class TCPlaceholder extends FrameLayout implements TCControl {

    private String name;

    public TCPlaceholder(Context context) {
        super(context);
    }

    public TCPlaceholder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TCPlaceholder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void configure(JSONObject config) throws JSONException {
        name = config.getString("name");
    }

    @Override
    public void addToGui(TCController controller, GridLayout mContentView) {
        mContentView.addView(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void processMessage(JSONObject msg) throws JSONException {

    }
}
