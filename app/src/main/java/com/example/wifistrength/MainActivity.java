package com.example.wifistrength;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String FILE_NAME= "info.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    private boolean isExternalStorageWritable() {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.v("State","Yes");
            return true;
        }
        else { return false;}
    }

    public boolean checkPermission(String permission) {
        int check  = ContextCompat.checkSelfPermission(this, permission);
        return (check== PackageManager.PERMISSION_GRANTED);
    }

    public void display(View view) {
        File file = new File(Environment.getExternalStorageDirectory(),FILE_NAME);

        Uri selectedUri = Uri.parse(file.getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "text/csv");

        if (intent.resolveActivityInfo(getPackageManager(), 0) != null)
        {
            startActivity(intent);
        }
        else {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("text/csv");
            startActivityForResult(intent, 7);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub

        switch(requestCode){

            case 7:

                if(resultCode==RESULT_OK){

                    String PathHolder = data.getData().getPath();

                    Toast.makeText(this, PathHolder , Toast.LENGTH_LONG).show();

                }
                break;

        }
    }

    public void compute(View view) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if(!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        StringBuilder s = new StringBuilder();
        s.append("Link name: "+wifiInfo.getSSID()+"\n");
        s.append("Link Speed: "+wifiInfo.getLinkSpeed()+" Mbps\n");
        s.append("MAC address: "+wifiInfo.getMacAddress()+"\n");
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);
        s.append("IP address: "+ipAddress+"\n");
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(),5);
        s.append("WiFi Strength level: "+level+"\n");

        TextView ans = (TextView) findViewById(R.id.result);
//        Log.v("Wifi", s.toString());
        ans.setText(s.toString());

        if(isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            File file = new File(Environment.getExternalStorageDirectory(),FILE_NAME);
            Log.v("External file", file.toString());

            try {
                FileOutputStream fos = new FileOutputStream(file);

                level =0;
                StringBuilder log = new StringBuilder();

                for(int i=0;i<60;i++) {
                    wifiInfo = wifiManager.getConnectionInfo();
                    level = wifiInfo.getRssi();
                    log.append(String.valueOf(level)+" dbm\t");
                    log.append("at time: "+String.valueOf(System.currentTimeMillis())+"\n");
                    Thread.sleep(100);
                }
                fos.write(log.toString().getBytes());
                fos.close();
                Toast.makeText(this,"File Saved to: "+file.toString(),Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
        else {
            Toast.makeText(this,"Cannot perform write operation, Permission denied",Toast.LENGTH_LONG).show();
        }

        File file = new File(Environment.getExternalStorageDirectory(),FILE_NAME);

        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            ArrayList<String> disp = new ArrayList<String>();
            String text;

            while((text = br.readLine())!=null) {
                disp.add(text);
            }


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, disp);
            ListView listView = (ListView) findViewById(R.id.display);
            listView.setAdapter(adapter);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
