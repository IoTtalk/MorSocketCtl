package tw.edu.nctu.pcslab.socketctl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SetupActivity extends AppCompatActivity {

    private final String deviceName = "MorSensor";
    private BluetoothManager btm;
    private BluetoothAdapter bta;
    private BluetoothLeScanner bts;
    private ArrayList<String> foundDevices;
    private ArrayAdapter<String> devicesListAdapter;

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
        bts = bta.getBluetoothLeScanner();

        // check if mobile phone support BLE
        if(bta == null){
            Toast.makeText(getBaseContext(), R.string.no_sup_ble, Toast.LENGTH_SHORT).show();
            finish();
        }
        ListView bleListView = (ListView) findViewById(R.id.ble_list_view);
        foundDevices = new ArrayList<String>();
        devicesListAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.ble_row_view, R.id.ble_row_checked_text_view, foundDevices);
        bleListView.setAdapter(devicesListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // open mobile phone directly, do not ask
        if(!bta.isEnabled()){
            bta.enable();
        }
        scanSmartSocket(true);
    }

    private void scanSmartSocket(boolean startScan){
        if(startScan){
            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
            ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
            ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
            ScanFilter filter = filterBuilder.setDeviceName(deviceName).build();
            filters.add(filter);
            bts.startScan(filters, settingsBuilder.build(), startScanCallback);
        }
        else{
            bts.stopScan(stopScanCallback);
        }
    }
    private ScanCallback startScanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, ScanResult scanResult) {
            BluetoothDevice device = scanResult.getDevice();
            if(foundDevices.contains(device.getAddress()))
                return;
            foundDevices.add(device.getAddress());
            String deviceInfo = device.getName() + " - " + device.getAddress();
            /*ScanRecord scanRecord = scanResult.getScanRecord();
            List<ParcelUuid> uuids = scanRecord.getServiceUuids();
            if(uuids != null) {
                for(int i = 0; i < uuids.size(); i++) {
                    deviceInfo += "\n" + uuids.get(i).toString();
                }
            }*/
            final String info = deviceInfo;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("ble_device", info);
                    devicesListAdapter.add(info);
                    devicesListAdapter.notifyDataSetChanged();
                }
            });
        }
        @Override
        public void onScanFailed(int i) {
            Toast.makeText(getBaseContext(), R.string.scan_failed, Toast.LENGTH_SHORT).show();
            finish();
        }
    };
    private ScanCallback stopScanCallback = new ScanCallback(){

    };

    @Override
    protected void onPause(){
        super.onPause();
        scanSmartSocket(false);
    }

}
