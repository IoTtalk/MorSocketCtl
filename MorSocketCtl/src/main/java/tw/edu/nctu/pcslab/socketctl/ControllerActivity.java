package tw.edu.nctu.pcslab.socketctl;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ControllerActivity extends AppCompatActivity {

    private final String TAG = "ControllerActivity";

    /* device list for ui */
    private ArrayList<String> deviceList;
    private ArrayAdapter<String> deviceListAdapter;
    private String currentDevice;

    /* socket list for ui */
    private ArrayList<String> socketList;
    private ArrayAdapter<String> socketListAdapter;

    /* device and socket list relation */
    private LinkedHashMap<String, ArrayList<String>> deviceLinkedHashMap;

    /* appliance list */
    private List<String> applianceList;
    private ArrayAdapter<String> applianceListAdapter;

    /*Mqtt client*/
    MqttAndroidClient mqttClient;
    private String mqttUri = "tcp://192.168.1.232:1883";
    private String clientId = "MorSocketAndroidClient";
    // subscribe
    private String deviceInfoTopic = "DeviceInfo"; // when there is a device online
    private String devicesInfoTopic = "DevicesInfo"; // receive after SyncDeviceInfo
    // publish
    private String syncDeviceInfoTopic = "SyncDeviceInfo"; // when app open
    private String switchTopic = "Switch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        currentDevice = null;
        deviceLinkedHashMap = new LinkedHashMap<String, ArrayList<String>>();
        //UI
        Spinner deviceListSpinner = (Spinner) findViewById(R.id.device_list_spinner);
        deviceList = new ArrayList<String>();
        deviceList.add(0, getString(R.string.select_morsocket_placeholder));
        deviceListAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.device_row_view, R.id.device_row_text_view, deviceList);
        deviceListSpinner.setAdapter(deviceListAdapter);

        final ListView socketListView = (ListView) findViewById(R.id.socket_list_view);
        socketList = new ArrayList<String>();
        socketListAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.socket_row_view, R.id.socket_row_text_view, socketList);
        socketListView.setAdapter(socketListAdapter);
        socketListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // rebind onCheckedChanged event after Layout has been changed
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                for(int i = 0; i < socketListView.getChildCount(); i++){
                    Switch sswitch = (Switch) socketListView.getChildAt(i).findViewById(R.id.sswitch);
                    TextView socketRowTextView = (TextView)socketListView.getChildAt(i).findViewById(R.id.socket_row_text_view);
                    final String index = socketRowTextView.getText().toString();
                    sswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            Log.d(TAG, "isChedcked:" + isChecked);
                            JSONObject data = new JSONObject();
                            try {
                                data.put("id", currentDevice);
                                data.put("index", index);
                                data.put("state", isChecked);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            MqttMessage message = new MqttMessage();
                            message.setPayload(data.toString().getBytes());
                            publishMessage(switchTopic, message);
                        }
                    });
                }
            }
        });

        // click listener for deviceListSpinner
        deviceListSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (!(item.toString() == getString(R.string.select_morsocket_placeholder))) {
                    currentDevice = item.toString();
                    ArrayList<String> sl = new ArrayList<String>(deviceLinkedHashMap.get(currentDevice));
                    socketList.clear();
                    for(int i = 0; i < sl.size(); i++)
                        socketList.add(sl.get(i));
                    Log.d(TAG, socketList.toString());
                    socketListAdapter.notifyDataSetChanged();
                }
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
                    subscribeToTopic(deviceInfoTopic);
                    subscribeToTopic(devicesInfoTopic);
                    MqttMessage message = new MqttMessage();
                    message.setPayload("synchronize".getBytes());
                    publishMessage(syncDeviceInfoTopic, message);
                } else {
                    //addToHistory("Connected to: " + serverURI);
                    Log.d(TAG, "Connected to : " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, cause.toString());
                Log.d(TAG, "The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, "Incoming message: " + new String(message.getPayload()));

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

        });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setConnectionTimeout(10);
        mqttConnectOptions.setKeepAliveInterval(3);
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
                    subscribeToTopic(deviceInfoTopic);
                    subscribeToTopic(devicesInfoTopic);
                    MqttMessage message = new MqttMessage();
                    message.setPayload("synchronize".getBytes());
                    publishMessage(syncDeviceInfoTopic, message);

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
    public void subscribeToTopic(String subscriptionTopic){
        try {
            mqttClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "subscribe success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "subscribe failed");
                }
            });
            mqttClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, "Message: " + topic + " : " + new String(message.getPayload()));
                    if(topic.equals(deviceInfoTopic)) {
                        parseDeviceInfo(message);
                    }
                    else if(topic.equals(devicesInfoTopic)) {
                        parseDevicesInfo(message);
                    }
                }
            });

        } catch (MqttException ex){
            Log.d(TAG, "Exception whilst subscribing");
            ex.printStackTrace();
        }
    }
    private void parseDeviceInfo(MqttMessage message) throws Exception{
        String jsonString = new String(message.getPayload());
        JSONObject jsonObj = new JSONObject(jsonString);
        Log.d(TAG, jsonString);
        final String device = jsonObj.getString("id");
        final ArrayList<String> listData = new ArrayList<String>();
        JSONArray sockets = jsonObj.getJSONArray("sockets");
        if (sockets != null) {
            for (int i = 0; i < sockets.length(); i++)
                listData.add(sockets.getString(i));
        }
        deviceLinkedHashMap.put(device, listData);
        if (!deviceList.contains(device)) {
            deviceList.add(device);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    deviceListAdapter.notifyDataSetChanged();
                }
            });
        }
        if(device.equals(currentDevice)){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    socketList.clear();
                    for(int i = 0; i < listData.size(); i++)
                        socketList.add(listData.get(i));
                    Log.d(TAG, "socketList" + socketList.toString());
                    socketListAdapter.notifyDataSetChanged();

                    Spinner appliancesListSpinner = (Spinner) findViewById(R.id.appliance_list_spinner);
                    if(appliancesListSpinner != null) {
                        applianceList = Arrays.asList(getResources().getStringArray(R.array.appliances));
                        applianceListAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.appliance_row_view, R.id.appliance_row_text_view, applianceList);
                        appliancesListSpinner.setAdapter(applianceListAdapter);
                    }

                    //click listener for appliancesListSpinner
                    /*appliancesListSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        });*/
                }
            });
        }
    }
    private void parseDeviceInfo(JSONObject jsonObj) throws Exception{
        String device = jsonObj.getString("id");
        ArrayList<String> listData = new ArrayList<String>();
        JSONArray sockets = jsonObj.getJSONArray("sockets");
        if (sockets != null) {
            for (int i = 0; i < sockets.length(); i++)
                listData.add(sockets.getString(i));
        }
        deviceLinkedHashMap.put(device, listData);
        if (!deviceList.contains(device)) {
            deviceList.add(device);
        }
    }
    private void parseDevicesInfo(MqttMessage message) throws Exception{
        String jsonString = new String(message.getPayload());
        JSONObject jsonObj = new JSONObject(jsonString);
        Log.d(TAG, jsonString);
        JSONArray devices = jsonObj.getJSONArray("devices");
        for(int i = 0; i < devices.length(); i++){
            JSONObject deviceObj = devices.getJSONObject(i);
            parseDeviceInfo(deviceObj);
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                deviceListAdapter.notifyDataSetChanged();
            }
        });
    }
    public void publishMessage(String publishTopic, MqttMessage message){
        try {
            mqttClient.publish(publishTopic, message);
            Log.d(TAG, "Message Published");
            if(!mqttClient.isConnected()){
                Log.d(TAG, mqttClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            Log.d(TAG, "Error Publishing: " + e.getMessage());
            e.printStackTrace();
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
        // synchronize devices information.
        if(mqttClient.isConnected()) {
            MqttMessage message = new MqttMessage();
            message.setPayload("synchronize".getBytes());
            publishMessage(syncDeviceInfoTopic, message);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
