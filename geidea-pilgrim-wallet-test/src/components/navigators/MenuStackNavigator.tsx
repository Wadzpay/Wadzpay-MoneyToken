import React from "react"
import { createNativeStackNavigator } from "@react-navigation/native-stack"

import {
  Menu,
  User,
  Contacts,
  SendViaQRCode,
  Settings,
  ChangePassword,
  PaymentSummary,
  PaymentSuccess,
  IbanVerificationScreen,
  VerifyPhoneOtp,
  OTPInput,
  PasscodeScreen
} from "~/screens"
import Notification from "~/screens/Menu/Notification"
import FromNotificationPaymentConfirmationScreen from "~/screens/Menu/Notification/FromNotificationPaymentConfirmationScreen"
import SellScreen from "~/screens/Menu/SellScreen"
import BuyScreen from "~/screens/Menu/BuyScreen"
import DepositAndWithdrawScreen from "~/screens/DepositAndWithdrawScreen"
import TransactionConfirmationScreen from "~/screens/TransactionConfirmationScreen"
import TransactionSuccessFailureScreen from "~/screens/TransactionSuccessFailureScreen"
import { string } from "yup"
import BuySellReciptScreen from "~/screens/Menu/BuySellReciptScreen"
import VerifyPhoneCode from "~/screens/Onboarding/VerifyPhoneCode"


type MenuScreenParams = {
  modalTypeOpen?: "deposit" | "withdraw" | "buy" | "sell"
}
export type VerifyCodeScreenType = "phone" | "email"

type VerifyCodeProps = {
  screenType: VerifyCodeScreenType
  target: string
}


export type BuySellReciptScreenParam = {
  title: string
  uuid : string
  transactionType : string
  from: string
  to: string
  totalAmount: string
  createdAt: string
  status: string
  description: string
  statusHeader: string
}

export type TransactionSuccessFailureParams = {
  transactionId: string
  transactionType: string
  bankAccountNumber: string
  userEmail: string
  fiatAmount: string
  createdAt: string
  status: string
}

export type MenuStackParamList = {
  Menu: MenuScreenParams
  User: undefined
  OTPInput: undefined
  ChangePassword: undefined
  Contacts: undefined
  Settings: undefined
  IbanVerificationScreen: undefined
  DepositAndWithdrawScreen: {
    screenType?: string
    asset?: string
  }
  TransactionConfirmationScreen: {
    bankAccountNumber: string
    userEmail: string
    amount: string
    fiatType: string
    transactionType: string
  }
  VerifyPhoneOtp: VerifyCodeProps
  BuySellReciptScreen:BuySellReciptScreenParam
  PasscodeScreen: undefined
}

const Stack = createNativeStackNavigator()

const MenuStackNavigator: React.FC = () => {
  return (
    <Stack.Navigator
      initialRouteName="Menu"
      screenOptions={{ headerShown: false }}
    >
      {/* <Stack.Screen name="IbanVerificationScreen" component={IbanVerificationScreen} />  */}
     <Stack.Screen name="Menu" component={Menu} />
      <Stack.Screen name="User" component={User} />
      <Stack.Screen name="VerifyPhoneOtp" component={VerifyPhoneOtp} />
      <Stack.Screen name="OTPInput" component={OTPInput} />
      <Stack.Screen name="PasscodeScreen" component={PasscodeScreen} />
      <Stack.Screen name="Contacts" component={Contacts} />
      <Stack.Screen name="SendViaQRCode" component={SendViaQRCode} />
      {/* <Stack.Screen
        name="DepositAndWithdrawScreen"
        component={DepositAndWithdrawScreen}
      /> */}
      {/* <Stack.Screen
        name="TransactionConfirmationScreen"
        component={TransactionConfirmationScreen}
      />
      <Stack.Screen
        name="TransactionSuccessFailureScreen"
        component={TransactionSuccessFailureScreen}
      /> */}
      {/* <Stack.Screen name="ChangePassword" component={ChangePassword} /> */}
      {/* <Stack.Screen name="Settings" component={Settings} />
      <Stack.Screen name="SellScreen" component={SellScreen} />
      <Stack.Screen name="BuyScreen" component={BuyScreen} />
      <Stack.Screen name="BuySellReciptScreen" component={BuySellReciptScreen} /> */}

    </Stack.Navigator>
  )
}

export default MenuStackNavigator
