
/*********************************************************************
This is an example for our nRF8001 Bluetooth Low Energy Breakout

  Pick one up today in the adafruit shop!
  ------> http://www.adafruit.com/products/1697

Adafruit invests time and resources providing this open source code, 
please support Adafruit and open-source hardware by purchasing 
products from Adafruit!

Written by Kevin Townsend/KTOWN  for Adafruit Industries.
MIT license, check LICENSE for more information
All text above, and the splash screen below must be included in any redistribution
*********************************************************************/

// This version uses call-backs on the event and RX so there's no data handling in the main loop!

#include <SPI.h>
#include "Adafruit_BLE_UART.h"

#define ADAFRUITBLE_REQ 10
#define ADAFRUITBLE_RDY 2
#define ADAFRUITBLE_RST 9

int pulsePin = 0;   // Pulse Sensor purple wire connected to analog pin 0


// these variables are volatile because they are used during the interrupt service routine!
volatile int BPM;               // used to hold the pulse rate
volatile int Signal;            // holds the incoming raw data
volatile int IBI = 600;         // holds the time between beats, must be seeded! 
volatile boolean Pulse = false; // true when pulse wave is high, false when it's low
volatile boolean QS = false;    // becomes true when Arduoino finds a beat.

volatile int rate[10];                    // array to hold last ten IBI values
volatile unsigned long sampleCounter = 0; // used to determine pulse timing
volatile unsigned long lastBeatTime = 0;  // used to find IBI
volatile int P = 512;                     // used to find peak in pulse wave, seeded
volatile int T = 512;                     // used to find trough in pulse wave, seeded
volatile int thresh = 525;                // used to find instant moment of heart beat, seeded
volatile int amp = 100;                   // used to hold amplitude of pulse waveform, seeded
volatile boolean firstBeat = true;        // used to seed rate array so we startup with reasonable BPM
volatile boolean secondBeat = false;      // used to seed rate array so we startup with reasonable BPM

//GSR variable
volatile int Signal_GSR;

Adafruit_BLE_UART uart = Adafruit_BLE_UART(ADAFRUITBLE_REQ, ADAFRUITBLE_RDY, ADAFRUITBLE_RST);

/**************************************************************************/
/*      This function is called whenever select ACI events happen         */
/**************************************************************************/
void aciCallback(aci_evt_opcode_t event)
{
  switch(event)
  {
    case ACI_EVT_DEVICE_STARTED:
      Serial.println(F("Advertising started"));
      break;
    case ACI_EVT_CONNECTED:
      Serial.println(F("Connected!"));
      break;
    case ACI_EVT_DISCONNECTED:
      Serial.println(F("Disconnected or advertising timed out"));
      break;
    default:
      break;
  }
}

/**************************************************************************/
/*    This function is called whenever data arrives on the RX channel     */
/**************************************************************************/
void rxCallback(uint8_t *buffer, uint8_t len)
{
  Serial.print(F("Received "));
  Serial.print(len);
  Serial.print(F(" bytes: "));
  
  for(int i = 0; i < len; i++)
  {
    Serial.print((char)buffer[i]); 
  }
  Serial.print(F(" ["));

  for(int i = 0; i < len; i++)
  {
    Serial.print(" 0x"); Serial.print((char)buffer[i], HEX); 
  }
  Serial.println(F(" ]"));

  /* Echo the PPG Point, Gsr Point, and Heart Rate (BPM) back! */
  for(int j = 0; j < 1000; j++) 
  {
    uart.print ("P"+String(Signal,DEC)+"G"+String(Signal_GSR,DEC)+"H"+ String(BPM));
  }
}

void interruptSetup()
{     
  // Initializes Timer2 to throw an interrupt every 2mS.
  TCCR2A = 0x02;     // DISABLE PWM ON DIGITAL PINS 3 AND 11, AND GO INTO CTC MODE
  TCCR2B = 0x06;     // DON'T FORCE COMPARE, 256 PRESCALER 
  OCR2A = 0x7C;      // SET THE TOP OF THE COUNT TO 124 FOR 500Hz SAMPLE RATE
  TIMSK2 = 0x02;     // ENABLE INTERRUPT ON MATCH BETWEEN TIMER2 AND OCR2A
  sei();             // MAKE SURE GLOBAL INTERRUPTS ARE ENABLED 
}

