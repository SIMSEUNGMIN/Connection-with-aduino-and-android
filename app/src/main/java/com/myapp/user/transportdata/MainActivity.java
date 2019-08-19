package com.myapp.user.transportdata;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static app.akexorcist.bluetotohspp.library.BluetoothState.REQUEST_ENABLE_BT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Layout
    Button recvButton = null;
    Button sendButton = null;

    //Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager = null;

    //Using the Accelometer
    private SensorEventListener mAccLis;
    private Sensor mAccelometerSensor = null;

    //측정값
    String sendAngleXY = null;

    //Using the bluetooth
    private BluetoothAdapter btAdapter = null;

    //페어링된 기기 목록
    Set<BluetoothDevice> pairedDevices = null;

    //페어링 된 블루투스 장치의 이름, 맥주소 목록
    List<String> pairedDevicesList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recvButton = findViewById(R.id.RECVConnect);
        sendButton = findViewById(R.id.SENDConnect);

        //Using the Gyroscope & Accelometer
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Using the Accelometer
        mAccelometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccLis = new AccelometerListener();

        //buttton Event
        recvButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);

        //블루투스 사용 가능하도록 설정
        initBluetooth();

//        //Click the button to start Accelometer
//        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //시작 코드
//                mSensorManager.registerListener(mAccLis, mAccelometerSensor, SensorManager.SENSOR_DELAY_UI);
//            }
//        });

//        //Click the button to end Accelometer
//        findViewById(R.id.btnEnd).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //종료 코드
//                mSensorManager.unregisterListener(mAccLis);
//            }
//        });

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

    private void getPairedDevices(final int numberOfId) {
        //연결하고자 하는 장치와 이미 페어링이 이루어져있다고 가정
        //Toast.makeText(this, "연결 가능한 기기 목록을 불러옵니다.", Toast.LENGTH_SHORT).show();

        pairedDevices = btAdapter.getBondedDevices();

        if(pairedDevices.size() > 0){
            //페어링된 장치가 있는 경우
            Toast.makeText(this, "연결 가능한 기기가 존재합니다.", Toast.LENGTH_SHORT).show();

            //Use the AlertDialog that print the paired devices list
            AlertDialog.Builder btBuilder = new AlertDialog.Builder(this);
            btBuilder.setTitle("블루투스 장치 선택");

            //페어링 된 블루투스 장치의 이름, 맥주소 목록 작성
            pairedDevicesList = new ArrayList<String>();

            for(BluetoothDevice device : pairedDevices){
                pairedDevicesList.add(device.getName() + "\n" + device.getAddress());
            }

            final CharSequence[] devices = pairedDevicesList.toArray(new CharSequence[pairedDevicesList.size()]);

            btBuilder.setItems(devices, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int number) {
                    //연결하고자 하는 장치의  bluetoothDevice 객체 불러오기
                    BluetoothDevice selectDevice = getDevice(pairedDevicesList.get(number));

                    //RECV일 경우 아두이노와 연결
                    //SEND일 경우 안드로이드와 연결
                    if(numberOfId == R.id.RECVConnect){
                        connectWithAduino(selectDevice);
                    }
                    else{
                        connectWithAdroid(selectDevice);
                    }

                }
            });

            AlertDialog deviceList = btBuilder.create();
            deviceList.show();
        }
        else{
            //페어링된 장치가 없는 경우
            Toast.makeText(this, "연결 가능한 기기가 없습니다.", Toast.LENGTH_SHORT).show();

        }
    }

    private BluetoothDevice getDevice(String data){
        BluetoothDevice selectDevice = null;

        String[] splitData = data.split("\n");

        String name = splitData[0];
        String address = splitData[1];

        for(BluetoothDevice device : pairedDevices){
            if(name.equals(device.getName()) && address.equals(device.getAddress())){
                selectDevice = device;
                break;
            }
        }

        return selectDevice;
    }

    private void connectWithAduino(BluetoothDevice selectDevice){
        UUID uuid = UUID.fromString("000011001-0000-1000-8000-00805F9B34FB");

        try{
            //소켓 생성
            BluetoothSocket aduinoSocket = selectDevice.createRfcommSocketToServiceRecord(uuid);
            //RMCOMM 채넣을 통한 연결
            aduinoSocket.connect();

        } catch (IOException e) {
            Toast.makeText(this, "연결 실패", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void connectWithAdroid(BluetoothDevice selectDevice){

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.RECVConnect:
                //페어링된 원격 디바이스 목록 구하기
                getPairedDevices(v. getId());
                break;
            case R.id.SENDConnect:
                //페어링된 원격 디바이스 목록 구하기
                getPairedDevices(v. getId());
                break;
        }
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
