package org.moontools.thingcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.GridLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.moontools.thingcontrol.controls.TCControl;
import org.moontools.thingcontrol.controls.TCControlFabric;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses JSON Object with layout configuration and generates Controls.
 * Created by georg on 12.11.16.
 */

public class ControlConfigurator {

    public static enum Orientation {
        portrait,
        landscape
    };


    JSONObject data;
    private Context context;
    private GridLayout parent;

    private List<TCControl> controls;
    private Orientation orientation;

    public Orientation getOrientation() {
        return orientation;
    }
    public List<TCControl> getControls() {
        return controls;
    }

    public ControlConfigurator(GridLayout parent, JSONObject data) {
        this.data = data;
        this.parent = parent;
        this.context = parent.getContext();
        if(null == data) {
            LogMessages.log(context.getResources().getString(R.string.missing_configuration));
        }
        extractData();
    }

    private void extractData() {
        try {
            controls = extractControls();
            extractOrientation();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void extractOrientation() throws JSONException {
        String orString = data.getString("orientation");
        if(orString == null) {
            LogMessages.err(context.getResources().getString(R.string.missing_orientation));
        }
        for(Orientation o : Orientation.values()) {
            if(o.name().equals(orString)) {
                orientation = o;
                return;
            }
        }
        LogMessages.err(context.getResources().getString(R.string.unknown_orientation));
    }

    public boolean validate() throws JSONException {
        return !LogMessages.isErrorLogged();
    }

    private List<TCControl> extractControls() throws JSONException {
        List<TCControl> controls = new ArrayList<TCControl>();
        JSONArray arr = data.optJSONArray("controls");
        if(arr == null) {
            LogMessages.err(context.getResources().getString(R.string.missing_controls));
            return null;
        }
        LayoutInflater inflater = LayoutInflater.from(context);

        for(int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            TCControl control = TCControlFabric.createInstance(obj, inflater, parent);
            if(control != null) {
                controls.add(control);
            }
        }
        if(controls.isEmpty()) {
            LogMessages.err(context.getResources().getString(R.string.missing_controls));
        }
        return controls;
    }

    public void processMessage(JSONObject msg) throws JSONException {
        String name = msg.getString("control");
        for(TCControl control : controls) {
            if(control.getName().equals(name)) {
                control.processMessage(msg);
            }
        }
    }

}
