package tw.edu.nctu.pcslab.socketctl;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tw.org.cic.dataManage.DataTransform;
import tw.org.cic.dataManage.MorSensorParameter;
import tw.org.cic.protocol.BluetoothLeService;
import tw.org.cic.protocol.SampleGattAttributes;

public class SetupActivity extends AppCompatActivity {

    private final String bleDeviceName = "MorSensor";
    private final String TAG = "SetupActivity";

    /* scan BLE devices */
    private BluetoothManager btm;
    private BluetoothAdapter bta;
    private BluetoothLeScanner bts;
    private ArrayList<String> foundDevices;
    private ArrayAdapter<String> bleListAdapter;
    private ArrayList<Integer> bleCheckedList;
    private int currentSetupIndex;

    /* connect BLE devices*/
    private BluetoothLeService bleService;
    private Intent gattServiceIntent;
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;

    /* setup handler */
    private Handler setupHandler;
    private HandlerThread setupThread;
    private int currentStep;

    /* scan WIFI */
    WifiManager wfm;
    private ArrayList<String> foundSSIDs;
    private ArrayAdapter<String> ssidListAdapter;


    /* WIFI information*/
    private String ssid;
    private String []ip;
    private String port;
    private String channel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // check if mobile phone support BLE device
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(getBaseContext(), R.string.no_sup_ble, Toast.LENGTH_SHORT).show();
            finish();
        }

        btm = (BluetoothManager)this.getSystemService(BLUETOOTH_SERVICE);
        bta = btm.getAdapter();
        if (!bta.isEnabled()) {
            int REQUEST_ENABLE_BT = 2;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            // check if mobile phone support BLE
            if (bta == null) {
                Toast.makeText(getBaseContext(), R.string.no_sup_ble, Toast.LENGTH_SHORT).show();
                finish();
            }
            else{
                bts = bta.getBluetoothLeScanner();
            }
        }
        ListView bleListView = (ListView) findViewById(R.id.ble_list_view);
        foundDevices = new ArrayList<String>();
        bleListAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.ble_row_view, R.id.socket_row_text_view, foundDevices);
        bleCheckedList = new ArrayList<Integer>();
        bleListView.setAdapter(bleListAdapter);

        // click listener for bleListView item
        bleListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                CheckedTextView chkItem = (CheckedTextView) v.findViewById(R.id.socket_row_text_view);
                chkItem.setChecked(!chkItem.isChecked());
                Button setupBtn = (Button) findViewById(R.id.setup_btn);
                if(!setupBtn.isEnabled()){
                    if(chkItem.isChecked())
                        chkItem.setChecked(false);
                    else
                        chkItem.setChecked(true);
                    return;
                }
                // add checked position to bleCheckedList
                if(chkItem.isChecked() && (!bleCheckedList.contains(position)))
                    bleCheckedList.add(position);
                // remove unchecked position from bleCheckedList
                else if((!chkItem.isChecked()) && bleCheckedList.contains(position))
                    bleCheckedList.remove(Integer.valueOf(position));
            }
        });

        setupThread = new HandlerThread("setupThread");
        setupThread.start();
        setupHandler = new Handler(setupThread.getLooper());

        // apListSpinner adapter bind
        wfm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if(!wfm.isWifiEnabled())
            wfm.setWifiEnabled(true);
        Spinner apListSpinner = (Spinner) findViewById(R.id.ap_list_spinner);
        foundSSIDs = new ArrayList<String>();
        ssidListAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.wifi_row_view, R.id.wifi_row_text_view, foundSSIDs);
        apListSpinner.setAdapter(ssidListAdapter);

        // click listener for apListSpinner
        apListSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                ssid = item.toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // click listener for setupBtn.
        final Button setupBtn = (Button) findViewById(R.id.setup_btn);
        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gattServiceIntent != null) {
                    stopService(gattServiceIntent);
                    SetupActivity.this.unbindService(bleServiceConnection);
                }
                EditText ap_pwd_editText = (EditText) findViewById(R.id.ap_pwd_editText);

                if(bleCheckedList.isEmpty()) {
                    Toast.makeText(getBaseContext(), R.string.not_select_morsockets, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ssid == null){
                    Toast.makeText(getBaseContext(), R.string.not_select_ap, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ap_pwd_editText.getText().toString().equals("")){
                    Toast.makeText(getBaseContext(), R.string.not_enter_ap_pwd, Toast.LENGTH_SHORT).show();
                    return;
                }
                currentStep = 0;
                gattServiceIntent = new Intent(SetupActivity.this, BluetoothLeService.class);
                SetupActivity.this.bindService(gattServiceIntent, bleServiceConnection, BIND_AUTO_CREATE);
                setupButtonToggle(false);
            }
        });

        ip = new String[4];
        ip[0] = "192";
        ip[1] = "168";
        ip[2] = "1";
        ip[3] = "232";
        port = "7654";
        channel = "00";

        ssid = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final  AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app need location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                });
                builder.show();
            }
        }

    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == android.app.Activity.RESULT_OK) {
            bts = bta.getBluetoothLeScanner();
        }
        else{
            finish();
        }
    }
    private void setupButtonToggle(Boolean enable){
        Button setupBtn = (Button) findViewById(R.id.setup_btn);
        ViewGroup bleListView = (ViewGroup) findViewById(R.id.ble_list_view);
        if(!enable){
            for(int index=0; index< bleListView.getChildCount(); ++index) {
                View nextChild = bleListView.getChildAt(index);
                nextChild.findViewById(R.id.setup_status).setVisibility(View.INVISIBLE);
            }
            setupBtn.setAlpha(.5f);
            setupBtn.setEnabled(false);
        }
        else{
            setupBtn.setAlpha(1.0f);
            setupBtn.setEnabled(true);
        }
    }
    private final ServiceConnection bleServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bleService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bleService.initialize()) {
                Log.d(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            bleService.connect(foundDevices.get(bleCheckedList.get(0)));
            currentSetupIndex = bleCheckedList.get(0);
            Log.d(TAG, "connect: "+foundDevices.get(bleCheckedList.get(0)));
            Log.d(TAG, "mBluetoothLeService:"+bleService);
            //Log.d(TAG, "mConnected:"+mConnected+" mDeviceAddress:"+mDeviceAddress+" mBluetoothLeService:"+mBluetoothLeService);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bleService = null;
            Log.d(TAG, "disconnect");
        }
    };

    /*cic code*/
    public static short[] MorSensorID = new short[10];
    private static short connectCount = 0;
    static boolean OverContentSize = false;
    static String note = "";
    private void Decode(byte[] values){
        byte[] RawCommand = new byte[20];
        switch ((short) values[0]) {
            case MorSensorParameter.IN_SENSOR_LIST:
                note = "i0x02:IN_SENSOR_LIST "+ connectCount;
                MorSensorID = new short[values[1]];
                for (int i = 0; i < MorSensorID.length; i++)
                    MorSensorID[i] = values[i + 2];//[discover][number][ID][ID][ID]....

                switch (connectCount){
                    case 0:
                        MorSensorParameter.sensorlist[0] = MorSensorParameter.searchSensor(MorSensorID);
                        break;
                    case 1:
                        MorSensorParameter.sensorlist[1] = MorSensorParameter.searchSensor(MorSensorID);
                        break;
                    case 2:
                        MorSensorParameter.sensorlist[2] = MorSensorParameter.searchSensor(MorSensorID);
                        break;
                    default:
                        connectCount = 0;
                        break;
                }
                if(MorSensorParameter.searchSensor(MorSensorID) == MorSensorParameter.ColorID) {
                    RawCommand[0] = (byte)0x31;
                    RawCommand[1] = (byte)0x03;
                    RawCommand[2] = (byte)0x01;
                    sendCommand(RawCommand);
                }else if(MorSensorParameter.searchSensor(MorSensorID) == MorSensorParameter.SpO2ID) {
                    RawCommand[0] = (byte)0x23;
                    RawCommand[1] = (byte)0xA0;
                    RawCommand[2] = (byte)0x00;
                    RawCommand[3] = (byte)0x22;
                    RawCommand[4] = (byte)0x03;
                    RawCommand[5] = (byte)0x01;
                    RawCommand[6] = (byte)0x09;
                    RawCommand[7] = (byte)0x09;
                    sendCommand(RawCommand);
                }
                Log.i(TAG, note);
                break;
            case MorSensorParameter.IN_SET_AP_SSID:
                note = "i0x41:IN_SET_AP_SSID";
                if(OverContentSize){
                    OverContentSize = false;
                    RawCommand = DataTransform.checkLengthASCII(2, ssid, (byte)0x41);
                    sendCommand(RawCommand);
                }
                Log.i(TAG, note);
                break;
            case MorSensorParameter.IN_SET_AP_PASSWORD:
                note = "i0x42:IN_SET_AP_PASSWORD";
                if(OverContentSize){
                    OverContentSize = false;
                    EditText apPwd = (EditText) findViewById(R.id.ap_pwd_editText);
                    String pwd = apPwd.getText().toString();
                    RawCommand = DataTransform.checkLengthASCII(2, pwd, (byte)0x42);
                    sendCommand(RawCommand);
                }
                Log.i(TAG, note);
                break;
            case MorSensorParameter.IN_SET_WIFI_SERVER_IP:
                note = "i0x43:IN_SET_WIFI_SERVER_IP";
                Log.i(TAG, note);
                break;
            case MorSensorParameter.IN_SET_TCP_CONNECTION:
                note = "i0x44:IN_SET_TCP_CONNECTION";
                //checkConnect = true;
                Log.i(TAG, note);
                break;
//            case MorSensorParameter.IN_BLE_SENSOR_DATA:
//                RawCommand[0] = MorSensorParameter.OUT_BLE_SENSOR_DATA;
//                RawCommand[1] = MorSensorParameter.IMUID;
//                SendCommand(RawCommand);
//                break;
        }
    }
    private boolean sendCommand(byte[] rawCommand){
        writeCharacteristic.setValue(rawCommand);
        return bleService.writeCharacteristic(writeCharacteristic);
    }

    public void sendSetupCommand(int step) {
        boolean res;
        int iterationLimit = 100, iteration = 0;
        // Five setup step.
        byte[] rawCommand = new byte[20];
        switch (step){
            case 0: //0x02_List
                rawCommand[0] = MorSensorParameter.OUT_SENSOR_LIST;
                break;
            case 1: //0x41_SSID
                if(ssid.length() > 18)
                    OverContentSize = true;
                rawCommand = DataTransform.checkLengthASCII(1, ssid, (byte)MorSensorParameter.OUT_SET_AP_SSID);
                Log.d(TAG, ssid);
                //Log.d(TAG, "btnWiFi_0x41 SSID: "+rawCommand[0]+" "+rawCommand[1]+" "+rawCommand[2]+" "+rawCommand[3]);
                break;
            case 2: //0x42_PWD
                EditText apPwd = (EditText) findViewById(R.id.ap_pwd_editText);
                String pwd = apPwd.getText().toString();
                if(pwd.length() > 18)
                    OverContentSize = true;
                rawCommand = DataTransform.checkLengthASCII(1, pwd, (byte)MorSensorParameter.OUT_SET_AP_PASSWORD);
                //Log.d(TAG, "btnWiFi_0x42 PWD: "+rawCommand[0]+" "+rawCommand[1]+" "+rawCommand[2]+" "+rawCommand[3]);
                break;
            case 3: //0x43_IP
                rawCommand[0] = MorSensorParameter.OUT_SET_WIFI_SERVER_IP;
                rawCommand[1] = (byte)(Short.parseShort(ip[0]));
                rawCommand[2] = (byte)(Short.parseShort(ip[1]));
                rawCommand[3] = (byte)(Short.parseShort(ip[2]));
                rawCommand[4] = (byte)(Short.parseShort(ip[3]));
                rawCommand[5] = (byte)(Short.parseShort(port) >> 8 & 0xFF);
                rawCommand[6] = (byte)(Short.parseShort(port) & 0xFF);
                rawCommand[7] = (byte)(Short.parseShort(channel));
                //Log.d(TAG, "btnWiFi_0x43 "+rawCommand[0]+" "+rawCommand[1]+" "+rawCommand[2]+" "+rawCommand[3] + " "+rawCommand[4]+"  " +rawCommand[5]+" "+rawCommand[6]+" "+ rawCommand[7]);
                break;
            case 4: //0x44_OC
                rawCommand[0] = MorSensorParameter.OUT_SET_TCP_CONNECTION;
                rawCommand[1] = MorSensorParameter.OUT_OUT_SET_OPEN;
                rawCommand[2] = (byte)(Short.parseShort(channel));
                //Log.d(TAG, "btnWiFi_0x44");
                break;
        }
        do{
            res = sendCommand(rawCommand);
            iteration++;
        }while(!res && iteration < iterationLimit);
        if(!res){
            setupButtonToggle(true);
        }
        Log.d(TAG, "SendCommand" + step + ":" + res + " "+ rawCommand[0]);
    }
    private void showSetupStatus(String message, boolean success) {
        ViewGroup bleListView = (ViewGroup)findViewById(R.id.ble_list_view);
        bleListView.getChildAt(currentSetupIndex).findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        TextView setupStatus = (TextView) bleListView.getChildAt(currentSetupIndex).findViewById(R.id.setup_status);
        setupStatus.setVisibility(View.VISIBLE);
        setupStatus.setText(message);
        setupStatus.setTextColor(ContextCompat.getColor(getBaseContext(), (success) ? R.color.colorSuccess : R.color.colorFailed));
    }
    // BLE BroadcastReceiver;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                invalidateOptionsMenu();
                Log.d(TAG, "ACTION_GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                invalidateOptionsMenu();
                setupButtonToggle(true);
                Log.d(TAG, "ACTION_GATT_DISCONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(bleService.getSupportedGattServices());
                Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                ViewGroup bleListView = (ViewGroup)findViewById(R.id.ble_list_view);
                for(Integer i : bleCheckedList){
                    ProgressBar pb = (ProgressBar) bleListView.getChildAt(i).findViewById(R.id.progressBar);
                    pb.setVisibility(View.VISIBLE);
                }
                // Start setup step1.
                setupHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendSetupCommand(currentStep);
                    }
                },100);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String mDeviceData = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d(TAG, mDeviceData);
                // check correctness
                switch(currentStep){
                    case 0: //0x02_List
                        if(!mDeviceData.startsWith("02")) {
                            Log.d(TAG, "Step0 error!");
                            showSetupStatus(getString(R.string.unknown_error), false);
                            setupButtonToggle(true);
                            return;
                        }
                        break;
                    case 1: //0x41_SSID
                        if(!mDeviceData.startsWith("41")) {
                            Log.d(TAG, "Step1 error!");
                            showSetupStatus(getString(R.string.unknown_error), false);
                            setupButtonToggle(true);
                            return;
                        }
                        break;
                    case 2: //0x42_PWD
                        if(!mDeviceData.startsWith("42")) {
                            Log.d(TAG, "Step2 error!");
                            showSetupStatus(getString(R.string.unknown_error), false);
                            setupButtonToggle(true);
                            return;
                        }
                        break;
                    case 3: //0x43_IP
                        if(!mDeviceData.startsWith("43")) {
                            Log.d(TAG, "Step3 error!");
                            showSetupStatus(getString(R.string.incorrect_ap_pwd), false);
                            setupButtonToggle(true);
                            return;
                        }
                        break;
                    case 4: //0x44_OC
                        if(!mDeviceData.startsWith("44")) {
                            Log.d(TAG, "Step4 error!");
                            showSetupStatus(getString(R.string.server_error), false);
                            setupButtonToggle(true);
                            return;
                        }
                        else{
                            Log.d(TAG, "Setup success!");
                            showSetupStatus(getString(R.string.setup_success), true);
                        }
                        setupButtonToggle(true);
                        return;
                }
                setupHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendSetupCommand(++currentStep);
                    }
                },100);
                //items.add(mDeviceData);
                //listView.setAdapter(adapter);
                Decode(DataTransform.hexToBytes(mDeviceData));
                //Log.e(TAG, "ACTION_DATA_AVAILABLE:" + mDeviceData);

            }
        }
    };

    // WIFI BroadcastReceiver
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            List<android.net.wifi.ScanResult> res = wfm.getScanResults();

            Log.d(TAG,res.toString());
            for(ScanResult s : res){
                if(!foundSSIDs.contains(s.SSID)) {
                    foundSSIDs.add(s.SSID);
                    ssidListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

        /*public void BtDisConnect(){
        if(mConnected)
        {
            mConnected = false;
            mDeviceAddress="123";
            mBluetoothLeService.disconnect();

            onBackPressed();
            Log.i(TAG, "BluetoothLe Disconnected.");
        }
    }*/
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String LIST_NAME = "NAME";
        String LIST_UUID = "UUID";
        String uuid;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        ArrayList<ArrayList<BluetoothGattCharacteristic>>  mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
            {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

                // Read
                if(gattCharacteristic.getUuid().toString().contains("00002a37-0000-1000-8000-00805f9b34fb"))
                {
                    final int charaProp = gattCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        readCharacteristic = null;
                        readCharacteristic = gattCharacteristic;
                        bleService.setCharacteristicNotification(
                                gattCharacteristic, true);
                    }
                }

                // SendCommands
                if(gattCharacteristic.getUuid().toString().contains("00001525-1212-efde-1523-785feabcd123"))
                {
                    final int charaProp = gattCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        if (writeCharacteristic != null) {
                            writeCharacteristic = null;
                        }
                        writeCharacteristic = gattCharacteristic;
                        Log.d(TAG,"writeCharacteristic");
                    }
                }
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // open bluetooth directly, do not ask
        if(!bta.isEnabled()){
            bta.enable();
        }
        scanMorSocket(true);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        wfm.startScan();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    private void scanMorSocket(boolean startScan){
        if(startScan){
            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
            ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
            ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
            ScanFilter filter = filterBuilder.setDeviceName(bleDeviceName).build();
            filters.add(filter);
            if(bts != null && bta.isEnabled())
                bts.startScan(filters, settingsBuilder.build(), scanCallback);
        }
        else{
            if(bts != null && bta.isEnabled())
                bts.stopScan(scanCallback);
        }
    }
    private ScanCallback scanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult scanResult) {
            BluetoothDevice device = scanResult.getDevice();
            if(foundDevices.contains(device.getAddress()))
                return;
            foundDevices.add(device.getAddress());
            String deviceInfo = device.getName() + "\n" + device.getAddress();
            final String info = deviceInfo;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, info);
                    bleListAdapter.notifyDataSetChanged();
                }
            });
        }
        @Override
        public void onScanFailed(int i) {
            Toast.makeText(getBaseContext(), R.string.scan_failed, Toast.LENGTH_SHORT).show();
            finish();
        }
    };


    @Override
    protected void onPause(){
        super.onPause();
        scanMorSocket(false);
        unregisterReceiver(mGattUpdateReceiver);
        unregisterReceiver(wifiReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(gattServiceIntent != null)
            unbindService(bleServiceConnection);
        bleService = null;
        setupThread.quit();
    }
}
