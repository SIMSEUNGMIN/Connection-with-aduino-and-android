package com.myapp.user.transportdata;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {

    //Using the bluetooth
    private BluetoothSPP bt;

    //Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager = null;

    //Using the Accelometer
    private SensorEventListener mAccLis;
    private Sensor mAccelometerSensor = null;

    //측정값
    String sendAngleXY = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Usgin the bluetooth
        bt = new BluetoothSPP(this); //initializing

        //블루투스 사용 불가시
        if(!bt.isBluetoothAvailable()){
            Toast.makeText(getApplicationContext(),
                    "Bluetooth is not available",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        //블루투스 데이터 수신
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        //블루투스 연결 됐을 때
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            //연결시
            @Override
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext(),
                        "Connect to " + name + "\n" + address,
                        Toast.LENGTH_SHORT).show();
            }

            //연결 해제시
            @Override
            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext(),
                        "Connection lost", Toast.LENGTH_SHORT).show();
            }

            //연결 실패시
            @Override
            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext(),
                        "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        //연결 시도
        Button btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bt.getServiceState() == BluetoothState.STATE_CONNECTED){
                    bt.disconnect();
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });

        //연결 끊기
        Button btnDisConnect = findViewById(R.id.btnDisConnect);
        btnDisConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt.disconnect();
            }
        });

        //Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Using the Accelometer
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccLis = new AccelometerListener();


        //Click the button to start Accelometer
        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        });

        //Click the button to end Accelometer
        findViewById(R.id.btnEnd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSensorManager.unregisterListener(mAccLis);
            }
        });
    }

    public void onDestroy(){
        super.onDestroy();
        bt.stopService();
    }

    public void onStart(){
        super.onStart();
        if(!bt.isBluetoothEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        }
        else{
            if(!bt.isServiceAvailable()){
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드끼리
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE){
            if(resultCode == Activity.RESULT_OK) {
                bt.connect(data);
            }
        }
        else if(requestCode == BluetoothState.REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }
            else{
                Toast.makeText(getApplicationContext(),
                        "Bluetooth was not enabled.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    private class AccelometerListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event){

            double accX = event.values[0];
            double accY = event.values[1];

            double angleXY = Math.atan2(accY, accX) * 180 / Math.PI;
            sendAngleXY = String.format("%.0f", angleXY);

            bt.send(sendAngleXY, true);

            Log.e("Log", "xy= " + sendAngleXY);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
