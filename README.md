# 4th-YearEngProject

### Monitoring of Arduino-based PPG and GSR Signals through an Android Device


The following is a fourth-year engineering project at Carleton University, for the study period 2015 – 2016...


#### Abstract

  The purpose of this project is to provide reliable heart rate readings, and to monitor the influence of stress on the heart rate. The group developed a system that monitors and processes PhotoPlethysmoGraphic (PPG) and Galvanic Skin Response (GSR) signals. A pulse/PPG sensor is used to estimate a person’s heart rate, and a GSR sensor is used to detect the person’s skin conductance. The Arduino microcontroller processes the estimated heart rate and detected skin conductance to obtain both PPG and GSR waveforms. The Arduino microcontroller is paired with an Android device via a Bluetooth Low Energy (BLE) wireless connection, to monitor the signals received from the Arduino microcontroller. Such a system could be used in hospitals, for bedside care, or by athletes, students, and so on.



#### Background

  Medical and engineering worlds are highly related nowadays. Most of the technologies used in hospitals are established by engineers. Despite the large number of existing technologies, estimating reliable heart rate readings has always been an issue in the medical world. There are many technologies that have been developed to estimate heart rate, but they are not very reliable and rather expensive. Nonetheless, there are many factors that could affect someone’s heart rate such as motion, emotions, stress, and so on. Such technologies introduced problems related to reliability, complexity, flexibility, mobility, portability, power consumption, and costs.
  
  Recent studies have shown that changes in the levels of biological stress influence the heart rate. In fact, the pumping process of the heart and in turn the blood flow throughout the body may be affected by changes in physiological stress. One way to measure the heart rate of a person is through analyzing a PPG signal. PPG is a procedure that determines the change in Blood Volume Pulse (usually in the soft tissues of the body) using the direct relationship between variations in volume and the absorption, reflection, and scattering of the light from a photo-emitter and then recorded by the photoreceptor. The changes in the pulsatile flow of the arterial blood are then represented in a PPG waveform, which can then be used to extract useful information such as a person’s heart rate. The modern way of estimating physiological stress is through the changes in skin conductance. GSR is a technique used to measure the changes in the skin conductance, usually to indicate an estimate of the level of biological stress.
  
  Both PPG and GSR sensors can be used to indicate how stress affects the heart rate by a simultaneous analysis of their corresponding waveforms.
  
  
  
#### Description

  A PPG sensor and a GSR sensor are interfacing with the 3.3/5V Arduino microcontroller to acquire the required signals. The GSR sensor is used for detecting the stress level, while the PPG sensor is used for estimating the heart rate. An Android application is developed to monitor the PPG and the GSR signals. The Arduino microcontroller interacts with the Android device through a wireless serial BLE connection. Standard algorithms for deriving heart rate from PPG signals and for deriving stress levels from GSR signals are available. These algorithms are adapted to the signals acquired using the Arduino microcontroller, and converted into the Android application.

  The project is divided into two parts, the Arduino circuitry and the Android application. Hence, the system consists of two subsystems; the first subsystem is composed of an Arduino microcontroller and two sensors (i.e. PPG and GSR sensors), while the second subsystem is composed of an Android monitoring application. Generally speaking, all of the acquiring and processing of signals will be done within the Arduino microcontroller subsystem. The Android device is simply a receiver and a sender. It receives the final results including heart rate, stress level, and the related signals as it sends the start/stop command for the acquisition of each signal to the Arduino microcontroller. The inter-communication between the Arduino microcontroller and the Android device is established by a BLE connection. The figure below shows a higher-level diagram of the system.
  
  ![alt tag](https://github.com/Itaf/4th-YearEngProject/blob/master/TheBigPicture.png)
  
  The two sensors are connected to the Grove shield that is mounted on top of the Arduino microcontroller. The GSR sensor that is used has Grove connectors, thus a Grove shield is needed for pairing it with the Arduino microcontroller. The Grove shield identifies the corresponding pins of the Arduino microcontroller, and allows the connection of additional sensors and/or other shields.
