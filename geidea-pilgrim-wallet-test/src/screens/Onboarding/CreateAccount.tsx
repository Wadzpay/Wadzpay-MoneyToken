import React, { useContext, useEffect, useState } from "react"
import { useTranslation, Trans } from "react-i18next"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"

import {
  Checkbox,
  Container,
  Link,
  ScreenLayoutOnboarding,
  SelectField,
  PhoneNumberField,
  Typography
} from "~/components/ui"
import { OnboardingNavigationProps } from "~/components/navigators/OnboardingStackNavigator"
import { fieldProps } from "~/constants/fieldProps"
import { useValidationSchemas, CreateAccountForm } from "~/constants"
import { OnboardingContext } from "~/context"
import { useSendPhoneOTP } from "~/api/otp"
import { formatPhoneNumber } from "~/utils"

// TODO replace dummy values
const dummyCountries = [
  { label: "Indonesia", value: "ID", phoneNumberPrefix: "+62" },
  { label: "India", value: "IN", phoneNumberPrefix: "+91" },
  { label: "Czech Republic", value: "CZ", phoneNumberPrefix: "+420" },
  { label: "Hungary", value: "HU", phoneNumberPrefix: "+36" },
  { label: "Philippines", value: "PH", phoneNumberPrefix: "+63" },
  { label: "Singapore", value: "SG", phoneNumberPrefix: "+65" },
  { label: "Slovakia", value: "SK", phoneNumberPrefix: "+421" },
  { label: "Bangladesh", value: "BG", phoneNumberPrefix: "+880" },
  { label: "Pakistan", value: "PK", phoneNumberPrefix: "+92" },
  {
    label: "The Kingdom of Saudi Arabia",
    value: "SA",
    phoneNumberPrefix: "+966"
  },
  { label: "The United Arab Emirates", value: "AE", phoneNumberPrefix: "+971" },
  { label: "Ukraine", value: "UA", phoneNumberPrefix: "+380" },
  { label: "United Kingdom", value: "UK", phoneNumberPrefix: "+44" }
]

const CreateAccount: React.FC<OnboardingNavigationProps> = ({
  navigation
}: OnboardingNavigationProps) => {
  const { t } = useTranslation()
  const { onboardingValues, setOnboardingValues } = useContext(OnboardingContext)
  const { createAccountSchema } = useValidationSchemas()
  const {
    control,
    handleSubmit,
    formState: { errors },
    getValues
  } = useForm<CreateAccountForm>({
    resolver: yupResolver(createAccountSchema),
    defaultValues: onboardingValues.createAccount
  })
  const [countryCode, setCountryCode] = useState(
    onboardingValues.createAccount.country
  )

  const {
    mutate: sendPhoneOTP,
    isLoading,
    error,
    isSuccess
  } = useSendPhoneOTP()

  useEffect(() => {
    if (isSuccess) {
      setFormState()

      navigation.navigate("VerifyPhoneCode", {
        screenType: "phone",
        target: getValues().phoneNumber
      })
    }
  }, [isSuccess])

  const setFormState = () => {
    setOnboardingValues({
      ...onboardingValues,
      createAccount: getValues()
    })
  }

  const onBack = () => {
    setFormState()
    navigation.goBack()
  }

  const onNext = () => {
    sendPhoneOTP({
      phoneNumber: formatPhoneNumber(getValues().phoneNumber)
    })
  }

  return (
    <ScreenLayoutOnboarding
      title={t("Create Your Account")}
      content={
        <Container spacing={1}>
          <SelectField
            label={t("Country")}
            name="country"
            items={dummyCountries}
            control={control}
            error={errors.country}
            initalCountryName = {dummyCountries.find((c) => c.value === countryCode)
              ?.label}
            onChange={setCountryCode}

          />
          <PhoneNumberField
            // TODO replace dummy values
            phoneNumberPrefix={
              dummyCountries.find((c) => c.value === countryCode)
                ?.phoneNumberPrefix
            }
            label={t("Phone Number")}
            name="phoneNumber"
            control={control}
            error={errors.phoneNumber}
            placeholder={t("Phone Number")}
            {...fieldProps.phoneNumber}
          />

          {countryCode == "SG" ? (
            <Checkbox
              //Added market aggreement for Singapore as per Anish's feedback on 11 Feb 2022
              label={t("Marketing purposes agreement")}
              name="isNewsletter"
              control={control}
              error={errors.isNewsletter}
            />
          ) : (
            <Typography></Typography>
          )}
          {/* <Checkbox
            label={t("Marketing purposes agreement")}
            name="isNewsletter"
            control={control}
            error={errors.isNewsletter}
          /> */}
          <Checkbox
            label={
              <Typography textAlign="left">
                <Trans
                  i18nKey="ToS and PP agreement"
                  t={t}
                  components={[
                    <Link
                      key="0"
                      title={t("Terms of Service")}
                      url="https://wadzpay.com/legal/conditions-of-access-to-the-wadzpay-website/"
                    />,
                    <Link
                      key="1"
                      title={t("Privacy Policy")}
                      url="https://wadzpay.com/PrivacyPolicy"
                    />
                  ]}
                />
              </Typography>
            }
           
            name="isAgreement"
            control={control}
            error={errors.isAgreement}
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

export default CreateAccount
