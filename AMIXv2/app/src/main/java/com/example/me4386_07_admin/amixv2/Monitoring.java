package com.example.me4386_07_admin.amixv2;

import java.io.UnsupportedEncodingException;

import com.example.me4386_07_admin.amixv2.BLEService;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Float.parseFloat;

public class Monitoring extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener
{
    /* CONSTANTS */
    public static final String TAG = "AMIX";
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    /* GLOBAL VARIABLES */
    private String filename;
    private int count = 0;
    private float startTime, endTime;

    private RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private BLEService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ArrayAdapter<String> listAdapter;

    private TextView mRemoteRssiVal;
    private ListView messageListView;
    private Button btnConnectDisconnect, btnStart, btnSend;
    private EditText edtMessage;
    private LineChart ppgChart, gsrChart;

    // Set initial UI state
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        filename = bundle.getString("fn");

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null)
        {
            Toast.makeText(Monitoring.this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        service_init();
        initPPG();
        initGSR();

        addListenerOnButton();  //Connect/Disconnect button listener
        addListenerOnButton1(); //Start/Stop button listener
        addListenerOnButton2(); //Send button listener
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled())
        {
            Log.i(TAG, "onResume - Bluetooth not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        final Context context = Monitoring.this;
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try
        {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(UARTStatusChangeReceiver);
        }
        catch (Exception ignore)
        {
            Log.e(TAG, ignore.toString());
        }

        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;
    }

    //UART services connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder rawBinder)
        {
            mService = ((BLEService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);

            if (!mService.initialize())
            {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname)
        {
            //mService.disconnect(mDevice);
            mService = null;
        }
    };

    //Handler of events received from UART services
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {}
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            final Intent mIntent = intent;

            if (action.equals(BLEService.ACTION_GATT_CONNECTED))
            {
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        Log.d(TAG, "UART_CONNECT");
                        btnConnectDisconnect.setText("Disconnect");
                        btnStart.setEnabled(true);
                        btnSend.setEnabled(true);

                        /*String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        edtMessage.setEnabled(true);
                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                        listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);*/   //Old Version

                        mState = UART_PROFILE_CONNECTED;
                    }
                });
            }

            if (action.equals(BLEService.ACTION_GATT_DISCONNECTED))
            {
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        Log.d(TAG, "UART_DISCONNECT");
                        btnConnectDisconnect.setText("Connect");
                        btnStart.setEnabled(false);
                        btnSend.setEnabled(false);

                        /*edtMessage.setEnabled(false);
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());*/  //Old Version

                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();
                    }
                });
            }

            if (action.equals(BLEService.ACTION_GATT_SERVICES_DISCOVERED))
            {
                mService.enableTXNotification();
            }

            if (action.equals(BLEService.ACTION_DATA_AVAILABLE))
            {
                if(btnStart.getText().equals("Stop"))
                {
                    final byte[] txValue = intent.getByteArrayExtra(BLEService.EXTRA_DATA);

                    runOnUiThread(new Runnable() {
                        public void run()
                        {
                            try
                            {
                                //Increment data points counter
                                count++;

                                //Get current time in milliseconds
                                endTime = System.currentTimeMillis();

                                String text = new String(txValue, "UTF-8");
                                String text1, text2, text3, text4;

                                //Get PPG point
                                text1 = text;
                                //Get heart rate
                                /*text2 = text.substring(text.indexOf(text1, 0),
                                        text.indexOf(",", text1.length()));
                                //Get GSR point
                                text3 = text.substring(text.indexOf(text2, text1.length()),
                                        text.indexOf(",", (text1.length() + text2.length())));
                                //Get stress level
                                text4 = text.substring(text.indexOf(text3,
                                        (text1.length() + text2.length())));*/

                                float ppgPoint = parseFloat(text1);
                                addEntries(ppgChart, ppgPoint);

                                /*float gsrPoint = parseFloat(text3);
                                addEntries(gsrChart, gsrPoint);*/

                                /*//if((count % 60) == 0)
                                //{
                                    final TextView rate = (TextView) findViewById(R.id.heartrate);
                                    rate.setText("Heart Rate: " + text2 + " BPM");

                                    final TextView level = (TextView) findViewById(R.id.stresslevel);
                                    level.setText("Stress Level: " + text4 + " %");
                                //}*/

                                /*String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                                listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                                messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);*/   //Old Version
                            }
                            catch (Exception e)
                            {
                                Log.e(TAG, e.toString());
                            }
                        }
                    });
                }
            }

            if (action.equals(BLEService.DEVICE_DOES_NOT_SUPPORT_UART))
            {
                showMessage("UART is not supported ... Disconnecting");
                mService.disconnect();
            }
        }
    };

    private void service_init()
    {
        final Context context = Monitoring.this;
        Intent bindIntent = new Intent(context, BLEService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(context).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BLEService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address ==" + mDevice + "mserviceValue" + mService);
                    //(TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");    //Old Version
                    mService.connect(deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK)
                {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "Bluetooth not enabled");
                    Toast.makeText(this, "Problem in Bluetooth Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {}

    private void showMessage(String msg)
    {
        final Context context = Monitoring.this;
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /*@Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED)
        {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }*/

    /* GRAPHING OF SIGNALS*/

    //Initializes the PPG signal's chart
    public void initPPG()
    {
        LineData ppgData;
        Legend legend;
        XAxis x;
        YAxis y1, y2;

        //Retrieve ppgChart from the XML layout
        ppgChart = (LineChart) findViewById(R.id.ppgChart);

        //Label the chart
        ppgChart.setDescription("");
        ppgChart.setNoDataTextDescription("No data at the moment");

        //Enable/disable custom features
        ppgChart.setHighlightPerTapEnabled(true);
        ppgChart.setTouchEnabled(true);
        ppgChart.setDragEnabled(true);
        ppgChart.setScaleEnabled(true);
        ppgChart.setDrawGridBackground(true);
        ppgChart.setPinchZoom(true);

        //Assign a data set to ppgChart
        ppgData = new LineData();
        ppgChart.setData(ppgData);

        //Add a legend
        legend = ppgChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);

        //Get and initialize the Axes
        x = ppgChart.getXAxis();
        y1 = ppgChart.getAxisLeft();
        y2 = ppgChart.getAxisRight();

        x.setDrawGridLines(true);
        y1.setDrawGridLines(true);
        y2.setEnabled(false);
    }

    //Initializes the GSR signal's chart
    public void initGSR()
    {
        LineData gsrData;
        Legend legend;
        XAxis x;
        YAxis y1, y2;

        //Retrieve gsrChart from the XML layout
        gsrChart = (LineChart) findViewById(R.id.gsrChart);

        //Label the chart
        gsrChart.setDescription("");
        gsrChart.setNoDataTextDescription("No data at the moment");

        //Enable/disable custom features
        gsrChart.setHighlightPerTapEnabled(true);
        gsrChart.setTouchEnabled(true);
        gsrChart.setDragEnabled(true);
        gsrChart.setScaleEnabled(true);
        gsrChart.setDrawGridBackground(true);
        gsrChart.setPinchZoom(true);

        //Assign a data set to gsrChart
        gsrData = new LineData();
        gsrChart.setData(gsrData);

        //Add a legend
        legend = gsrChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);

        //Get and initialize the Axes
        x = gsrChart.getXAxis();
        y1 = gsrChart.getAxisLeft();
        y2 = gsrChart.getAxisRight();

        x.setDrawGridLines(true);
        y1.setDrawGridLines(true);
        y2.setEnabled(false);
    }

    //Adds data entry/point to the specified chart
    public void addEntries(LineChart chart, float point)
    {
        LineData data = chart.getData();
        LineDataSet set;

        if(data != null)
        {
            set = data.getDataSetByIndex(0);

            if(set == null)
            {
                //Create a DataSet
                set = createSet();
                data.addDataSet(set);
            }

            //Add values
            data.addXValue((endTime - startTime) + "ms");
            data.addEntry(new Entry(point, set.getEntryCount()), 0);

            //Notify the chart about the new data
            chart.notifyDataSetChanged();

            //Limit the number of visible entries
            chart.setVisibleXRange(0, 10);

            //Move to the last entry
            chart.moveViewToX(data.getXValCount() - 11);

            //Refresh the chart, used for dynamic data readings
            //chart.invalidate();
        }
    }

    public LineDataSet createSet()
    {
        LineDataSet set = new LineDataSet(null, "");
        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.BLUE);
        set.setLineWidth(2f);
        set.setCircleSize(3f);
        set.setFillColor(Color.BLUE);
        set.setHighLightColor(Color.RED);
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(7f);

        return set;
    }

    /* BUTTON LISTENERS */

    //Disconnect/Connect
    public void addListenerOnButton()
    {
        final Context context = Monitoring.this;
        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);

        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                if (!mBtAdapter.isEnabled())
                {
                    Log.i(TAG, "onClick - Bluetooth not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else
                {
                    if (btnConnectDisconnect.getText().equals("Connect"))
                    {
                        //Connect button pressed, open DeviceListActivity class,
                        // with popup windows that scan for devices...
                        Intent newIntent = new Intent(context, ScanActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    }
                    else
                    {
                        //Disconnect button pressed
                        if (mDevice != null)
                        {
                            mService.disconnect();
                        }
                    }
                }
            }
        });
    }

    //Start
    public void addListenerOnButton1()
    {
        btnStart = (Button) findViewById(R.id.btn_start);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                if(btnStart.getText().equals("Start"))
                {
                    btnStart.setText("Stop");
                    String message;
                    byte[] value;

                    //Get current time in milliseconds (at the start)
                    startTime = System.currentTimeMillis();

                    try
                    {
                        //Send start command to service
                        message = "Start";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        // TODO Auto-generated catch block
                        //e.printStackTrace();
                    }
                }
                else
                {
                    btnStart.setText("Start");
                    String message;
                    byte[] value;

                    try
                    {
                        //Send stop command to service
                        message = "Stop";
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        // TODO Auto-generated catch block
                        //e.printStackTrace();
                    }
                }

                //Testing the charts...
                /*final Random rand = new Random();

                for(int i = 0; i < 200; i++)
                {
                    runOnUiThread(new Runnable() {
                        public void run()
                        {
                            try
                            {
                                addEntries(ppgChart, (float) rand.nextInt(100));
                                addEntries(gsrChart, (float) rand.nextInt(100));
                            }
                            catch (Exception e)
                            {}
                        }
                    });
                    count++;
                }*/
            }
        });
    }

    //Send
    public void addListenerOnButton2()
    {
        final Context context = Monitoring.this;
        btnSend = (Button) findViewById(R.id.btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(context, Emergency.class);

                Bundle bundle = new Bundle();
                bundle.putString("fn", filename);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
    }

   /* public void addListenerOnButton2()
    {
        btnSend = (Button) findViewById(R.id.sendButton);

        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        edtMessage = (EditText) findViewById(R.id.sendText);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                EditText editText = (EditText) findViewById(R.id.sendText);
                String message = editText.getText().toString();
                byte[] value;

                try
                {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);

                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                }
                catch (UnsupportedEncodingException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }*/
}
