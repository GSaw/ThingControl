package org.moontools.thingcontrol;

import org.json.JSONObject;

/**
 * Created by georg on 16.11.16.
 */

public interface TCController {
    public void sendMessage(JSONObject msg);
}
