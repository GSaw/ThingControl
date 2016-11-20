package org.moontools.thingcontrol;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by georg on 12.11.16.
 */

public class Communicator implements Response.Listener<JSONObject>, Response.ErrorListener  {

    private static final String TAG = Communicator.class.getName();

    private RequestQueue queue;
    private WebSocketClient wsClient = null;
    private CommunicationClient commClient;

    private static void debug(String msg) {
        Log.d(TAG, msg );
    }

    private static void error(String msg) {
        Log.e(TAG, msg );
    }
    private Object watchdog = new Object();
    private boolean connected = false;

    Thread watchdogThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //debug("wd: check connection");

                synchronized (watchdog) {
                    if (!connected) {
                        if(wsClient != null) {
                            wsClient.close();
                        }
                        debug("try to re-connect ws client");
                        URI uri;
                        try {
                            uri = new URI(commClient.getWebSocketUrl());
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                            return;
                        }
                        debug("create new ws connection");
                        wsClient = new WSConenctor(uri);
                        try {
                            debug("start connection blocking");
                            wsClient.connectBlocking();
                            debug("connected");

                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    });

    @Override
    public void onErrorResponse(VolleyError error) {
        String error_msg = "unknown reason";
        if(error instanceof TimeoutError) {
            error_msg = "connection timeout";
        } else if(error instanceof NoConnectionError) {
            error_msg = error.getMessage();
        }
        LogMessages.err("Connection error: " + error_msg);
        Log.e(TAG, "Error loading configuration " + error_msg);
        error.printStackTrace();
    }

    @Override
    public void onResponse(JSONObject response) {
        commClient.processConfiguration(response);
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendMessage(JSONObject msg) {
        Log.d(TAG, "send message " + msg + " (" + isConnected() + ")");
        if(isConnected()) {
            this.wsClient.send(msg.toString());
        }
    }

    private class WSConenctor extends  WebSocketClient {


        public WSConenctor(URI serverURI) {
            super(serverURI, new Draft_17(), null, 1000);
        }

        @Override
        public void onOpen (ServerHandshake handshakedata){
            commClient.updateConnectionState(ConnectionState.open);
            synchronized (watchdog) {
                connected = true;
            }
        }

        @Override
        public void onMessage (String message){
            commClient.onMessage(message);
        }

        @Override
        public void onClose ( int code, String reason,boolean remote){
            commClient.updateConnectionState(ConnectionState.closed);
            //TODO: try to reestablish communication
            synchronized (watchdog) {
                connected = false;
            }
        }

        @Override
        public void onError (Exception ex){
            //TODO: what should we do here?
            debug("ws conenction failed! " + ex.getMessage());
            ex.printStackTrace();
        }
    };


    public static enum ConnectionState {
        closed,
        open
    }

    public interface CommunicationClient {
        public Context getContext();

        public String getConfigurationUrl();
        public String getWebSocketUrl();

        public void onMessage(String message);

        public void processConfiguration(JSONObject config);
        public void updateConnectionState(ConnectionState state);

    }

    public Communicator(CommunicationClient client) {
        this.commClient = client;
    }

    public void createWebsocketConenction() {

        watchdogThread.start();
    }


    public void queryConfiguration() {
        debug("create queue");
        queue = Volley.newRequestQueue(commClient.getContext());
        String url = commClient.getConfigurationUrl();
        debug("add query " + url);
        Request request = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

}
