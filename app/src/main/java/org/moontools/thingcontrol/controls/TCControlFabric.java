package org.moontools.thingcontrol.controls;

import android.view.LayoutInflater;
import android.widget.GridLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.moontools.thingcontrol.LogMessages;
import org.moontools.thingcontrol.R;
import org.moontools.thingcontrol.tools.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by georg on 12.11.16.
 */

public class TCControlFabric {

    final static Map<String, Integer> CONTROLS = new HashMap<>();

    static {
        CONTROLS.put("button", R.layout.tc_button );
        CONTROLS.put("button_l", R.layout.tc_button_large );
        CONTROLS.put("button_s", R.layout.tc_button_small );
        CONTROLS.put("display", R.layout.tc_display );
        CONTROLS.put("display_l", R.layout.tc_display_large );
        CONTROLS.put("placeholder", R.layout.tc_placeholder );
        CONTROLS.put("placeholder_l", R.layout.tc_placeholder_large );
        CONTROLS.put("placeholder_s", R.layout.tc_placeholder_small );
    }

    public static TCControl createInstance(JSONObject config, LayoutInflater inflater, GridLayout parent) {

        try {
            String objType = config.getString("type");
            if(!CONTROLS.containsKey(objType)) {
                LogMessages.err("Error whiile parsing " + Utils.getFirstNChars(config.toString(), 15) + ": unknown type " + objType);
                return null;
            }
            TCControl c = (TCControl)inflater.inflate(CONTROLS.get(objType), parent, false);
            c.configure(config);
            return c;
        } catch (JSONException e) {
            LogMessages.err("Error whiile parsing " + Utils.getFirstNChars(config.toString(), 15) + ": " + e.getMessage());
            return null;
        }

    }
}
