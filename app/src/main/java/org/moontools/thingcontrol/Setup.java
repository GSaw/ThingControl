package org.moontools.thingcontrol;

/**
 * Created by georg on 12.11.16.
 */

public class Setup {
    public static Setup getInstance() {
        return new Setup();
    }

    public String getConfigurationUrl() {
        return "http://192.168.178.34:1880/dashboard/thermostat";
    }

    public String getWebsocketUrl() {
        return "http://192.168.178.34:1880/dashboard/thermostat/ws";
    }
}
