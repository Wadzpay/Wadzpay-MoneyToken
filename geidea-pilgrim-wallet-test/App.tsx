import 'react-native-get-random-values'
import React , { useEffect, useState }from 'react';
import {useColorScheme} from 'react-native';
import {QueryClient, QueryClientProvider} from 'react-query';
import {
  EnvironmentContextProvider,
  NotificationsContextProvider,
  OnboardingContextProvider,
} from './src/context';
import {UserContextProvider} from './src/context';
import {
  NavigationContainer,
  useNavigation,
  useNavigationContainerRef,
} from '@react-navigation/native';

import {LogBox} from 'react-native';
import {RootStackNavigator} from '~/components/navigators';
import { requestUserPermission } from '~/helper/NotificationService';
import messaging from '@react-native-firebase/messaging';
import { Alert } from "react-native";

const queryClient = new QueryClient();


function App(): JSX.Element {
  const [splashLoader, setSplashLoader] = useState(true)
  //const isDarkMode = useColorScheme() === 'dark'; TODO: Nandani to implement in settings page
  LogBox.ignoreLogs(['new NativeEventEmitter']); // Ignore log notification by message
  LogBox.ignoreAllLogs(); //Ignore all log notifications
  LogBox.ignoreLogs(['EventEmitter.removeListener']);

// useEffect(async () => {
//  await requestUserPermission()
// }, [])

// useEffect(() => {
//   const unsubscribe = messaging().onMessage(async remoteMessage => {
//     Alert.alert('A new FCM message arrived!', JSON.stringify(remoteMessage));
//   });

//   return unsubscribe;
// }, []);

useEffect(() => {
  setTimeout(() => {
    setSplashLoader(false)
  }, 500)

 //return splashTimer;
}, []);

  return (
    <QueryClientProvider client={queryClient}>
      <EnvironmentContextProvider>
        <UserContextProvider>
          <NavigationContainer>
            <OnboardingContextProvider>
              <RootStackNavigator  loading={splashLoader} />
            </OnboardingContextProvider>
          </NavigationContainer>
        </UserContextProvider>
      </EnvironmentContextProvider>
    </QueryClientProvider>
  );
}

export default App;
