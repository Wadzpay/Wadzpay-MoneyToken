import React from "react"
import { createNativeStackNavigator } from "@react-navigation/native-stack"

import {
  Notification,
  PaymentSuccess,
  Home,
  PaymentSummary,
  User,
  ChangePassword,
  TransactionDetail
} from "~/screens"
import FromNotificationPaymentConfirmationScreen from "~/screens/Menu/Notification/FromNotificationPaymentConfirmationScreen"
import { SendNotificationForRecievingPaymentData, transactionConfirmationData } from "~/api/models"
import TopUpScreen from "~/screens/Home/TopUpScreen"
import RefundScreen from "~/screens/Home/RefundScreen"
import TransactionConfirmationScreenTopUp from "~/screens/TransactionConfirmationScreenTopUp"

export type HomeStackNavigatorParamList = {
  FromNotificationPaymentConfirmationScreen: {
    notificationDataParam?: SendNotificationForRecievingPaymentData
  },
  TransactionConfirmationScreenTopUp: {
    transactionConfirmationDataParam?: transactionConfirmationData
  },
  TopUpScreen: undefined,
  TransactionDetail: {
    transactionId: string;
  };
}
const Stack = createNativeStackNavigator()

const HomeStackNavigator: React.FC = () => {
  return (
    <Stack.Navigator
      initialRouteName="Home"
      screenOptions={{ headerShown: false }}
    >
      <Stack.Screen name="Home" component={Home} />
      <Stack.Screen name="Notification" component={Notification} />
      {/* <Stack.Screen
        name="FromNotificationPaymentConfirmationScreen"
        component={FromNotificationPaymentConfirmationScreen}
      /> */}
 
      <Stack.Screen name="PaymentSuccess" component={PaymentSuccess} />
      <Stack.Screen name="PaymentSummary" component={PaymentSummary} />
      <Stack.Screen name="User" component={User} />
      <Stack.Screen name="ChangePassword" component={ChangePassword} />
      <Stack.Screen name="TransactionDetail" component={TransactionDetail} />
      
    </Stack.Navigator>
  )
}

export default HomeStackNavigator
