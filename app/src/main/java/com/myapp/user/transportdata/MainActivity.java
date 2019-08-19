package com.myapp.user.transportdata;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;

import static app.akexorcist.bluetotohspp.library.BluetoothState.REQUEST_ENABLE_BT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    //Layout
    Button recvButton = null;
    Button sendButton = null;
    ListView pairedDevices = null;

    //Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager = null;

    //Using the Accelometer
    private SensorEventListener mAccLis;
    private Sensor mAccelometerSensor = null;

    //측정값
    String sendAngleXY = null;

    //Using the bluetooth
    private BluetoothAdapter btAdapter = null;

    //paired devices
    private ArrayAdapter<String> arrayAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recvButton = findViewById(R.id.RECVConnect);
        sendButton = findViewById(R.id.SENDConnect);
        pairedDevices = findViewById(R.id.pairedDevices);

        //Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Using the Accelometer
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccLis = new AccelometerListener();

        //buttton Event
        recvButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        //ListView 초기화
        initPairedDevicesList();

        //블루투스 사용 가능하도록 설정
        initBluetooth();
    }

    private void initPairedDevicesList() {

//        //일단 목록 숨기기
//        pairedDevices.setVisibility(View.INVISIBLE);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        pairedDevices.setAdapter(arrayAdapter);
        pairedDevices.setOnItemClickListener(this);
    }

    private void initBluetooth() {
        //블루투스를 지원하는지 확인
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter == null){
            //device does not support Bluetooth
            Toast.makeText(this, "블루투스 사용 불가능", Toast.LENGTH_SHORT).show();
        }

        //블루투스 활성화 확인
        if(!btAdapter.isEnabled()){
            //비활성화시 활성화
            Toast.makeText(this, "블루투스를 활성화시킵니다.", Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Toast.makeText(this, "블루투스가 활성화 되어있습니다.", Toast.LENGTH_SHORT).show();
    }

    private void getPairedDevices() {
        //연결하고자 하는 장치와 이미 페어링이 이루어져있다고 가정
        Toast.makeText(this, "연결 가능한 기기 목록을 불러옵니다.", Toast.LENGTH_SHORT).show();

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if(pairedDevices.size() > 0){
            //페어링된 장치가 있는 경우
            Toast.makeText(this, "연결 가능한 기기가 존재합니다.", Toast.LENGTH_SHORT).show();

        }
        else{
            //페어링된 장치가 없는 경우
            Toast.makeText(this, "연결 가능한 기기가 없습니다.", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.RECVConnect:
                //페어링된 원격 디바이스 목록 구하기
                getPairedDevices();
                break;
            case R.id.SENDConnect:
                //페어링된 원격 디바이스 목록 구하기
                getPairedDevices();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }


    private class AccelometerListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event){

            double accX = event.values[0];
            double accY = event.values[1];

            double angleXY = Math.atan2(accY, accX) * 180 / Math.PI;
            sendAngleXY = String.format("%.0f", angleXY);

            //recvbt.send(sendAngleXY, true);

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
