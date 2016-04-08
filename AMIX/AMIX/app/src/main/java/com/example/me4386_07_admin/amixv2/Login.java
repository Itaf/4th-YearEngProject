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

public class Login extends AppCompatActivity
{
    private String filename;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loginButtonListener();
        registerButtonListener();
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
    public void loginButtonListener()
    {
        final Context context = Login.this;
        btnLogin = (Button) findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(Login.this, R.string.register, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    //New User
    public void registerButtonListener()
    {
        final Context context = Login.this;
        btnRegister = (Button) findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(context, Registration.class);
                startActivity(intent);
            }
        });
    }
}
