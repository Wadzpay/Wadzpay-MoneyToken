import { createNativeStackNavigator } from "@react-navigation/native-stack"
import React from "react"

import {
  RequestResetPasswordCode,
  SubmitResetPasswordCode
} from "~/screens/ResetPassword"

export type ResetPasswordStackParamList = {
  SignIn: undefined
  RequestResetPasswordCode: undefined
  SubmitResetPasswordCode: { email: string; newPassword: string }
}

const Stack = createNativeStackNavigator()

const ResetPasswordStackNavigator: React.FC = () => {
  return (
    <Stack.Navigator
      initialRouteName="ResetPassword"
      screenOptions={{ headerShown: false }}
    >
      <Stack.Screen
        name="RequestResetPasswordCode"
        component={RequestResetPasswordCode}
      />
      <Stack.Screen
        name="SubmitResetPasswordCode"
        component={SubmitResetPasswordCode}
      />
    </Stack.Navigator>
  )
}

export default ResetPasswordStackNavigator
