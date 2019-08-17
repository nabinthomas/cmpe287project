
# CMPE 287 Project - Android Network Monitor

## Overview 
This is an app which tracks the network bandwidth usage on Android devices. 

## Instructions to build/use

### Prerequisites

1. Android Studio with Pixel 2 (Android Pie, API level 28) Emulator, or equivalent hardware to run the app. 
2. Network Connectivity for the device (or simulator)
3. Apps using network should be present (example youtube). 

### Steps to use in emulator

1. Clone the repository 
  <pre>
  git clone https://github.com/nabinthomas/cmpe287project.git
  </pre>
2. A Directory named cmpe287project will be created and the code will be downloaded. 
2. Open Android Studio and open the above folder to open the project. 
3. Build and run using the emulator
4. First time when the App starts, it request for Usage Monitor Access. This is required for the app to monitor Network data usage by each App. 
5. If you have some apps running in background which uses network, the current upload and download speed will be shown on the Main Activity. 
6. Selecting an App in the main screen will show the network usage by the app. 
7. When the first time the App detail screen is opened, App will request permission for Phone access. App will not make phone calls, but this permission is required to use the APIs to access detailed network usage. 
