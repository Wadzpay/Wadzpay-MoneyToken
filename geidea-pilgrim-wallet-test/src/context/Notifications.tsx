// import AsyncStorage from "@react-native-async-storage/async-storage"
 import React, { PropsWithChildren, useEffect, useRef, useState } from "react"

import messaging from '@react-native-firebase/messaging';
import { Alert } from "react-native";

// import * as Notifications from "expo-notifications"
// import {
//   NavigationContainer,
//   NavigationContext,
//   useNavigation
// } from "@react-navigation/native"
// import { SendNotificationForRecievingPaymentData } from "~/api/models"
// // import { RootStackNavigator } from "~/components/navigators"

 type FirebasePushToken = string | null

type NotificationsContextType = {
    fireBasePushToken: FirebasePushToken
//   setExpoPushToken: (value: string) => Promise<boolean>
//   removeExpoPushToken: () => Promise<boolean>
//   isNotificationRecieved: boolean
//   notifCount: number
//   setNotiCount: (value: number) => void
//   removeNotificationRecieved: (value: boolean) => void
//   notificationData: SendNotificationForRecievingPaymentData
//   setNotificationData:() => void
//   isNotificationEnabled: boolean
//   setIsNotificationEnabled: (value: boolean) => void
//   isTransactionNotificationRecieved: boolean
//   removeTransactionNotificationRecieved: (value: boolean) => void
//   setTransactionUuid: (uuid:string) => void
//   getTransactionUuid : string

//   notificationPaymentData: SendNotificationForRecievingPaymentData,
//   setNotificationPaymentObject: () => void,
//   isPaymentNotificationRecieved: boolean,
//   removePaymentNotificationRecieved: (value: boolean) => void
}

export const NotificationsContext =
  React.createContext<NotificationsContextType>({
    fireBasePushToken: null,
    // setExpoPushToken: () => Promise.resolve(false),
    // removeExpoPushToken: () => Promise.resolve(false),
    // isNotificationRecieved: false,
    // removeNotificationRecieved: () => {
    //   false
    // },
    // notificationData: {},
    // notifCount: 0,
    // setNotiCount: () => {},
    // setNotificationData: () => {},
    // isNotificationEnabled: false,
    // setIsNotificationEnabled: () => {
    //   false
    // },
    // isTransactionNotificationRecieved: false,
    // removeTransactionNotificationRecieved: () => {
    // false
    // },
    // setTransactionUuid: () => {},
    // getTransactionUuid: "",
    // notificationPaymentData: {},
    // setNotificationPaymentObject: () => {},
    // isPaymentNotificationRecieved: false,
    // removePaymentNotificationRecieved: () => {
    // false
    // }
  })

type Props = PropsWithChildren<{}>

