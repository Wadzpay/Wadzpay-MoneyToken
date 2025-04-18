import React, { useEffect } from "react"
import { StyleSheet } from "react-native"
import { useTranslation } from "react-i18next"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"
import { NativeStackScreenProps } from "@react-navigation/native-stack"

import {
  Button,
  Container,
  ErrorModal,
  Header,
  ScreenLayoutBottomActions,
  TextField,
  theme,
  Typography
} from "~/components/ui"
import {
  fieldProps,
  ResetPasswordForm,
  useValidationSchemas
} from "~/constants"
import { useCheckValidEmail, useRequestResetPasswordCode } from "~/api/user"
import env from "~/env"
import { ResetPasswordStackParamList } from "~/components/navigators"
import { watch } from "fs"
import { useState } from "react"

const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  resend: { height: theme.iconSize.md } // Should be the same as loading spinner
})

type Props = NativeStackScreenProps<
  ResetPasswordStackParamList,
  "RequestResetPasswordCode"
>

const ResetPassword: React.FC<Props> = ({ navigation }: Props) => {
  const { t } = useTranslation()
  const { resetPasswordSchema } = useValidationSchemas()

  const [email,setEmail] = useState("")
  const {
    control,
    handleSubmit,
    formState: { errors },
    getValues
  } = useForm<ResetPasswordForm>({
    resolver: yupResolver(resetPasswordSchema),
    defaultValues: {
      email: env.DEFAULT_USER_EMAIL,
      newPassword: env.DEFAULT_USER_PASSWORD,
      confirmPassword: env.DEFAULT_USER_PASSWORD
    }
  })
  const {
    mutate: requestResetPasswordCode,
    isLoading,
    isSuccess,
    error,
    reset: resetRequestResetPasswordCode
  } = useRequestResetPasswordCode()
  
  const [hitAPI, setHitAPI]= useState(false)
  const {
    data: checkValidEmail,
    isLoading: isLoadingEmail,
    error: errorEmail,
    isSuccess: validEmail
  } = useCheckValidEmail(email,hitAPI)


  const onSendResetPasswordEmail = async () => {
    let { email } = getValues()
    email = email.toLocaleLowerCase()
    await requestResetPasswordCode({ email })
  }

  const onCheckValidEmail = () => {
    let { email } = getValues()
    email = email.toLocaleLowerCase()
    setEmail(email)
    setHitAPI(true)
  }

  useEffect(() => {
    if (isSuccess) {
      let { email, newPassword } = getValues()
      email = email.toLocaleLowerCase()
      navigation.navigate("SubmitResetPasswordCode", {
        email,
        newPassword
      })
      resetRequestResetPasswordCode()
    }
  }, [isSuccess])

  useEffect(() => {
    
    if(validEmail){
      onSendResetPasswordEmail()
    } else {

    }
  }, [validEmail])

  useEffect(() => {
  },[errorEmail])

  return (
    <ScreenLayoutBottomActions
      dismissKeyboard
      header={
        <Header
          title={t("Reset Password")}
          leftIconName="ArrowLeft"
          onLeftIconClick={() => navigation.goBack()}
        />
      }
      content={
        <Container spacing={1} style={styles.container}>
          <TextField
            label={t("Email")}
            name="email"
            control={control}
            error={errors.email}
            iconName="User"
            placeholder={`${t("Email Address")} * `}
            {...fieldProps.username}
          />
          <TextField
            label={t("New password")}
            name="newPassword"
            control={control}
            error={errors.newPassword}
            iconName="Lock"
            maxLength={16}
            placeholder={`${t("New password")} *`}
            isPassword
            autoComplete="off"
            {...fieldProps.newPassword}
          />
          <Typography color={"grayMedium"} textAlign={"left"}>
            {" "}
            {t("Password regex note")}
          </Typography>
          <TextField
            label={t("Confirm password")}
            name="confirmPassword"
            control={control}
            error={errors.confirmPassword}
            iconName="Lock"
            maxLength={16}
            placeholder={`${t("Re-enter Password")} *`}
            isPassword
            autoComplete="off"
            {...fieldProps.password}
          />
          {
            errorEmail && <ErrorModal error={errorEmail}/>
          }
          
        </Container>
      }
      actions={
        <Container spacing={2}>
          <Button
            variant="primary"
            onPress={handleSubmit(onCheckValidEmail)}
            text={t("Send Email")}
            loading={isLoadingEmail}
          />
        </Container>
      }
    />
  )
}

export default ResetPassword
