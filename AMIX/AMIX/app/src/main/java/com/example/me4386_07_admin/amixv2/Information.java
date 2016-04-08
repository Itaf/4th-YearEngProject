package com.example.me4386_07_admin.amixv2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.TextView;

public class Information extends AppCompatActivity
{
    private float [] parameters;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_information);
        android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP;
        layoutParams.y = 150;

        Bundle bundle = getIntent().getExtras();

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

        String s = "PPG Parameters: \n\n"
                + "Heart Rate = " + parameters[0] + " BPM\n"
                + "Average = " + parameters[1] + "\n"
                + "Standard Deviation = " + parameters[2] + "\n"
                + "CV = " + parameters[3] + "\n"
                + "SDSD = " + parameters[4] + "\n"
                + "RMSSD = " + parameters[5] + "\n"
                + "\nGSR Parameters: \n\n"
                + "Average = " + parameters[6] + "\n"
                + "Standard Deviation = " + parameters[7] + "\n"
                + "Kurtosis = " + parameters[8] + "\n"
                + "Skewness = " + parameters[9] + "\n";

        ((TextView) findViewById(R.id.parameters)).setText(s);
    }
}
