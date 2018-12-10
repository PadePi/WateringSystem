/*

 This sketch will print the IP address of your WiFi Shield (once connected)
 to the Serial monitor. From there, you can open that address in a web browser.
 
 */

#include <WiFi.h>
#include <Wire.h>
#include "RTClib.h"

RTC_DS3231 rtc;

char daysOfTheWeek[7][12] = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};


const char* ssid     = "AndroidAPCEA3";
const char* password = "12345678";

//Analog Input for moisure sensor
#define ANALOG_PIN_MOISTURE 36
int soil_moisture = 0;

//Digital output for water pump
#define DIGITAL_PIN_PUMP 2

//Pins and variables for sonic sensor
#define SONIC_TRIGGER 33
#define SONIC_ECHO 32

long duration;
int distance;

//variables for automation

//If true watering will start automatically depending on minimal water level
//If false watering will start automatically depending on scheduled days
boolean automatedByMoisture;

int minimal_soil_moisture;

//indexes from 0 to 7 are weekdays from Sunday to Saturday
//If index is true watering is needed on the day, otherwise it's false
boolean daysToWater[7];

boolean already_watered_today=false;

char* currentDay;
int reversed_moisture;

WiFiServer server(80);

void setup()
{
    Serial.begin(9600);
    DateTime now = rtc.now();
    
    automatedByMoisture=true;

    minimal_soil_moisture=0;

    currentDay=daysOfTheWeek[now.dayOfTheWeek()];
    
    pinMode(DIGITAL_PIN_PUMP, OUTPUT);      // set the water pump pin mode

    pinMode(SONIC_TRIGGER, OUTPUT); // Sets the trigPin as an Output
    pinMode(SONIC_ECHO, INPUT); // Sets the echoPin as an Input

    delay(10000);

    // We start by connecting to a WiFi network

    Serial.println();
    Serial.println();
    Serial.print("Connecting to ");
    Serial.println(ssid);

    WiFi.begin(ssid, password);

    while (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.print(".");
    }

    Serial.println("");
    Serial.println("WiFi connected.");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());
    
    server.begin();

}

