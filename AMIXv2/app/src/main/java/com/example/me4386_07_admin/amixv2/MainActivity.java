package com.example.me4386_07_admin.amixv2;


import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity
{
    private String filename;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addListenerOnButton1();
        addListenerOnButton2();
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
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    public boolean fileLookup()
    {
        filename = ((EditText) findViewById(R.id.editText)).getText().toString();
        String s = "";
        FileInputStream inputStream;

        try
        {
            inputStream = openFileInput(filename);
            //InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            inputStream.close();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /* BUTTON LISTENERS */

    //Login
    public void addListenerOnButton1()
    {
        final Context context = MainActivity.this;
        button = (Button) findViewById(R.id.button1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                boolean found = fileLookup();
                if(found)
                {
                    Intent intent = new Intent(context, Monitoring.class);

                    Bundle bundle = new Bundle();
                    bundle.putString("fn", filename);
                    intent.putExtras(bundle);

                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(MainActivity.this, R.string.register, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    //New User
    public void addListenerOnButton2()
    {
        final Context context = MainActivity.this;
        button = (Button) findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(context, NewUser.class);
                startActivity(intent);
            }
        });
    }


}
