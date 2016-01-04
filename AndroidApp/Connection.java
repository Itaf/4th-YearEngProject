import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class Connection extends AppCompatActivity{
    /* Constants */
    private static final int REQUEST_ENABLE_BT = 1;

    /* Global Variables */
    private Button button;
    private int n = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean connected = false;
    private boolean enabled = false;
    private SparseArray<BluetoothDevice> mDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        addListenerOnButton3();
        addListenerOnButton4();
        addListenerOnButton5();

        // Determine whether BLE is supported on the device.
        // Then, selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        setBLE();
        mDevices = new SparseArray<BluetoothDevice>();
    }

    /* Button Listeners */

    //Start Scan
    public void addListenerOnButton3()
    {
        final Context context = this;
        button = (Button) findViewById(R.id.button3);

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                //Scan for devices...
                if(enabled) {
                    Intent intent = new Intent(context, DeviceScanActivity.class);
                    startActivity(intent);
                }
                else
                {
                    enableBLE();
                }
            }
        });
    }

    //Connect
    public void addListenerOnButton4()
    {
        final Context context = this;
        button = (Button) findViewById(R.id.button4);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Connect to the selected device...
                if (connected)
                {
                    findViewById(R.id.button4).setEnabled(false);
                    findViewById(R.id.button5).setEnabled(true);
                }
                else
                {
                    //Retry...
                }
            }
        });
    }

    //Monitor
    public void addListenerOnButton5()
    {
        final Context context = this;
        button = (Button) findViewById(R.id.button5);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, Monitoring.class);
                startActivity(intent);
            }
        });
    }

    /* BLUETOOTH Functions */

    public void setBLE()
    {
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        enableBLE();
    }

    public void enableBLE()
    {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if(mBluetoothAdapter.isEnabled())
        {
            //Do something...
            enabled = true;
        }
    }
}
