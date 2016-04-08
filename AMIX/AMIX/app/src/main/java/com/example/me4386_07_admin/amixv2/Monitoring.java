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
import android.app.NotificationManager;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Integer.parseInt;

public class Monitoring extends AppCompatActivity
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

    private Button btnConnectDisconnect, btnStart, btnShare;
    private FloatingActionButton btnInfo;

    private LineChart ppgChart, gsrChart;
    private TextView rate, level;
    private int count = 0;
    private float [] parameters;

    private float sum1 = 0;
    private float sum2 = 0;
    private float sum3 = 0;
    private float sum4 = 0;
    private float sum5 = 0;
    private float sum6 = 0;
    private float sum7 = 0;
    private float sum8 = 0;
    private float sum9 = 0;

    private float RR = 0;
    private float RR1 = 0;
    private float RR2 = 0;

    private float temp = 0;
    private float temp1 = 0;
    private float temp2 = 0;

    private int mState = UART_PROFILE_DISCONNECTED;
    private BLEService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;

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

        parameters = new float[11];

        rate = (TextView) findViewById(R.id.heartrate);
        rate.setText("Heart Rate: -");

        level = (TextView) findViewById(R.id.stresslevel);
        level.setText("Stress Level: -");

        connectButtonListener();  //Connect/Disconnect button listener
        startButtonListener();  //Start/Stop button listener
        shareButtonListener(); //Share button listener
        infoButtonListener(); //Info button listener

        service_init();
        initPPG();
        initGSR();
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

                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                    }
                });
            }

            if (action.equals(BLEService.ACTION_GATT_SERVICES_DISCOVERED))
            {
                mService.enableTXNotification();
            }

            if (action.equals(BLEService.ACTION_DATA_AVAILABLE))
            {
                if(btnStart.getText().toString().equals("Stop"))
                {
                    final byte[] txValue = intent.getByteArrayExtra(BLEService.EXTRA_DATA);

                    runOnUiThread(new Runnable() {
                        public void run()
                        {
                            try
                            {
                                if(txValue != null)
                                {
                                    String text = new String(txValue, "UTF-8");
                                    String text1, text2, text3;

                                    //Increment data points counter
                                    count++;

                                    //Get PPG and GSR points...
                                    text1 = text.substring(1, text.indexOf("G"));

                                    int ppgPoint = parseInt(text1);
                                    addEntries(ppgChart, ppgPoint);

                                    text2 = text.substring(text.indexOf("G")+1, text.indexOf("H"));
                                    int gsrPoint = parseInt(text2);
                                    addEntries(gsrChart, gsrPoint);

                                    text3 = text.substring(text.indexOf("H")+1, text.length());

                                    int heartRate = parseInt(text3);
                                    rate.setText("Heart Rate: " + heartRate + " BPM");
                                    parameters[0] = heartRate;

                                    checkHR(heartRate);
                                    calculateParameters((int) parameters[0], gsrPoint);
                                }
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

    public void checkHR(int hr)
    {
        final Context context = Monitoring.this;
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(hr > 100 || hr < 50)
        {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.heartratebeat)
                            .setContentTitle("AMIX")
                            .setContentText("Irregular Heart Rate!");

            // An ID allows you to update the notification later on (if needed)...
            mNotificationManager.notify(1, mBuilder.build());
        }
        else
        {
            mNotificationManager.cancelAll();
        }
    }

    public void calculateParameters(int hr, int gsr)
    {
        float avgHR, avgGSR, avgRR, devHR, SDSD, RMSSD, CV, devGSR, kurtosisGSR, skewnessGSR;

        //Average
        sum1 += hr;
        avgHR = sum1/count;
        parameters[1] = avgHR;

        //Standard Deviation
        sum2 += Math.pow((hr - avgHR), 2);
        devHR = (float) Math.sqrt(sum2/(count-1));
        parameters[2] = devHR;

        //CV (Coefficient of Variation) - the ratio of the standard deviation to the mean
        CV = devHR/avgHR;
        parameters[3] = CV;

        if(count < 2)
        {
            RR = hr;
        }
        else if(count >= 2 && count%2 == 0)
        {
            RR1 = hr - RR;
            RR = hr;
        }
        else
        {
            RR2 = hr - RR;
            RR = hr;

            sum3 += RR2 - RR1;
            avgRR = sum3/(count - 1);

            //SDSD (Standard Deviation of Successive Differences) - similar to standard deviation
            sum4 += Math.pow(((RR2 - RR1) - avgRR), 2);
            SDSD = (float) Math.sqrt(sum4/(count - 1));
            parameters[4] = SDSD;

            //RMSSD (Root mean square of successive differences)...
            sum5 += Math.pow((RR2 - RR1), 2);
            RMSSD = (float) Math.sqrt(sum5/(count - 1));
            parameters[5] = RMSSD;
        }

        //GSR Parameters...
        sum6 += gsr;
        avgGSR = sum6/count;
        parameters[6] = avgGSR;

        //Standard Deviation
        sum7 += Math.pow((gsr - avgGSR), 2);
        devGSR = (float) Math.sqrt(sum7/(count-1));
        parameters[7] = devGSR;
        if(count % 10 == 0)
        {
            temp2 = parameters[7];
        }

        if(devGSR > 0)
        {
            //Kurtosis
            sum8 += Math.pow(((gsr - avgGSR) / devGSR), 4);
            kurtosisGSR = (sum8 / count) - 3;
            parameters[8] = kurtosisGSR;
            if(count % 10 == 0)
            {
                temp1 = parameters[8];
            }

            //Skewness
            sum9 += Math.pow(((gsr - avgGSR) / devGSR), 3);
            skewnessGSR = sum9 / count;
            parameters[9] = skewnessGSR;
            if(count % 10 == 0)
            {
                temp2 = parameters[9];
            }
        }

        if(temp == 0 && temp1 == 0 && temp2 == 0)
        {
            level.setText("Checking...");
        }

        if(((parameters[7] - temp) <= 1 && (parameters[7] - temp) >= 0) ||
                ((parameters[8] - temp1) <= 1 && (parameters[8] - temp1) >= 0) ||
                ((parameters[9] - temp2) <= 1 && (parameters[9] - temp2) >= 0))
        {
            level.setText("Normal!");
        }

        if(((parameters[7] - temp) >= 2.5 && (parameters[7] - temp) < 5) ||
                ((parameters[8] - temp1) >= 2.5 && (parameters[8] - temp1) < 5) ||
                ((parameters[9] - temp2) >= 2.5 && (parameters[9] - temp2) < 5))
        {
            level.setText("Aroused!");
        }

        if(((parameters[7] - temp) >= 5) || ((parameters[8] - temp1) >= 5) || ((parameters[9] - temp2) >= 5))
        {
            level.setText("Stressed!");
        }

        if(((parameters[7] - temp) < 0) || ((parameters[8] - temp1) < 0) || ((parameters[9] - temp2) < 0))
        {
            level.setText("Relaxing!");
        }
    }

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
        ppgChart.setDrawGridBackground(true);
        ppgChart.setPinchZoom(true);

        //Assign a data set to ppgChart
        ppgData = new LineData();
        ppgChart.setData(ppgData);

        //Remove legend
        ppgChart.getLegend().setEnabled(false);

        //Get and initialize the Axes
        x = ppgChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        y1 = ppgChart.getAxisLeft();
        y2 = ppgChart.getAxisRight();

        x.setDrawGridLines(true);
        y1.setDrawGridLines(true);
        y1.setStartAtZero(false);
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
        gsrChart.setAutoScaleMinMaxEnabled(false);
        gsrChart.setDrawGridBackground(true);
        gsrChart.setPinchZoom(true);

        //Assign a data set to gsrChart
        gsrData = new LineData();
        gsrChart.setData(gsrData);

        //Remove legend
        gsrChart.getLegend().setEnabled(false);

        //Get and initialize the Axes
        x = gsrChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        y1 = gsrChart.getAxisLeft();
        y2 = gsrChart.getAxisRight();

        x.setDrawGridLines(true);
        y1.setDrawGridLines(true);
        y1.setStartAtZero(false);
        y2.setEnabled(false);
    }

    //Adds data entry/point to the specified chart
    public void addEntries(LineChart chart, int point)
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
            data.addXValue("");
            data.addEntry(new Entry(point, set.getEntryCount()), 0);

            //Notify the chart about the new data
            chart.notifyDataSetChanged();

            //Limit the number of visible entries
            chart.setVisibleXRange(0,  70);

            //Move to the last entry
            chart.moveViewToX(data.getXValCount() - 71);

            //Refresh the chart, used for dynamic data readings
            chart.invalidate();
        }
    }

    public LineDataSet createSet()
    {
        LineDataSet set = new LineDataSet(null, "");
        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.RED);
        set.setLineWidth(1f);
        set.setCircleSize(0f);
        set.setFillColor(Color.RED);
        set.setHighLightColor(Color.RED);
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(0f);

        return set;
    }

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
                //When the Scanner return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null)
                {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultDevice.address ==" + mDevice + "mServiceValue" + mService);
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

    private void showMessage(String msg)
    {
        final Context context = Monitoring.this;
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /* BUTTON LISTENERS */

    //Disconnect/Connect
    public void connectButtonListener()
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
                    if (btnConnectDisconnect.getText().toString().equals("Connect"))
                    {
                        //Connect button pressed, open DeviceListActivity class,
                        // with popup windows that scan for devices...
                        Intent newIntent = new Intent(context, Scanner.class);
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
    public void startButtonListener()
    {
        btnStart = (Button) findViewById(R.id.btn_start);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                if(btnStart.getText().toString().equals("Start"))
                {
                    btnStart.setText("Stop");
                    String message = "Start";

                    count = 0;
                    parameters = new float[11];

                    byte[] value;

                    try
                    {
                        //Send start command to service
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
                    String message = "Stop";;
                    byte[] value;

                    try
                    {
                        //Send stop command to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        // TODO Auto-generated catch block
                        //e.printStackTrace();
                    }
                }
            }
        });
    }

    //Share
    public void shareButtonListener()
    {
        final Context context = Monitoring.this;
        btnShare = (Button) findViewById(R.id.btn_share);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(context, Sharing.class);

                Bundle bundle = new Bundle();
                bundle.putString("fn", filename);
                intent.putExtras(bundle);

                if(count > 2)
                {
                    bundle.putFloat("HR", parameters[0]);
                    bundle.putFloat("A1", parameters[1]);
                    bundle.putFloat("D1", parameters[2]);
                    bundle.putFloat("CV", parameters[3]);
                    bundle.putFloat("SDSD", parameters[4]);
                    bundle.putFloat("RMSSD", parameters[5]);
                    bundle.putFloat("A2", parameters[6]);
                    bundle.putFloat("D2", parameters[7]);
                    bundle.putFloat("K2", parameters[8]);
                    bundle.putFloat("S2", parameters[9]);
                }

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    //More Info.
    public void infoButtonListener()
    {
        final Context context = Monitoring.this;
        btnInfo = (FloatingActionButton) findViewById(R.id.fab);

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {

                Intent intent = new Intent(context, Information.class);

                Bundle bundle = new Bundle();

                bundle.putString("fn", filename);

                if(count > 2)
                {
                    bundle.putFloat("HR", parameters[0]);
                    bundle.putFloat("A1", parameters[1]);
                    bundle.putFloat("D1", parameters[2]);
                    bundle.putFloat("CV", parameters[3]);
                    bundle.putFloat("SDSD", parameters[4]);
                    bundle.putFloat("RMSSD", parameters[5]);
                    bundle.putFloat("A2", parameters[6]);
                    bundle.putFloat("D2", parameters[7]);
                    bundle.putFloat("K2", parameters[8]);
                    bundle.putFloat("S2", parameters[9]);
                }

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
