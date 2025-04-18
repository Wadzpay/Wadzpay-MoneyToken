import React, { useEffect } from "react"
import { useTranslation } from "react-i18next"
import { Alert, StyleSheet } from "react-native"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"

import { RootNavigationProps } from "~/components/navigators"
import { fieldProps, useValidationSchemas } from "~/constants"
import {
  Button,
  Container,
  ErrorModal,
  Header,
  ScreenLayoutBottomActions,
  TextField,
  Typography
} from "~/components/ui"
import { ChangePasswordForm } from "~/constants/formTypes"
import { useChangePassword } from "~/api/user"

const styles = StyleSheet.create({
  container: {
    flex: 1
  }
})

const ChangePassword: React.FC<RootNavigationProps> = ({
  navigation
}: RootNavigationProps) => {
  const { t } = useTranslation()

  const { changePasswordSchema } = useValidationSchemas()
  const {
    control,
    handleSubmit,
    formState: { errors }
  } = useForm<ChangePasswordForm>({
    resolver: yupResolver(changePasswordSchema),
    defaultValues: {
      currentPassword: "",
      newPassword: "",
      confirmPassword: ""
    }
  })

  const {
    mutate: changePassword,
    isSuccess,
    isLoading,
    error
  } = useChangePassword()

  useEffect(() => {
    if (isSuccess) {
      Alert.alert(t("Password changed successfully!"), "", [
        { text: t("Continue"), onPress: () => navigation.goBack() }
      ])
    }
  }, [isSuccess])

  const onSubmit = (changePasswordForm: ChangePasswordForm) => {
    changePassword(changePasswordForm)
  }

  return (
    <ScreenLayoutBottomActions
      dismissKeyboard
      header={
        <Header
          title={t("Change Password")}
          leftIconName="ArrowLeft"
          onLeftIconClick={() => navigation.goBack()}
        />
      }
      content={
        <Container spacing={1} style={styles.container}>
          <TextField
            label={t("Current password")}
            name="currentPassword"
            control={control}
            error={errors.currentPassword}
            maxLength={16}
            placeholder={`${t("Current password")} *`}
            isPassword
            {...fieldProps.password}
          />
          <Typography color={"grayMedium"} textAlign={"left"}>
            {" "}
            {t("Password regex note")}
          </Typography>
          <TextField
            label={t("New password")}
            name="newPassword"
            control={control}
            error={errors.newPassword}
            maxLength={16}
            placeholder={`${t("New password")} *`}
            isPassword
            {...fieldProps.newPassword}
          />

          <TextField
            label={t("Confirm new password")}
            name="confirmPassword"
            control={control}
            error={errors.confirmPassword}
            maxLength={16}
            placeholder={`${t("Re-enter password")} *`}
            isPassword
            {...fieldProps.password}
          />
          <ErrorModal error={error} />
        </Container>
      }
      actions={
        <Button
          variant="primary"
          onPress={handleSubmit(onSubmit)}
          text={t("Change Password")}
          loading={isLoading}
        />
      }
    />
  )
}

export default ChangePassword
