#include <BluetoothSerial.h>


const int ledPin = 5; 

// Setting PWM properties
const int freq = 5000;
const int ledChannel = 0;
const int resolution = 8;

// cau hinh blueth
BluetoothSerial SerialBT;
String message = "";
char incomingChar;


void setup() {
  // Configure LED PWM functionalities
  ledcSetup(ledChannel, freq, resolution);
  // Attach the channel to the GPIO to be controlled
  ledcAttachPin(ledPin, ledChannel);

  // Start Serial and Bluetooth
  Serial.begin(115200);
  SerialBT.begin("ESP32_bluetooth");
  Serial.println("The device started, now you can pair it with Bluetooth!");
}

void loop() {
  ControlLed();
}

void processMessage(String msg) {
  if (msg == "led_on") {
    // Turn on the LED
    ledcWrite(ledChannel, 255); // Full brightness
  } else if (msg == "led_off") {
    // Turn off the LED
    ledcWrite(ledChannel, 0); // No brightness
  } else {
    // Handle PWM values
    int value = msg.toInt();
    if (value >= 0 && value <= 255) {
      ledcWrite(ledChannel, value);
    }
  }
}

void ControlLed()
{
    // Read received messages from Bluetooth
  if (SerialBT.available()) {
    char incomingChar = SerialBT.read();
    if (incomingChar != '\n') {
      message += String(incomingChar);
    } else {
      processMessage(message);
      message = "";
    }
    Serial.write(incomingChar);  // Debug: Echo received characters to Serial Monitor
  }

  // Optional: Read from Serial monitor if needed
  if (Serial.available() > 0) {
    String serialMessage = "";
    while (Serial.available() > 0) {
      char command = (byte)Serial.read();
      if (command == ':') {
        break;
      } else {
        serialMessage += command;
      }
      delay(1);
    }
    processMessage(serialMessage);
  }

  delay(20);
}
