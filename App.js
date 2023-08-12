import React from 'react';
import { StyleSheet, Text, View, TextInput, Button, ActivityIndicator, Alert, TouchableOpacity, PermissionsAndroid, Platform, SafeAreaView } from 'react-native';

import RNFetchBlob from 'rn-fetch-blob';
import Permissions from 'react-native-permissions';
import { NativeModules } from 'react-native';

 const { MyWifiModule } = NativeModules;
 console.log(MyWifiModule)

//import MyWifiModule from 'react-native-pk-wifi-manager'


export default class App extends React.Component {

   constructor(props) {
      super(props);

      this.state = {

         wifiSSID: '',
         password: '',
         isLoading: false,
         secureTextEntry: true,
      }
   }
   toggleSecureEntry = () => {
      this.setState(prevState => ({
         secureTextEntry: !prevState.secureTextEntry,
      }));
   };
   render() {
      return (
         <SafeAreaView style={styles.container_safe}>
            <View style={styles.container}>
               <Text style={styles.textView}>Connect your network</Text>
               <TextInput style={styles.input} placeholder="Wifi-SSID"
                  value={this.state.wifiSSID}
                  color="black"
                  onChangeText={(TextInputText) => this.setState({ wifiSSID: TextInputText.toString() })} />
               <TextInput
                  style={styles.input}
                  placeholder="Password"
                  value={this.state.password}
                  secureTextEntry={this.state.secureTextEntry}
                  color="black"
                  onChangeText={(TextInputText) => this.setState({ password: TextInputText.toString() })} />

               <TouchableOpacity style={styles.toggleButton} onPress={this.toggleSecureEntry}>
                  <Text style={styles.toggleButtonText}>{this.state.secureTextEntry ? 'Show' : 'Hide'}</Text>
               </TouchableOpacity>


               <View style={styles.btncon}>

                  {!this.state.isLoading ?
                     <Button
                        onPress={() => this.handlebutton()}
                        title="Connect"
                        color="red"

                     />
                     :
                     null
                  }
                  {this.state.isLoading ?
                     <ActivityIndicator color={"#FF0000"} />
                     :
                     null

                  }

               </View>
            </View>
         </SafeAreaView>
      );
   }

   handlebutton = () => {
      this.requestWifiPermission()
   }

   requestWifiPermission = async () => {
      if (Platform.OS === 'android') {
         try {
            const granted = await PermissionsAndroid.request(
               PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
               {
                  title: 'Wi-Fi Permission',
                  message: 'This app requires access to your Wi-Fi network.',
               }
            );
            if (granted === PermissionsAndroid.RESULTS.GRANTED) {
               console.log('Wi-Fi permission granted');
               this.connectToWifi();
            } else {
               console.log('Wi-Fi permission denied');
            }
         } catch (err) {
            console.log(err);
         }
      } else {
         const response = await Permissions.request('location', {
            type: 'whenInUse',
          });
          
          if (response === 'granted') {
            this.setConnection();
            // Location permission granted, you can now use WiFi-related APIs
          } else {
            this.setConnection();
            console.log('Wi-Fi permission denied');
            // Location permission denied, handle accordingly
          }
      }
   };

   // Connect to Wi-Fi
   connectToWifi = async () => {
      this.setState({ isLoading: true })
      if (this.state.wifiSSID === '' || this.state.password === '') {
         alert("Enter your wifi SSID/Password")
         this.setState({ isLoading: false })
         return true
      } else {
         await MyWifiModule.getAvailableWifiNetworks(true)
            .then(networks => {
               // Handle the list of available Wi-Fi networks
               console.log('Available Wi-Fi networks:', networks);
               const foundNetwork = networks.find(network => network.ssid === this.state.wifiSSID);

               if (foundNetwork) {
                  this.setConnection()
                  console.log('Found network:', foundNetwork);
               } else {
                  console.log(`Network with SSID "${targetSSID}" not found.`);
               }
            })
            .catch(error => {
               // Handle errors
               this.setState({ isLoading: false })
               console.log('Error getting available Wi-Fi networks:', error);
            });

      }

   };

   setConnection = async() => {
     await MyWifiModule.connectToWifi(this.state.wifiSSID, this.state.password,false)
         .then(() => {
            console.log('Connected to WiFi: ', this.state.wifiSSID);
            this.setState({ isLoading: false })
            this.callAPI();
         })
         .catch(error => {
            console.error('Error connecting to WiFi: ', error);
            this.setState({ isLoading: false })
         });
   }
   callAPI = async () => {
      this.setState({ isLoading: true })
      const headers = {
         'Content-Type': 'application/json',
         // Other headers as needed
      };
      RNFetchBlob.config({
         trusty: true
      })
         .fetch('GET', "place your url here", headers)
         .then(response => {
            return response.json()
         })
         .then(responseJson => {
            Alert.alert(
               "Success",
               JSON.stringify(responseJson),
               [
                  {
                     text: "Ok",
                     onPress: () => {
                        this.setState({ isLoading: false })
                        // Handle Ok press if needed
                     },
                  },
               ],
               { cancelable: true }
            );

         })
         .catch(error => {
            console.log(error);
            this.setState({ isLoading: false })
            Alert.alert(
               "Error",
               `An error occurred: ${error.message}`,
               [
                  {
                     text: "Ok",
                     onPress: () => {
                        // Handle Ok press if needed
                     },
                  },
               ],
               { cancelable: true }
            );
         })
   };
}

const styles = StyleSheet.create({
   container: {
      flex: 1,
      backgroundColor: '#fff',
      justifyContent: 'center',
      alignItems: 'center',
      margin: 30
   },
   textView: {
      fontSize: 25,
      color: 'black',
      fontWeight: 'bold'

   },

   container_safe: {
      flex: 1,
      backgroundColor: '#fff',

   },
   input: {
      borderColor: "gray",
      width: "100%",
      borderWidth: 1,
      borderRadius: 10,
      padding: 10,
      marginStart: 30,
      marginEnd: 30,
      marginTop: 20,
      color: "#000000"
   },
   btncon: {
      backgroundColor: '#fff',
      alignItems: 'center',
      justifyContent: 'center',
      margin: 20
   },
   toggleButton: {
      marginTop: 5,
   },
   toggleButtonText: {
      color: 'blue',
   },
});