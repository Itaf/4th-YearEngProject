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

public class Emergency extends AppCompatActivity
{
    String filename;
    String message;
    String to;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        filename = bundle.getString("fn");

        message = readFile();
        ((TextView) findViewById(R.id.info)).setText(message);

        addListenerOnButton();
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
            e.printStackTrace();
        }

        return s;
    }

    /* BUTTON LISTENER */
    public void addListenerOnButton()
    {
        button = (Button) findViewById(R.id.send);

        button.setOnClickListener(new View.OnClickListener() {
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
            //to = message.substring(message.length()-11, message.length());
            m.sendTextMessage(to, null, comment + "\n" + message, null, null);
            Toast.makeText(getApplicationContext(),"Sent successfuly", Toast.LENGTH_LONG).show();
        }
        catch(Exception ex)
        {
            Toast.makeText(getApplicationContext(),"Failed to send", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }
}

