/*

 This sketch will print the IP address of your WiFi Shield (once connected)
 to the Serial monitor. From there, you can open that address in a web browser.
 
 */

#include <WiFi.h>


const char* ssid     = "UPC1179815";
const char* password = "QOECXIIM";

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

int minimal_water_level;

//indexes from 0 to 7 are weekdays from Monday to Sunday
//If index is true watering is needed on the day, otherwise it's false
boolean daysToWater[7];

boolean already_watered_today=false;


WiFiServer server(80);

void setup()
{
    Serial.begin(9600);
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

int value = 0;

void loop(){
 WiFiClient client = server.available();   // listen for incoming clients

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
          
        }
      }
    }
    // close the connection:
    client.stop();
    Serial.println("Client Disconnected.");
  }
}
