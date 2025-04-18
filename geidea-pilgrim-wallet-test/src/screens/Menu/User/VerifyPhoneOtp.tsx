import React, { useContext } from "react"
import { NativeStackScreenProps } from "@react-navigation/native-stack"

import { useTranslation } from "react-i18next"

import { MenuStackParamList, OnboardingStackParamList } from "~/components/navigators"
import { OnboardingContext } from "~/context"
import { useSendPhoneOTP, useSendPhoneOtpCommon, useVerifyPhoneOTP, useVerifyPhoneOtpCommon } from "~/api/otp"
import VerifyOtpCodeScreen from "./VerifyOtpCodeScreen"

type Props = NativeStackScreenProps<MenuStackParamList, "VerifyPhoneOtp">

const VerifyPhoneOtp: React.FC<Props> = ({ navigation, route }: Props) => {
  const { target } = route.params
  const { t } = useTranslation()
  const { onboardingValues } = useContext(OnboardingContext)

  const useRequestCode = useSendPhoneOtpCommon()
  const useVerifyCode = useVerifyPhoneOtpCommon()
 //navigation.navigate("User") 
  return (
    <VerifyOtpCodeScreen
      useRequestCode={useRequestCode}
      useVerifyCode={useVerifyCode}
      onVerifySuccess={() => navigation.navigate("PasscodeScreen")}
      onBack={navigation.goBack}
      extraParams={{ phoneNumber: target }}
      title={t("Verify Phone Number")}
      target={target}
    />
  )
}

export default VerifyPhoneOtp
