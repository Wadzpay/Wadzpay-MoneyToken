import React, { useContext } from "react"
import { NativeStackScreenProps } from "@react-navigation/native-stack"

import { useTranslation } from "react-i18next"
import { CommonActions } from "@react-navigation/native"

import { OnboardingStackParamList } from "~/components/navigators"
import { VerifyCode } from "~/screens"
import {
  useUserDetailsAndEmailOTP,
  useVerifyEmailOTPAndCreateUser
} from "~/api/user"
import { OnboardingContext } from "~/context"

type Props = NativeStackScreenProps<OnboardingStackParamList, "VerifyEmailCode">

const VerifyEmailCode: React.FC<Props> = ({ navigation, route }: Props) => {
  const { target } = route.params
  const { t } = useTranslation()
  const { onboardingValues } = useContext(OnboardingContext)

  const useRequestCode = useUserDetailsAndEmailOTP()
  const useVerifyCode = useVerifyEmailOTPAndCreateUser()

  return (
    <VerifyCode
      useRequestCode={useRequestCode}
      useVerifyCode={useVerifyCode}
      onVerifySuccess={() =>
        navigation.dispatch(
          CommonActions.reset({
            index: 0,
            routes: [{ name: "EnableNotifications" }]
          })
        )
      }
      onBack={navigation.goBack}
      extraParams={{
        email: target,
        phoneNumber: onboardingValues.createAccount.phoneNumber,
        password: onboardingValues.accountDetails.newPassword
      }}
      title={t("Verify Your Email")}
      target={target}
    />
  )
}

export default VerifyEmailCode
