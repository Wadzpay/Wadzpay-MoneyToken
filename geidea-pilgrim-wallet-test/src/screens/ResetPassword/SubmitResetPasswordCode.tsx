import React from "react"
import { NativeStackScreenProps } from "@react-navigation/native-stack"
import { useTranslation } from "react-i18next"
import { Alert } from "react-native"

import { ResetPasswordStackParamList } from "~/components/navigators"
import { VerifyCode } from "~/screens"
import {
  useRequestResetPasswordCode,
  useSubmitResetPasswordCode
} from "~/api/user"

type Props = {
  email: string
  newPassword: string
} & NativeStackScreenProps<ResetPasswordStackParamList, "SubmitResetPasswordCode">

const SubmitResetPasswordCode: React.FC<Props> = ({
  navigation,
  route
}: Props) => {
  const { email, newPassword } = route.params

  const { t } = useTranslation()
  const useRequestCode = useRequestResetPasswordCode()
  const useVerifyCode = useSubmitResetPasswordCode()

  return (
    <VerifyCode
      useRequestCode={useRequestCode}
      useVerifyCode={useVerifyCode}
      onVerifySuccess={() => {
        Alert.alert(
          t("Password reset successful!"),
          t("You can now try logging in"),
          [{ text: t("Login"), onPress: () => navigation.navigate("SignIn") }]
        )
      }}
      onBack={navigation.goBack}
      extraParams={{ email, newPassword }}
      title={t("Verify your email")}
      target={email}
    />
  )
}

export default SubmitResetPasswordCode