export const NotificationsContextProvider: React.FC<Props> = ({
  children
}: Props) => {
  const [fcmToken, setFcmToken] = useState<FirebasePushToken>(null)

  useEffect(()=>{
    onAppBootstrap()
  },[fcmToken])

  async function onAppBootstrap() {
    // Register the device with FCM
    await messaging().registerDeviceForRemoteMessages();
  
    // Get the token
    const token = await messaging().getToken();
    setFcmToken(token)
  
    //console.log("token ", token)
    // Save the token
   // await postToApi('/users/1234/tokens', { token });
  }


  useEffect(() => {
    const unsubscribe = messaging().onMessage(async remoteMessage => {
      Alert.alert('A new FCM message arrived!', JSON.stringify(remoteMessage));
    });

    return unsubscribe;
  }, []);
 

//   const [notificationData, setNotification] = useState({})
//   const [notificationPaymentData, setNotificationPaymentData] = useState({})

//   const [notifCount, setNotifCount] = useState(0)
//   const [isNotificationRecieved, setIsNotificationRecieved] = useState(false)
//   const [isTransactionNotificationRecieved, setIsTransactionNotificationRecieved] = useState(false)
//   const [isPaymentNotificationRecieved, setIsPaymentNotificationRecieved] = useState(false)
//   const [getTransactionUuid, setTransactionUuidInState] = useState("")
//   const [isNotificationEnabled, setIsNotificationEnabledState] = useState(false)

// useEffect(() => {
//     const getExpoPushTokenAsync = async () => {
//       setToken(await AsyncStorage.getItem("expoPushToken"))
//     }
//     getExpoPushTokenAsync()
//     Notifications.addNotificationResponseReceivedListener(
//       _handleNotificationResponse
//     )
//     return () =>
//       Notifications.addNotificationResponseReceivedListener(
//         _handleNotificationResponse
//       ).remove()
//   }, [])

 // const navigation = React.useContext(NavigationContext);

//  const setTransactionUuid = (uuid: string) => {
//   console.log("setTransactionUuid ", uuid)
//   setTransactionUuidInState(uuid)
//   console.log("getTransactionUuid ", getTransactionUuid)
  
//  }

//   const setIsNotificationEnabled = () => {
//     setIsNotificationEnabledState(true)
//   }

//   const setNotiCount = (count: number) => {
//     setNotifCount(count)
//   }
//   const setNotificationData = () => {
//     setNotification({})
//   }
//   const setNotificationPaymentObject = () => {
//     setNotificationPaymentData({})
//   }
  

//    const _handleNotificationResponse = (response: any) => {
//     console.log("&&**&&", response)
//     if(Object.keys(response.notification?.request.content.data).length !== 0) {
//       if(response.notification?.request.content.data.uuid && !response.notification?.request?.content?.data.amount) {
//         // Notification for transaction detail page
//         setTransactionUuid(response.notification?.request.content.data.uuid)
//         setIsTransactionNotificationRecieved(true)

//       }  
//       else {
//         // Notification for recive payment request
//         console.log("********")
//         setNotification(response.notification?.request.content.data)
//         setIsNotificationRecieved(true)
//         setNotiCount(notifCount + 1)
//       }
     
//     }  else if(Object.keys(response.notification?.request?.content?.title).includes("Your KYC verification")) {
//       setIsTransactionNotificationRecieved(false)
//       setIsPaymentNotificationRecieved(false)
//     }
//    // console.log("isNotificationRecieved ", isNotificationRecieved)
//   }


//   const setExpoPushToken: (value: string) => Promise<boolean> = async (
//     value
//   ) => {
//     try {
//       await AsyncStorage.setItem("expoPushToken", value)
//       setToken(value)
//       return true
//     } catch (e) {
//       // eslint-disable-next-line no-console
//       console.error("Error saving expo push token to async storage.")
//       return false
//     }
//   }

//   const removeExpoPushToken: () => Promise<boolean> = async () => {
//     try {
//       await AsyncStorage.removeItem("expoPushToken")
//       setToken(null)
//       return true
//     } catch (e) {
//       // eslint-disable-next-line no-console
//       console.error("Error removing expo push token to async storage.")
//       return false
//     }
//   }

//   const removeNotificationRecieved = (value: any) => {
//     setIsNotificationRecieved(value)
//   }
//   const removeTransactionNotificationRecieved = (value: any) => {
//     setIsTransactionNotificationRecieved(value)
//   }

//   const removePaymentNotificationRecieved = (value: any) => {
//     setIsPaymentNotificationRecieved(value)
//   }
  return (
    <NotificationsContext.Provider
      value={{
        fireBasePushToken: fcmToken,
        // setExpoPushToken,
        // removeExpoPushToken,
        // isNotificationRecieved,
        // removeNotificationRecieved,
        // notificationData,
        // notifCount,
        // setNotiCount,
        // isNotificationEnabled,
        // setIsNotificationEnabled,
        // setNotificationData,
        // isTransactionNotificationRecieved,
        // removeTransactionNotificationRecieved,
        // setTransactionUuid,
        // getTransactionUuid,
        // notificationPaymentData,
        // setNotificationPaymentObject,
        // isPaymentNotificationRecieved,
        // removePaymentNotificationRecieved
      }}
    >
      {children}
    </NotificationsContext.Provider>
  )
}