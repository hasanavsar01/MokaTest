package com.example.mokatest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import Moka7.*;

public class MainActivity extends AppCompatActivity {
    private S7Client client = new S7Client();
    private String[] parameters;
    Button buttonStart, buttonStop;
    boolean isActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);

        buttonStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                isActive = true;
                readData();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                isActive = false;
            }
        });
    }

    public void readData(){
        parameters = new String[5];
        parameters[0] = ((EditText) findViewById(R.id.editTextIp)).getText().toString();
        parameters[1] = ((EditText) findViewById(R.id.editTextRack)).getText().toString();
        parameters[2] = ((EditText) findViewById(R.id.editTextSlot)).getText().toString();
        parameters[3] = ((EditText) findViewById(R.id.editTextDb)).getText().toString();
        parameters[4] = ((EditText) findViewById(R.id.editTextAddress)).getText().toString();

        new PlcReader().execute(parameters);

        if(isActive){
            refresh(3000);
        }
    }

    private void refresh(int miliseconds){
        final Handler handler = new Handler();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                readData();
            }
        };
        handler.postDelayed(runnable, miliseconds);
    }

    public void readdb_val(View v ){

        parameters = new String[5];
        parameters[0] = ((EditText) findViewById(R.id.editTextIp)).getText().toString();
        parameters[1] = ((EditText) findViewById(R.id.editTextRack)).getText().toString();
        parameters[2] = ((EditText) findViewById(R.id.editTextSlot)).getText().toString();
        parameters[3] = ((EditText) findViewById(R.id.editTextDb)).getText().toString();
        parameters[4] = ((EditText) findViewById(R.id.editTextAddress)).getText().toString();

        new PlcReader().execute(parameters);
    }

    private class PlcReader extends AsyncTask<String, Void, String> {
        String ret = "";

        @Override
        protected String doInBackground(String... params){
            try{
                client.SetConnectionType(S7.S7_BASIC);

                String IpAddress = params[0];
                int rack = Integer.parseInt(params[1]);
                int slot = Integer.parseInt(params[2]);
                int dbNo = Integer.parseInt(params[3]);
                int startAddress = Integer.parseInt(params[4]);

                int res = client.ConnectTo(IpAddress, rack,slot); //PLC data

                if(res == 0){//connection ok
                    byte[] data = new byte[4];
                    res = client.ReadArea(S7.S7AreaDB,dbNo,startAddress,4, data);
                    ret = "Value of DB7.DBD0:" + S7.GetFloatAt(data, 0);
                }else{
                    ret = "ERROR: " + S7Client.ErrorText(res);
                    isActive = false;
                }
                client.Disconnect();
            }catch(Exception e){
                ret = "EXC: " + e.toString();
                isActive = false;
                Thread.interrupted();
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result){
            TextView txValue = (TextView) findViewById(R.id.textViewValue);
            txValue.setText(ret);
        }
    }
}
