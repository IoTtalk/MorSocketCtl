package tw.edu.nctu.pcslab.socketctl;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ControllerActivity extends AppCompatActivity {

    private final String TAG = "ControllerActivity";

    /* device list */
    private ArrayList<String> deviceList;
    private ArrayAdapter<String> deviceListAdapter;
    private String currentDevice;

    /* socket list */
    private ArrayList<String> socketList;
    private ArrayAdapter<String> socketListAdapter;

    /* appliance list */
    private List<String> applianceList;
    private ArrayAdapter<String> applianceListAdapter;

    /* handler */
    private Handler getDeviceListHander;
    private HandlerThread getDeviceListThread;
    private Handler getSocketListHander;
    private HandlerThread getSocketListThread;

    /*RESTful URL*/
    private String urlString = "http://192.168.11.103:8899";
    private String deviceListAPI = "device_list";
    private String socketListAPI = "socket_list";

    /*Mqtt client*/
    MqttAndroidClient mqttClient;
    private String mqttUri = "tcp://192.168.11.103:1883";
    private String clientId = "MorSocketAndroidClient";
    private String subscriptionTopic = "DeviceInfo";
    //private String publishTopic = "exampleAndroidPublishTopic";
    //private String publishMessage = "Hello World!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        currentDevice = null;

        //UI
        Spinner deviceListSpinner = (Spinner) findViewById(R.id.device_list_spinner);
        deviceList = new ArrayList<String>();
        deviceList.add(0, getString(R.string.select_morsocket_placeholder));
        deviceListAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.device_row_view, R.id.device_row_text_view, deviceList);
        deviceListSpinner.setAdapter(deviceListAdapter);

        ListView socketListView = (ListView) findViewById(R.id.socket_list_view);
        socketList = new ArrayList<String>();
        socketListAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.socket_row_view, R.id.socket_row_text_view, socketList);
        socketListView.setAdapter(socketListAdapter);

        /*Spinner appliancesListSpinner = (Spinner) findViewById(R.id.appliance_list_spinner);
        applianceList = Arrays.asList(getResources().getStringArray(R.array.appliances));
        applianceListAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.appliance_row_view, R.id.appliance_row_text_view, applianceList);
        appliancesListSpinner.setAdapter(applianceListAdapter);*/

        //Handler
        getDeviceListThread = new HandlerThread("getDeviceListThread");
        getDeviceListThread.start();
        getDeviceListHander = new Handler(getDeviceListThread.getLooper());

        getSocketListThread = new HandlerThread("getSocketListThread");
        getSocketListThread.start();
        getSocketListHander = new Handler(getDeviceListThread.getLooper());

        // click listener for deviceListSpinner
        deviceListSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (!(item.toString() == getString(R.string.select_morsocket_placeholder)))
                    currentDevice = item.toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //mqtt
        mqttClient = new MqttAndroidClient(getApplicationContext(), mqttUri, clientId);
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    //addToHistory("Reconnected to : " + serverURI);
                    Log.d(TAG, "Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    //addToHistory("Connected to: " + serverURI);
                    Log.d(TAG, "Connected to : " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                //addToHistory("The Connection was lost.");
                Log.d(TAG, "The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //addToHistory("Incoming message: " + new String(message.getPayload()));
                Log.d(TAG, "Incoming message: " + new String(message.getPayload()));

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

        });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        try {
            //addToHistory("Connecting to " + serverUri);
            mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //addToHistory("Failed to connect to: " + serverUri);
                    Log.d(TAG, exception.toString());
                    Log.d(TAG, "Failed to connect to: " + mqttUri);

                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }

    }

    public void subscribeToTopic(){
        try {
            mqttClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //addToHistory("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //addToHistory("Failed to subscribe");
                }
            });

            // THIS DOES NOT WORK!
            mqttClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    Log.d(TAG, "Message: " + topic + " : " + new String(message.getPayload()));
                }
            });

        } catch (MqttException ex){
            Log.d(TAG, "Exception whilst subscribing");
            ex.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_setup) {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
            return true;
        }
        else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //update device list
        /*getDeviceListHander.post(new Runnable() {
            @Override
            public void run() {
                getDeviceList();
                getDeviceListHander.postDelayed(this, 5000);
            }
        });
        //update socket list
        getSocketListHander.post(new Runnable() {
            @Override
            public void run() {
                if(currentDevice != null) {
                    getSocketList();
                }
                getSocketListHander.postDelayed(this, 1000);
            }
        });*/

    }

    @Override
    protected void onPause(){
        super.onPause();
        getDeviceListHander.removeCallbacksAndMessages(null);
        getSocketListHander.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDeviceListHander.removeCallbacksAndMessages(null);
        getSocketListHander.removeCallbacksAndMessages(null);
        getDeviceListThread.quit();
        getSocketListThread.quit();
    }

    void getDeviceList(){
        HttpURLConnection conn = null;
        try {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            URL url = new URL(urlString + "/" + deviceListAPI);
            Log.d(TAG, urlString + "/" + deviceListAPI);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.connect();
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
            String jsonString1 = reader.readLine();
            reader.close();
            // parse json
            String jsonString = jsonString1;
            JSONObject jsonObj = new JSONObject(jsonString);
            JSONArray devices = jsonObj.getJSONArray("devices");
            //loop to get all json objects from devices json array
            if(devices.length() != 0) {
                deviceList.clear();
                for (int i = 0; i < devices.length(); i++) {
                    String device = devices.getString(i);
                    deviceList.add(device);
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        deviceListAdapter.notifyDataSetChanged();
                    }
                });
            }
            if(currentDevice != null && !deviceList.contains(currentDevice)){
                Toast.makeText(getBaseContext(), R.string.device_disconnected, Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, jsonObj.get("devices").toString());
        }
        catch (ConnectException ce){
            deviceList.clear();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    deviceListAdapter.notifyDataSetChanged();
                }
            });
            Toast.makeText(getBaseContext(), R.string.server_error, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error: " + ce);
        }
        catch (Exception e) {
            Log.d(TAG, "Error: " + e);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    void getSocketList(){
        HttpURLConnection conn = null;
        try {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            URL url = new URL(urlString + "/" + socketListAPI + "/" + currentDevice);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.connect();
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
            String jsonString = reader.readLine();
            reader.close();
            // parse json
            JSONObject jsonObj = new JSONObject(jsonString);
            Log.d(TAG, jsonString);
            JSONArray sockets = jsonObj.getJSONArray("sockets");
            // loop to get all json objects from devices json array
            for(int i = 0; i < sockets.length(); i++) {
                String socket = sockets.getString(i);
                if(socket.length() == 1){
                    socket = "0" + socket;
                }
                if(!socketList.contains(socket)){
                    socketList.add(socket);
                    new Handler(Looper.getMainLooper()).post(new Runnable () {
                        @Override
                        public void run () {
                            socketListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
            Log.d(TAG, jsonObj.get("sockets").toString());
        }
        catch (ConnectException ce){
            socketList.clear();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    socketListAdapter.notifyDataSetChanged();
                }
            });
            Toast.makeText(getBaseContext(), R.string.server_error, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Error: " + ce);
        }
        catch (Exception e) {
            Log.d(TAG, "Error: " + e);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

}
