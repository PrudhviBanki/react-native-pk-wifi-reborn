//
//  MyWifiModule.m
//  wifi_reborn
//
//  Created by webasto on 10/08/23.
//

#import "MyWifiModule.h"
#import <React/RCTBridgeModule.h>
#import <React/RCTLog.h>
#import <NetworkExtension/NetworkExtension.h>
#import <SystemConfiguration/CaptiveNetwork.h>

@implementation MyWifiModule

RCT_EXPORT_MODULE();


RCT_EXPORT_METHOD(connectToWifi:(NSString*)ssid
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
  
  if (@available(iOS 11.0, *)) {
    NEHotspotConfiguration* configuration = [[NEHotspotConfiguration alloc] initWithSSID:ssid];
    configuration.joinOnce = true;
    
    [[NEHotspotConfigurationManager sharedManager] applyConfiguration:configuration completionHandler:^(NSError * _Nullable error) {
      if (error != nil) {
        reject(@"nehotspot_error", @"Error while configuring WiFi", error);
      } else {
        resolve(nil);
      }
    }];
    
  } else {
    reject(@"ios_error", @"Not supported in iOS<11.0", nil);
  }
}

RCT_EXPORT_METHOD(connectToWifi:(NSString*)ssid
                  withPassphrase:(NSString*)passphrase
                  isWEP:(BOOL)isWEP
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
  
  if (@available(iOS 11.0, *)) {
    NEHotspotConfiguration* configuration;
    if (@available(iOS 11.0, *)) {
      NEHotspotConfiguration* configuration;
      if (isWEP) {
        configuration = [[NEHotspotConfiguration alloc] initWithSSID:ssid passphrase:passphrase isWEP:YES];
      } else {
        configuration = [[NEHotspotConfiguration alloc] initWithSSID:ssid passphrase:passphrase isWEP:NO];
      }
      configuration.joinOnce = true;
      
      [[NEHotspotConfigurationManager sharedManager] applyConfiguration:configuration completionHandler:^(NSError * _Nullable error) {
        if (error != nil) {
          reject(@"nehotspot_error", @"Error while configuring WiFi", error);
        } else {
          resolve(nil);
        }
      }];
      
    } else {
      reject(@"ios_error", @"Not supported in iOS<11.0", nil);
      
    }
  }
}

RCT_EXPORT_METHOD(getAvailableWifiNetworks:(BOOL)includeHiddenNetworks resolver:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
 
  NSMutableArray *availableNetworks = [NSMutableArray array];
  
  NSArray *interfaceNames = (__bridge_transfer id)CNCopySupportedInterfaces();
  for (NSString *interfaceName in interfaceNames) {
    NSDictionary *info = (__bridge_transfer id)CNCopyCurrentNetworkInfo((__bridge CFStringRef)interfaceName);
    NSString *ssid = info[(__bridge NSString *)kCNNetworkInfoKeySSID];
    
    if (ssid) {
      [availableNetworks addObject:ssid];
    }
  }
  
  if (includeHiddenNetworks) {
    // Include additional hidden networks
    [availableNetworks addObjectsFromArray:@[@"HiddenNetwork1", @"HiddenNetwork2"]];
  }
  
  if (availableNetworks.count > 0) {
    NSDictionary *result = @{
      @"networks": availableNetworks,
      @"includeHidden": @(includeHiddenNetworks)
    };
    resolve(result);
  } else {
    NSString *errorCode = @"NO_NETWORKS_FOUND";
    NSString *errorMessage = @"No WiFi networks found";
    NSError *error = [NSError errorWithDomain:errorCode code:0 userInfo:@{ NSLocalizedDescriptionKey: errorMessage }];
    reject(errorCode, errorMessage, error);
  }
}

RCT_EXPORT_METHOD(disconnectFromWifiNetwork:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    // Implement WiFi disconnection logic here
    // Replace this with actual WiFi disconnection code
    
    BOOL disconnectedSuccessfully = YES; // Replace with your actual disconnection logic
    
    if (disconnectedSuccessfully) {
        resolve(@"Disconnected from WiFi successfully");
    } else {
        NSString *errorCode = @"DISCONNECTION_ERROR";
        NSString *errorMessage = @"Failed to disconnect from WiFi";
        NSError *error = [NSError errorWithDomain:errorCode code:0 userInfo:@{ NSLocalizedDescriptionKey: errorMessage }];
        reject(errorCode, errorMessage, error);
    }
}
RCT_EXPORT_METHOD(disconnectFromWifiNetwork:(NSString*)ssid
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
  
  if (@available(iOS 11.0, *)) {
    [[NEHotspotConfigurationManager sharedManager] getConfiguredSSIDsWithCompletionHandler:^(NSArray<NSString *> *ssids) {
      if (ssids != nil && [ssids indexOfObject:ssid] != NSNotFound) {
        [[NEHotspotConfigurationManager sharedManager] removeConfigurationForSSID:ssid];
      }
      resolve(nil);
    }];
  } else {
    reject(@"ios_error", @"Not supported in iOS<11.0", nil);
  }
  
}


@end
