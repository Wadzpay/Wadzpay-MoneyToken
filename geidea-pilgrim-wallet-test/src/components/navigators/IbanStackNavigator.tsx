import React from "react"
import { NativeStackScreenProps } from "@react-navigation/native-stack"
import IbanVerificationScreen from "~/screens/IbanVerificationScreen"



export type IbanStackParamList = {
  IbanVerificationScreen: {
    hideSkip?: boolean
  }
}

export type IbanNavigationProps = NativeStackScreenProps<
IbanStackParamList,
  keyof IbanStackParamList
>

const IbanStackNavigator: Record<
  keyof IbanStackParamList,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  React.FC<any>
> = {
  IbanVerificationScreen : IbanVerificationScreen
}

export default IbanStackNavigator
