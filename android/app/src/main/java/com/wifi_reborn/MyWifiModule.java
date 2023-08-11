package com.wifi_reborn;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.List;

public class MyWifiModule extends ReactContextBaseJavaModule {

    WifiManager wifiManager;
    ReactApplicationContext reactContext;
    ConnectivityManager connectivityManager;
    WifiScanReceiver wifiScanReceiver;

    private BroadcastReceiver wifiStateReceiver;

    ConnectivityManager.NetworkCallback networkCallback;

    public MyWifiModule(ReactApplicationContext reactContext) {
        super(reactContext);
        wifiManager = (WifiManager) reactContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getReactApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "MyWifiModule";
    }

    @ReactMethod
    public void connectToWifi(String ssid, String password,boolean isWap, final Promise promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

//            WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
//                    .setSsid(ssid)
//                    .setWpa2Passphrase(password)
//                    .build();
//            // connectivityManager.requestNetwork(request, networkCallback);
//            List<WifiNetworkSuggestion> suggestionsList = new ArrayList<>();
//            suggestionsList.add(suggestion);
//
//            // Register the suggestions with the system
//            int status = wifiManager.addNetworkSuggestions(suggestionsList);
//
//            if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
//                promise.resolve("Connected to Wi-Fi network: " + ssid);
//            } else {
//                promise.reject("CONNECT_FAIL", "Failed to connect to Wi-Fi network: " + ssid);
//            }
            WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password)
                    .build();

            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(specifier)
                    .build();

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    try {
                        connectivityManager.bindProcessToNetwork(network);
                        promise.resolve("Connected to Wi-Fi network:");
                    } catch (SecurityException e) {
                        promise.reject("BIND_FAIL", "Failed to bind process to network: " + e.getMessage());
                    }
                }
                @Override
                public void onLost(Network network) {
                    promise.resolve("Disconnected from Wi-Fi network: ");
                }
                @Override
                public void onUnavailable() {
                    promise.reject("CONNECT_FAIL", "Failed to connect to Wi-Fi network: ");
                }
            };
             connectivityManager.requestNetwork(request, networkCallback);
        } else {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = "\"" + ssid + "\"";
            wifiConfig.preSharedKey = "\"" + password + "\"";
            Handler handler = new Handler(getReactApplicationContext().getMainLooper());
            handler.post(() -> {
                int networkId = wifiManager.addNetwork(wifiConfig);
                if (networkId != -1) {
                    boolean success = wifiManager.enableNetwork(networkId, true);
                    wifiManager.reconnect();
                    if (success) {
                        promise.resolve("Connected to Wi-Fi network: " + ssid);
                    } else {
                        promise.reject("CONNECT_FAIL", "Failed to connect to Wi-Fi network: " + ssid);
                    }
                }
            });
        }

    }
    public static class WifiScanReceiver extends BroadcastReceiver {
        private final Runnable onScanComplete;

        public WifiScanReceiver(Runnable onScanComplete) {
            this.onScanComplete = onScanComplete;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                // Call the provided callback when scan results are available
                if (onScanComplete != null) {
                    onScanComplete.run();
                }
            }
        }
    }
    @ReactMethod
    public void getAvailableWifiNetworks(boolean rescan, Promise promise) {
        try {
            if (rescan) {
                wifiScanReceiver= new WifiScanReceiver(() -> {
                    reactContext.unregisterReceiver(wifiScanReceiver);
                    if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    WritableArray networks = mapScanResultsToJS(scanResults);
                    promise.resolve(networks);
                });

                if (!wifiManager.startScan()) {
                    promise.reject("failure");
                    return;
                }
                reactContext.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            } else {
                if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                List<ScanResult> scanResults = wifiManager.getScanResults();
                WritableArray networks = mapScanResultsToJS(scanResults);
                promise.resolve(networks);
            }
        } catch (Exception e) {
            promise.reject(e);
            e.printStackTrace();
        }
    }
    private WritableArray mapScanResultsToJS(List<ScanResult> scanResults) {
        WritableArray wifiArray = new WritableNativeArray();

        for (ScanResult result : scanResults) {
            WritableNativeMap wifiObject = new WritableNativeMap();
            wifiObject.putString("ssid", result.SSID);
            wifiObject.putString("bssid", result.BSSID);
            wifiObject.putString("capabilities", result.capabilities);
            wifiObject.putInt("frequency", result.frequency);
            wifiObject.putInt("level", result.level);
            wifiObject.putDouble("timestamp", (double) result.timestamp);
            wifiArray.pushMap(wifiObject);
        }

        return wifiArray;
    }
    public  boolean isConnected(ScanResult scanResult) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo != null && wifiInfo.getSSID().equals("\"" + scanResult.SSID + "\"");
    }
    @ReactMethod
    public void disableWifi() {
        Handler handler = new Handler(getReactApplicationContext().getMainLooper());
        handler.post(() -> {
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
        });
    }

    @ReactMethod
    public void enableWifi() {
        Handler handler = new Handler(getReactApplicationContext().getMainLooper());
        handler.post(() -> {
            if (wifiManager != null && !wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        });
    }

    @ReactMethod
    private void disconnectWifiNetwork(String ssid, final Promise promise, WifiManager wifiManager) {
        if (ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configuredNetworks) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                boolean success = wifiManager.removeNetwork(config.networkId);
                wifiManager.saveConfiguration(); // Save the updated configuration

                if (success) {
                    promise.resolve("Disconnected from Wi-Fi network: " + ssid);
                } else {
                    promise.reject("DISCONNECT_FAIL", "Failed to disconnect from Wi-Fi network: " + ssid);
                }
                return;
            }
        }

        promise.reject("NETWORK_NOT_FOUND", "Wi-Fi network not found: " + ssid);
    }
    public void disconnectFromWifiNetwork() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            networkCallback = null;
        }
    }
}