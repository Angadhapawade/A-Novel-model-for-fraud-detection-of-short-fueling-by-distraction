#include <SoftwareSerial.h>

const int trigPin = 7;
const int echoPin = 6;
// defines variables
long duration;
int distance;

int count=0;

SoftwareSerial mySerial(2, 3); // RX, TX1

char sendFlag = '0';
char receiveFlag = 0;

void setup() {

  //Start seria port
  mySerial.begin(9600);
  Serial.begin(9600);
  pinMode(trigPin, OUTPUT); // Sets the trigPin as an Output
  pinMode(echoPin, INPUT); // Sets the echoPin as an Input
  pinMode(LED_BUILTIN, OUTPUT);
}
 
void loop() {

    //Read the data from the sensors
    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);
    // Sets the trigPin on HIGH state for 10 micro seconds
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);
    // Reads the echoPin, returns the sound wave travel time in microseconds
    duration = pulseIn(echoPin, HIGH);
    // Calculating the distance
    distance= duration*0.034/2;
    // Prints the distance on the Serial Monitor
    Serial.print("Distance: ");
    Serial.println(distance);

    float sensorVoltage;                                      // Initialize Variable for Sensor Voltage
    float sensorValue;                                                // Initialize Variable for Sensor Value
    
    mySerial.print('$');
    mySerial.print(distance);
    mySerial.println('#');
    /*
    if(mySerial.available()){
      receiveFlag = mySerial.read();
      if(receiveFlag == '0') {
        //Keep the status unchanged
      } else {
        Serial.print("Mobile phone command to turn OFF engine:");
        Serial.println(receiveFlag);
        //digitalWrite(LED_BUILTIN, LOW);
      }
    }
    */
    delay(1000);
    
}

