import React, { useContext, useEffect, useRef, useState } from "react"
import { createStackNavigator, StackScreenProps } from "@react-navigation/stack"

import OnboardingStackNavigator, {
  OnboardingStackParamList
} from "./OnboardingStackNavigator"
import AppTabNavigator, { AppTabNavigatorParamList } from "./AppTabNavigator"
import { MenuStackParamList } from "./MenuStackNavigator"
import { TransactionStackNavigatorParamList } from "./TransactionStackNavigator"
import { SendFundsStackNavigatorParamList } from "./SendFundsStackNavigator"
import ResetPasswordStackNavigator, {
  ResetPasswordStackParamList
} from "./ResetPasswordStackNavigator"

import {
  Welcome,
  EnableNotifications,
  SignIn,
  DevelopmentSettings,
  Loading,
  kycJumioWebviewScreen
} from "~/screens"
import { NotificationsContext, UserContext } from "~/context"
import { isDev } from "~/utils"
import { useSignOut, useUser, useUserKYC } from "~/api/user"
import { Alert, AppState } from "react-native"
import IbanVerificationScreen from "~/screens/IbanVerificationScreen"
import TransactionConfirmationScreenTopUp from "~/screens/TransactionConfirmationScreenTopUp"
import TopUpScreen from "~/screens/Home/TopUpScreen"
import RefundScreen from "~/screens/Home/RefundScreen"

export type RootStackParamList = {
  Welcome: undefined
  Home: undefined
  EnableNotifications: undefined
  SignIn: undefined
  SuccessResetPassword: undefined
  DevelopmentSettings: undefined
  IbanVerificationScreen: undefined
  Loading: undefined
} & OnboardingStackParamList &
  ResetPasswordStackParamList &
  AppTabNavigatorParamList &
  MenuStackParamList &
  TransactionStackNavigatorParamList &
  SendFundsStackNavigatorParamList

export type RootNavigationProps = StackScreenProps<
  RootStackParamList,
  keyof RootStackParamList
>

const Stack = createStackNavigator()

type Props = {
  loading: boolean
}

const RootStackNavigator: React.FC<Props> = ({ loading }: Props) => {
  const {
    user,
    isLoading: isUserLoading,
    isUserKycApproved,
    isLoadingSignupPage,
    bankAccountNumber,
    countryCode,
    isIbanSkipped,
    reLoadSignUpPage
  } = useContext(UserContext)
  //const { expoPushToken } = useContext(NotificationsContext)
  const [userApproved, setUserApproved] = useState(false)
  const [kycUserUrl, setKycUserUrl] = useState("")
  const [showLauncher, setShowLauncher] = useState(false)


  // useEffect(() => {
  //   if (!userApproved) reLoadSignUpPage()
  // }, [])

  useEffect(() => {
    if (!userApproved) reLoadSignUpPage()
  }, [])

 // console.log("user ", user, "isUserKycApproved", isUserKycApproved)

 return (
  <Stack.Navigator
    initialRouteName={"Loading"}
    screenOptions={{ headerShown: false }}
  >
    {isUserLoading || loading ? (
       <Stack.Screen name="Loading" component={Loading} />
     //<Stack.Screen name="EnableNotifications" component={EnableNotifications} />
    ) : // : user && !userApproved && kycUserUrl.length > 0 ? // TODO allow user to do KYC when signed in
    // <>

    //   </>
    user && isUserKycApproved ? (
      // Show enable notifications screen if the token was not set
      // Don't show enable notifications screen on simulator/emulator
      <>
        <Stack.Screen
          name="AppTabNavigator"
          component={AppTabNavigator}
          options={{ headerShown: false }}
        />
        <Stack.Screen name="TransactionConfirmationScreenTopUp" component={TransactionConfirmationScreenTopUp} />
        <Stack.Screen name="TopUpScreen" component={TopUpScreen} />
      <Stack.Screen name="RefundScreen" component={RefundScreen} />
        {!isDev && (
          <Stack.Screen
            name="DevelopmentSettings"
            component={DevelopmentSettings}
            options={{ headerShown: true }}
          />
        )}
      </>
    ) : (
      <>
        <Stack.Screen name="Welcome" component={Welcome} />
        <Stack.Screen name="SignIn" component={SignIn} />
        <Stack.Screen
          name="ResetPassword"
          component={ResetPasswordStackNavigator}
        />
        <Stack.Screen
          name="kycJumioWebviewScreen"
          component={kycJumioWebviewScreen}
        />
        {isDev && (
          <Stack.Screen
            name="DevelopmentSettings"
            component={DevelopmentSettings}
            options={{ headerShown: true }}
          />
        )}
        {Object.entries(OnboardingStackNavigator).map(([name, component]) => (
          <Stack.Screen key={name} name={name} component={component} />
        ))}
      </>
    )}
  </Stack.Navigator>
)
}
export default RootStackNavigator