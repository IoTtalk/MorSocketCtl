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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
    private String urlString = "http://192.168.1.232:8899";
    private String deviceListAPI = "device_list";
    private String socketListAPI = "socket_list";


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
                if(!(item.toString() == getString(R.string.select_morsocket_placeholder)))
                    currentDevice = item.toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

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

        if(currentDevice != null) {
            //update device list
            getDeviceListHander.post(new Runnable() {
                @Override
                public void run() {
                    getDeviceList();
                    getDeviceListHander.postDelayed(this, 1000);
                }
            });
            //update socket list
            getSocketListHander.post(new Runnable() {
                @Override
                public void run() {
                    getSocketList();
                    getSocketListHander.postDelayed(this, 1000);
                }
            });
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, "pause");
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
            deviceList.clear();
            for(int i = 0; i < devices.length(); i++) {
                String device = devices.getString(i);
                deviceList.add(device);
            }
            new Handler(Looper.getMainLooper()).post(new Runnable () {
                @Override
                public void run () {
                    deviceListAdapter.notifyDataSetChanged();
                }
            });
            if(currentDevice != null && !deviceList.contains(currentDevice)){
                Toast.makeText(getBaseContext(), R.string.device_disconnected, Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, jsonObj.get("devices").toString());
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
            //loop to get all json objects from devices json array
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
