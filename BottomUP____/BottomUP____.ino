#include <SoftwareSerial.h>

//Connection with aduino, android
//Using the Unity

SoftwareSerial btSerial(2,3); //TX, RX

void setup() {
  //put your setup code here, to run once:
  Serial.begin(9600);
  Serial.println("Connection");
  btSerial.begin(9600);
}

void loop() {
  //put your main code here, to run repeatedly:

  String allString = "";

  //connection with FSR
  int sensorvalA0 = analogRead(A0); //Accelator //break change
  int sensorvalA1 = analogRead(A1); //break //accelator change

  //change the value of FSR
  int accelator = map(sensorvalA0, 0, 1023, 0, 100);
  int breakA1 = map(sensorvalA1, 0, 1023, 0, 100);

  while(btSerial.available()){//블루투스에서 데이터 받아올 때
    char data = btSerial.read();

    if(data == '\n'){
       Serial.println(allString + " " + String(breakA1)+ " " + String(accelator));
    }
    else{
      allString += data;
    }
  }

  delay(45);

}
