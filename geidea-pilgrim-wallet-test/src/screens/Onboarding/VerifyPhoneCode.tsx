import React, { useContext } from "react"
import { NativeStackScreenProps } from "@react-navigation/native-stack"

import { useTranslation } from "react-i18next"

import { OnboardingStackParamList } from "~/components/navigators"
import { VerifyCode } from "~/screens"
import { OnboardingContext } from "~/context"
import { useSendPhoneOTP, useVerifyPhoneOTP } from "~/api/otp"

type Props = NativeStackScreenProps<OnboardingStackParamList, "VerifyEmailCode">

const VerifyPhoneCode: React.FC<Props> = ({ navigation, route }: Props) => {
  const { target } = route.params
  const { t } = useTranslation()
  const { onboardingValues } = useContext(OnboardingContext)

  const useRequestCode = useSendPhoneOTP()
  const useVerifyCode = useVerifyPhoneOTP()

  return (
    <VerifyCode
      useRequestCode={useRequestCode}
      useVerifyCode={useVerifyCode}
      onVerifySuccess={() => navigation.navigate("AccountDetails")}
      onBack={navigation.goBack}
      extraParams={{ phoneNumber: onboardingValues.createAccount.phoneNumber }}
      title={t("Verify Phone Number")}
      target={target}
    />
  )
}

export default VerifyPhoneCode
