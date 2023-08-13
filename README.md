# react-native-pk-wifi-reborn

This npm supports both Android and iOS.

npm link : https://www.npmjs.com/package/react-native-pk-wifi-manager

## Getting started

### Mostly automatic installation

        `$ npm i react-native-pk-wifi-manager`                               

  or

        `$ yarn add react-native-pk-wifi-manager` 

## Android

#ACCESS_FINE_LOCATION permission: 
You must request the ACCESS_FINE_LOCATION permission at runtime to use the device's Wi-Fi scanning and managing capabilities. In order to accomplish this, you can use the PermissionsAndroid API or React Native Permissions.

## iOS

#install pod  
Add Hotspot,Location permissons in your app  

## Usage

```javascript
import MyWifiModule from 'react-native-pk-wifi-manager';


//Scan list of wifi
MyWifiModule.getAvailableWifiNetworks(true)
.then(networks => {
              
})
.catch(error => {
              
});

//connect with ssid and password method (isWep for iOS=true optional for Android)
MyWifiModule.connectToWifi('ssid', 'password','isWep')
.then(() => {
         
})
.catch(error => {
           
});

//Disconnect wifi method
MyWifiModule.disconnectFromWifiNetwork.then(() => {
         
})
.catch(error => {
           
});
```
