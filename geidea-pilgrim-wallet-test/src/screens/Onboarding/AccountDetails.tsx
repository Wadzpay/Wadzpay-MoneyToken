import React, { useContext, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"
import {
  Button,
  ErrorModal,
  Header,
  ScreenLayoutBottomActions,
  theme,
  Typography
} from "~/components/ui"
import { Container, ScreenLayoutOnboarding, TextField } from "~/components/ui"
import { OnboardingNavigationProps } from "~/components/navigators/OnboardingStackNavigator"
import {
  AccountDetailsForm,
  fieldProps,
  useValidationSchemas
} from "~/constants"
import { OnboardingContext } from "~/context"
import { useUserDetailsAndEmailOTP } from "~/api/user"

const AccountDetails: React.FC<OnboardingNavigationProps> = ({
  navigation
}: OnboardingNavigationProps) => {
  const { t } = useTranslation()
  const { onboardingValues, setOnboardingValues } =
    useContext(OnboardingContext)
  const { accountDetailsSchema } = useValidationSchemas()
  const {
    control,
    handleSubmit,
    formState: { errors },
    getValues
  } = useForm<AccountDetailsForm>({
    resolver: yupResolver(accountDetailsSchema),
    defaultValues: onboardingValues.accountDetails
  })

  const {
    mutate: userDetailsAndEmailOTP,
    isLoading,
    error,
    isSuccess
  } = useUserDetailsAndEmailOTP()

  useEffect(() => {
    if (isSuccess) {
      navigation.navigate("VerifyEmailCode", {
        screenType: "email",
        target: getValues().email
      })
    }
  }, [isSuccess])

  const setFormState = () => {
    setOnboardingValues({
      ...onboardingValues,
      accountDetails: getValues()
    })
  }

  const onBack = () => {
    setFormState()
    navigation.pop(2) // Go back to CreateAccount screen
  }

  const onNext = () => {
    const { email, newPassword } = getValues()
    userDetailsAndEmailOTP({
      email,
      password: newPassword,
      phoneNumber: onboardingValues.createAccount.phoneNumber
    })

    setFormState()
  }

  return (
    <ScreenLayoutOnboarding
      title={t("Account Details")}
      content={
        <Container spacing={1}>
          <TextField
            label={t("Email Address")}
            name="email"
            control={control}
            error={errors.email}
            placeholder={`${t("Email Address")} * `}
            autoCapitalize="none"
            {...fieldProps.email}
          />
          <TextField
            label={t("Password")}
            name="newPassword"
            control={control}
            error={errors.newPassword}
            maxLength={16}
            placeholder={`${t("Password")} *`}
            isPassword
            {...fieldProps.newPassword}
          />
          <Typography color={"grayMedium"} textAlign={"left"}>
            {" "}
            {t("Password regex note")}
          </Typography>
          <TextField
            label={t("Confirm Password")}
            name="confirmPassword"
            control={control}
            error={errors.confirmPassword}
            maxLength={16}
            placeholder={`${t("Re-enter Password")} *`}
            isPassword
            {...fieldProps.newPassword}
          />
        </Container>
      }
      onBack={onBack}
      onNext={handleSubmit(onNext)}
      nextDisabled={isLoading}
      nextLoading={isLoading}
      error={error}
    />
  )
}

export default AccountDetails