ISR(TIMER2_COMPA_vect)
{                     
  // triggered when Timer2 counts to 124
  cli();                                  // disable interrupts while we do this
  Signal = analogRead(0);                 // read the PPG (Pulse) Sensor 
  Signal_GSR = analogRead(2);             // read the GSR Sensor
  sampleCounter += 2;                     // keep track of the time in mS with this variable
  int N = sampleCounter - lastBeatTime;   // monitor the time since the last beat to avoid noise

  //  find the peak and trough of the pulse wave
  if(Signal < thresh && N > (IBI/5)*3)
  {
    // avoid dichrotic noise by waiting 3/5 of last IBI
    if (Signal < T)
    {                        
      // T is the trough
      T = Signal; // keep track of lowest point in pulse wave 
    }
  }

  if(Signal > thresh && Signal > P)
  { 
    // thresh condition helps avoid noise
    P = Signal; // P is the peak
  } // keep track of highest point in pulse wave

  //  NOW IT'S TIME TO LOOK FOR THE HEART BEAT
  // signal surges up in value every time there is a pulse
  if (N > 250)
  {                            
    // avoid high frequency noise
    if ( (Signal > thresh) && (Pulse == false) && (N > (IBI/5)*3) )
    {        
      Pulse = true;                        // set the Pulse flag when we think there is a pulse
      IBI = sampleCounter - lastBeatTime;  // measure time between beats in mS
      lastBeatTime = sampleCounter;        // keep track of time for next pulse

      if(secondBeat)
      {                        
        // if this is the second beat, if secondBeat == TRUE
        secondBeat = false;       // clear secondBeat flag
        for(int i = 0; i <= 9; i++)
        {             
          // seed the running total to get a realisitic BPM at startup
          rate[i] = IBI;                      
        }
      }

      if(firstBeat)
      {                         
        // if it's the first time we found a beat, if firstBeat == TRUE
        firstBeat = false;    // clear firstBeat flag
        secondBeat = true;    // set the second beat flag
        sei();                // enable interrupts again
        return;               // IBI value is unreliable so discard it
      }   

      // keep a running total of the last 10 IBI values
      word runningTotal = 0;  // clear the runningTotal variable    

      for(int i = 0; i <= 8; i++)
      {
        // shift data in the rate array
        rate[i] = rate[i+1];      // and drop the oldest IBI value 
        runningTotal += rate[i];  // add up the 9 oldest IBI values
      }

      rate[9] = IBI;             // add the latest IBI to the rate array
      runningTotal += rate[9];   // add the latest IBI to runningTotal
      runningTotal /= 10;        // average the last 10 IBI values 
      BPM = 60000/runningTotal;  // how many beats can fit into a minute? that's BPM!
      QS = true;                 // set Quantified Self flag 
      // QS FLAG IS NOT CLEARED INSIDE THIS ISR
    }                       
  }

  if (Signal < thresh && Pulse == true)
  {
    // when the values are going down, the beat is over
    Pulse = false;        // reset the Pulse flag so we can do it again
    amp = P - T;          // get amplitude of the pulse wave
    thresh = amp/2 + T;   // set thresh at 50% of the amplitude
    P = thresh;           // reset these for next time
    T = thresh;
  }

  if (N > 2500)
  {
    // if 2.5 seconds go by without a beat
    thresh = 512;                    // set thresh default
    P = 512;                         // set P default
    T = 512;                         // set T default
    lastBeatTime = sampleCounter;    // bring the lastBeatTime up to date        
    firstBeat = true;                // set these to avoid noise
    secondBeat = false;              // when we get the heartbeat back
  }
  sei();  // enable interrupts when youre done!
} // end isr

/**************************************************************************/
/*      Configure the Arduino and start advertising with the radio        */
/**************************************************************************/
void setup(void)
{ 
  Serial.begin(9600);
  
  while(!Serial);   // Leonardo/Micro should wait for serial init
  Serial.println(F("Adafruit Bluefruit Low Energy nRF8001 Callback Echo demo"));

  uart.setRXcallback(rxCallback);
  uart.setACIcallback(aciCallback);
  uart.begin();
  
  interruptSetup();
}

/**************************************************************************/
/*            Constantly checks for new events on the nRF8001             */
/**************************************************************************/
void loop()
{
  uart.pollACI();
}

