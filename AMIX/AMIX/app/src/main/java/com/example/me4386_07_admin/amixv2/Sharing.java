package com.example.me4386_07_admin.amixv2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Sharing extends AppCompatActivity
{
    private String filename;
    private String message1, message2, message3;
    private String to;
    private Button btnSend;
    private float [] parameters;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        filename = bundle.getString("fn");

        parameters = new float[11];
        parameters[0] = bundle.getFloat("HR");
        parameters[1] = bundle.getFloat("A1");
        parameters[2] = bundle.getFloat("D1");
        parameters[3] = bundle.getFloat("CV");
        parameters[4] = bundle.getFloat("SDSD");
        parameters[5] = bundle.getFloat("RMSSD");
        parameters[6] = bundle.getFloat("A2");
        parameters[7] = bundle.getFloat("D2");
        parameters[8] = bundle.getFloat("K2");
        parameters[9] = bundle.getFloat("S2");

        message1 = readFile();

        message2 = "PPG Parameters: \n"
                + "Heart Rate = " + parameters[0] + " BPM\n"
                + "Average = " + parameters[1] + "\n"
                + "Standard Deviation = " + parameters[2] + "\n"
                + "CV = " + parameters[3] + "\n"
                + "SDSD = " + parameters[4] + "\n"
                + "RMSSD = " + parameters[5];

        message3 = "GSR Parameters: \n"
                + "Average = " + parameters[6] + "\n"
                + "Standard Deviation = " + parameters[7] + "\n"
                + "Kurtosis = " + parameters[8] + "\n"
                + "Skewness = " + parameters[9];

        ((TextView) findViewById(R.id.info)).setText(message1 + "\n\n" + message2 + "\n\n" + message3);

        sendButtonListener();
    }

    private String readFile()
    {
        int count = 0;
        String s = "";

        FileInputStream inputStream;
        try
        {
            inputStream = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            BufferedReader br = new BufferedReader(inputStreamReader);
            String recv = "";
            StringBuilder sb = new StringBuilder();

            while((recv = br.readLine()) != null)
            {
                count++;
                if(count == 6)
                {
                    to = recv;
                    break;
                }
                else if(count == 5)
                {
                    sb.append(recv);
                }
                else
                {
                    sb.append(recv+"\n");
                }
            }

            inputStream.close();
            s = sb.toString();
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }

        return s;
    }

    /* BUTTON LISTENER */
    public void sendButtonListener()
    {
        btnSend = (Button) findViewById(R.id.btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                sendMessage();
            }
        });
    }

    private void sendMessage()
    {
        try
        {
            String comment = ((EditText) findViewById(R.id.comment)).getText().toString();
            SmsManager m = SmsManager.getDefault();

            if(!comment.equals(""))
            {
                m.sendTextMessage(to, null, comment, null, null);
            }
            m.sendTextMessage(to, null, message1, null, null);
            m.sendTextMessage(to, null, message2, null, null);
            m.sendTextMessage(to, null, message3, null, null);

            Toast.makeText(getApplicationContext(),"Sent successfully", Toast.LENGTH_LONG).show();
        }
        catch(Exception ex)
        {
            Toast.makeText(getApplicationContext(),"Failed to send", Toast.LENGTH_LONG).show();
            //ex.printStackTrace();
        }
    }
}