void loop(){
 WiFiClient client = server.available();   // listen for incoming clients
 DateTime now = rtc.now();
 
 if(automatedByMoisture)
 {  
    soil_moisture = analogRead(ANALOG_PIN_MOISTURE);
    reversed_moisture=(soil_moisture-4095)*(-1);
    if(reversed_moisture<minimal_soil_moisture && !already_watered_today)
    {
      Serial.println("Automated moisture condition matched");
      digitalWrite(DIGITAL_PIN_PUMP, HIGH);
      delay(10000);
      digitalWrite(DIGITAL_PIN_PUMP, LOW);
      already_watered_today=true;
    }
 }

 if(!automatedByMoisture)
 {
    if(daysToWater[now.dayOfTheWeek()] && !already_watered_today)
    {
      Serial.println("Automated by days condition matched");
      digitalWrite(DIGITAL_PIN_PUMP, HIGH);
      delay(10000);
      digitalWrite(DIGITAL_PIN_PUMP, LOW);
      already_watered_today=true;
    }
 }

 newDayInitialization();

  if (client) {                             // if you get a client,
    Serial.println("New Client.");           // print a message out the serial port
    String currentLine = "";                // make a String to hold incoming data from the client
    while (client.connected()) {            // loop while the client's connected
      if (client.available()) {             // if there's bytes to read from the client,
        char c = client.read();             // read a byte, then
        Serial.write(c);                    // print it out the serial monitor
        if (c == '\n') {                    // if the byte is a newline character

          // if the current line is blank, you got two newline characters in a row.
          // that's the end of the client HTTP request, so send a response:
          if (currentLine.length() == 0) {
            // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
            // and a content-type so the client knows what's coming, then a blank line:

            // break out of the while loop:
            break;
          } else {    // if you got a newline, then clear currentLine:
            currentLine = "";
          }
        } else if (c != '\r') {  // if you got anything else but a carriage return character,
          currentLine += c;      // add it to the end of the currentLine
        }

        // Check to see if the client request was "GET /H" or "GET /L":
        if (currentLine.endsWith("GET /measureMoisture")) {

            soil_moisture = analogRead(ANALOG_PIN_MOISTURE);
            client.println("HTTP/1.1 200 OK");
            client.println("Content-type:text/html");
            client.println();

            // the content of the HTTP response follows the header:
            client.print(soil_moisture);

            // The HTTP response ends with another blank line:
            client.println();
        }
        if (currentLine.endsWith("POST /startPump")) {
          digitalWrite(DIGITAL_PIN_PUMP, HIGH);                // Start water pump
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
        } if (currentLine.endsWith("POST /stopPump")) {
          digitalWrite(DIGITAL_PIN_PUMP, LOW);                // Start water pump
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
        }if (currentLine.endsWith("GET /waterLevel")) {
          // Clears the trigPin
          digitalWrite(SONIC_TRIGGER, LOW);
          delayMicroseconds(2);
          // Sets the trigPin on HIGH state for 10 micro seconds
          digitalWrite(SONIC_TRIGGER, HIGH);
          delayMicroseconds(10);
          digitalWrite(SONIC_TRIGGER, LOW);
          // Reads the echoPin, returns the sound wave travel time in microseconds
          duration = pulseIn(SONIC_ECHO, HIGH);
          // Calculating the distance
          distance= duration*0.034/2;
          // Prints the distance on the Serial Monitor
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();

          // the content of the HTTP response follows the header:
          client.print(distance);

          // The HTTP response ends with another blank line:
          client.println();
          
        }if (currentLine.endsWith("POST /minimalWater")) {
          Serial.println("Automation MOISTURE settings reached");
          automatedByMoisture=true;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }if (currentLine.endsWith("POST /scheduledDays")) {
          Serial.println("Automation DAYS settings reached");
          automatedByMoisture=false;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }if (currentLine.endsWith("POST /under40")) {
          Serial.println("UNDER 40 settings reached");
          minimal_soil_moisture=4095 * 0.4;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }
        if (currentLine.endsWith("POST /under50")) {
          minimal_soil_moisture=4095 * 0.5;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }if (currentLine.endsWith("POST /under60")) {
          minimal_soil_moisture=4095 * 0.6;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }if (currentLine.indexOf("Sunday")>0) {
          Serial.println("SUNDAY reached");
          daysToWater[0]=true;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }if (currentLine.indexOf("Monday")>0) {
          daysToWater[1]=true;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }
        if (currentLine.indexOf("Tuesday")>0) {
          daysToWater[2]=true;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }if (currentLine.indexOf("Wednesday")>0) {
          daysToWater[3]=true;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }if (currentLine.indexOf("Thursday")>0) {
          Serial.println("THURSDAY settings reached");
          daysToWater[4]=true;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }if (currentLine.indexOf("Friday")>0) {
          daysToWater[5]=true;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }if (currentLine.indexOf("Saturday")>0) {
          daysToWater[6]=true;
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }if (currentLine.indexOf("noDaySelected")>0) {
          int i;
          for (i = 0; i < 7; ++i)
          {
              daysToWater[i] = false;
          }
          client.println("HTTP/1.1 200 OK");
          client.println("Content-type:text/html");
          client.println();
          // The HTTP response ends with another blank line:
          client.println();
        }
      }
    }
    // close the connection:
    client.stop();
    Serial.println(currentDay);
    Serial.println(now.dayOfTheWeek());
    Serial.println(automatedByMoisture);
    Serial.println(already_watered_today);
    Serial.println(daysToWater[4]);
    Serial.println(reversed_moisture);
    Serial.println(minimal_soil_moisture);
    int j=0;
    String days;
    for (j = 0; j < 7; ++j)
    {
      if(daysToWater[j]== true){
        days+=daysOfTheWeek[j];
      }
    }
    Serial.println(days);
  }
}


void newDayInitialization(){
  DateTime now = rtc.now();
  if(currentDay!=daysOfTheWeek[now.dayOfTheWeek()])
  {
    already_watered_today=false;
    currentDay=daysOfTheWeek[now.dayOfTheWeek()];
  }
}
