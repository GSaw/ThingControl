package org.moontools.thingcontrol.controls;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.moontools.thingcontrol.R;
import org.moontools.thingcontrol.TCController;

/**
 * Interface for a generic ThingControl's Control.
 * Created by georg on 12.11.16.
 */

public interface TCControl {
    public void configure(JSONObject config) throws JSONException;
    public void addToGui(TCController controller, GridLayout mContentView);
    public String getName();
    public void processMessage(JSONObject msg) throws JSONException;

}
