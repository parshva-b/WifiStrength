package com.example.wifistrength;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class MainActivity extends AppCompatActivity {

    private static final String FILE_NAME= "info.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView display = findViewById(R.id.display);
        display.setMovementMethod(new ScrollingMovementMethod());

        File temp = new File(getFilesDir() + "/"+FILE_NAME);
        if (temp.exists()) {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(temp, "rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                raf.setLength(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void display(View view) {
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder s = new StringBuilder();
            String text;

            while((text = br.readLine())!=null) {
                s.append(text).append("\n");
            }

            TextView tv = findViewById(R.id.display);
            tv.setText(s.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void compute(View view) {
//
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) {
//            Log.v("bacchi","wifi bandh hai bacchi");
//            return;
            wifiManager.setWifiEnabled(true);
        }

        for(int i=0;i<60;i++) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int level = wifiInfo.getRssi();
            //        Log.v("Wifi Strength", "Ans: "+level);
            TextView ans = (TextView) findViewById(R.id.result);
            ans.setText(String.valueOf(level)+" dbm");

        FileOutputStream fos = null;

        try {
            fos = openFileOutput(FILE_NAME,MODE_APPEND);
            fos.write(String.valueOf(level).getBytes());
            fos.write(" dbm\n".getBytes());
            Toast.makeText(this, "Saved to "+ getFilesDir() + "/" +FILE_NAME,Toast.LENGTH_LONG).show();
            Thread.sleep(300);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if(fos!=null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        }

    }

}
