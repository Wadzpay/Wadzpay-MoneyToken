#import "AppDelegate.h"
#import <React/RCTBundleURLProvider.h>
#import <Firebase.h>
#import <UserNotifications/UserNotifications.h>

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  self.moduleName = @"WadzpayPilgrimWallet";
  [FIRApp configure];
  // You can add your custom initial props in the dictionary below.
  // They will be passed down to the ViewController used by React Native.
  self.initialProps = @{};
  
  [self requestPushNotificationPermissions];


  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

- (void)requestPushNotificationPermissions
{
  // iOS 10+
  UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
  [center getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {
    
    switch (settings.authorizationStatus)
    {
      // User hasn't accepted or rejected permissions yet. This block shows the allow/deny dialog
      case UNAuthorizationStatusNotDetermined:
      {
//        center.delegate = self;
        [center requestAuthorizationWithOptions:(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge) completionHandler:^(BOOL granted, NSError * _Nullable error)
         {
           if(granted)
           {
             [[UIApplication sharedApplication] registerForRemoteNotifications];
           }
           else
           {
             // notify user to enable push notification permission in settings
           }
         }];
        break;
      }
      // the user has denied the permission
      case UNAuthorizationStatusDenied:
      {
        // notify user to enable push notification permission in settings
        break;
      }
      // the user has accepted; Register a PN token
      case UNAuthorizationStatusAuthorized:
      {
        [[UIApplication sharedApplication] registerForRemoteNotifications];
        break;
      }
      default:
        break;
    }
  }];
}

- (void)application:(UIApplication*)app didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)devToken
{
  // parse token bytes to string
  const char *data = (const char *)[devToken bytes];
  NSMutableString *token = [NSMutableString string];
  for (NSUInteger i = 0; i < [devToken length]; i++)
  {
    [token appendFormat:@"%02.2hhX", data[i]];
  }
  
  // print the token in the console.
  NSLog(@"Push Notification Token: %@", [token copy]);
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
  // could not register a Push Notification token at this time.
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler
{
  // app has received a push notification
}
- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index"];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
}

/// This method controls whether the `concurrentRoot`feature of React18 is turned on or off.
///
/// @see: https://reactjs.org/blog/2022/03/29/react-v18.html
/// @note: This requires to be rendering on Fabric (i.e. on the New Architecture).
/// @return: `true` if the `concurrentRoot` feature is enabled. Otherwise, it returns `false`.
- (BOOL)concurrentRootEnabled
{
  return true;
}

@end
