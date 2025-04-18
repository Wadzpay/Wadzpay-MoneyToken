import React from "react"
import { NativeStackScreenProps } from "@react-navigation/native-stack"


import {
  CreateAccount,
  AccountDetails,
  Success,
  PersonalDetails,
  EnableNotifications,
  IbanVerificationScreen
} from "~/screens"
import VerifyEmailCode from "~/screens/Onboarding/VerifyEmailCode"
import VerifyPhoneCode from "~/screens/Onboarding/VerifyPhoneCode"

export type VerifyCodeScreenType = "phone" | "email"

type VerifyCodeProps = {
  screenType: VerifyCodeScreenType
  target: string
}

export type OnboardingStackParamList = {
  CreateAccount: undefined
  VerifyPhoneCode: VerifyCodeProps
  VerifyEmailCode: VerifyCodeProps
  AccountDetails: undefined
  Success: undefined
  PersonalDetails: undefined
  EnableNotifications: undefined
  IbanVerificationScreen: undefined
}

export type OnboardingNavigationProps = NativeStackScreenProps<
  OnboardingStackParamList,
  keyof OnboardingStackParamList
>

const OnboardingStackNavigator: Record<
  keyof OnboardingStackParamList,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  React.FC<any>
> = {
  CreateAccount: CreateAccount,
  VerifyPhoneCode: VerifyPhoneCode,
  VerifyEmailCode: VerifyEmailCode,
  AccountDetails: AccountDetails,
  Success: Success,
  PersonalDetails: PersonalDetails,
  EnableNotifications: EnableNotifications,
  IbanVerificationScreen: IbanVerificationScreen
}

export default OnboardingStackNavigator
