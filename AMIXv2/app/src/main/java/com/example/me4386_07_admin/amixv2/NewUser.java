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

import java.io.FileOutputStream;

public class NewUser extends AppCompatActivity
{
    private String filename;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addListenerOnButton();  //SignUp button listener
        addListenerOnButton1(); //Cancel button listener
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


    private void writeFile()
    {
        final Context context = NewUser.this;
        filename = ((EditText) findViewById(R.id.editText1)).getText().toString();
        String string = ((EditText) findViewById(R.id.editText1)).getText().toString() + "\n";
        String string1 = ((EditText) findViewById(R.id.editText2)).getText().toString() + "\n";
        String string2 = ((EditText) findViewById(R.id.editText4)).getText().toString() + "\n";
        String string3 = ((EditText) findViewById(R.id.editText6)).getText().toString() + "\n";
        String string4 = ((EditText) findViewById(R.id.editText7)).getText().toString() + "\n";
        String string5 = ((EditText) findViewById(R.id.editText11)).getText().toString() + "\n";
        FileOutputStream outputStream;

        try
        {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(("Name: " + string).getBytes());
            outputStream.write(("Birth: " + string1).getBytes());
            outputStream.write(("Gender: " + string2).getBytes());
            outputStream.write(("Address: " + string3).getBytes());
            outputStream.write(("Condition: " + string4).getBytes());
            outputStream.write((string5).getBytes());
            outputStream.close();
            Toast.makeText(context, "Registration successful", Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /* BUTTON LISTENERS */

    //Sign Up
    public void addListenerOnButton()
    {
        final Context context = NewUser.this;
        button = (Button) findViewById(R.id.button3);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                writeFile();

                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    //Cancel
    public void addListenerOnButton1()
    {
        final Context context = NewUser.this;
        button = (Button) findViewById(R.id.button4);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
