package com.example.me4386_07_admin.amixv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Scanner extends AppCompatActivity
{
    /* CONSTANTS */
    public static final String TAG = Scanner.class.getSimpleName();
    private static final long SCAN_PERIOD = 10000;  //Stops scanning after 10 seconds.

    /* GLOBAL VARIABLES */
    private BluetoothAdapter mBluetoothAdapter;
    private TextView mEmptyList;
    private List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    private ServiceConnection onService = null;
    private Map<String, Integer> devRssiValues;
    private Handler mHandler;
    private boolean mScanning;
    private Button btnScanCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        final Context context = Scanner.this;

        setContentView(R.layout.devices_list);
        android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP;
        layoutParams.y = 300;

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null)
        {
            Toast.makeText(context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateList();
        mEmptyList = (TextView) findViewById(R.id.empty);
        scanButtonListener();  //Scan/Cancel button listener
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private void populateList()
    {
        final Context context = Scanner.this;
        /* Initialize device list container */
        Log.d(TAG, "populateList");
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new DeviceAdapter(context, deviceList);
        devRssiValues = new HashMap<String, Integer>();

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable)
    {
        final Button button = (Button) findViewById(R.id.btn_scan);

        if (enable)
        {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    button.setText(R.string.scan);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            button.setText(R.string.cancel);
        }
        else
        {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            button.setText(R.string.scan);
        }

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addDevice(device, rssi);
                        }
                    });
                }
            });
        }
    };

    private void addDevice(BluetoothDevice device, int rssi)
    {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList)
        {
            if (listDev.getAddress().equals(device.getAddress()))
            {
                deviceFound = true;
                break;
            }
        }

        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound)
        {
            deviceList.add(device);
            mEmptyList.setVisibility(View.GONE);
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            BluetoothDevice device = deviceList.get(position);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());

            Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            finish();
        }
    };

    class DeviceAdapter extends BaseAdapter
    {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices)
        {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount()
        {
            return devices.size();
        }

        @Override
        public Object getItem(int position)
        {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewGroup vg;

            if (convertView != null)
            {
                vg = (ViewGroup) convertView;
            }
            else
            {
                vg = (ViewGroup) inflater.inflate(R.layout.device, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);
            byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0)
            {
                tvrssi.setText("Rssi = " + String.valueOf(rssival));
            }

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED)
            {
                Log.i(TAG, "device::"+device.getName());
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setTextColor(Color.GRAY);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText(R.string.paired);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);

            }
            else
            {
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
            }
            return vg;
        }
    }

    private void showMessage(String msg)
    {
        final Context context = Scanner.this;
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void scanButtonListener()
    {
        btnScanCancel = (Button) findViewById(R.id.btn_scan);
        btnScanCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                if (mScanning == false)
                {
                    scanLeDevice(true);
                }
                else
                {
                    finish();
                }
            }
        });
    }
}
