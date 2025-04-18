import messaging from '@react-native-firebase/messaging';
import { Alert, Platform } from 'react-native';
import {PermissionsAndroid} from 'react-native';

export async function requestUserPermission() {

    if(Platform.OS === 'ios') {
        const authStatus = await messaging().requestPermission();
        const enabled =
          authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
          authStatus === messaging.AuthorizationStatus.PROVISIONAL;
        if (enabled) {
          //('Authorization status:', authStatus);
          getFcmToken()
        }
    } else {
        PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);
        console.log("here before fcm token")
       await getFcmToken()
    }
}
    const getFcmToken = async () => {
        try {
            await messaging().registerDeviceForRemoteMessages();
            // Get the token
            const token = await messaging().getToken();
            console.warn("token ", token)
            Alert.alert("token " , token)
        } catch (error) {
            console.log("error in FCM ", error)
        }       
    }

 
