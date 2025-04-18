import React, { useContext } from "react"
import { CommonActions } from "@react-navigation/native"
import { useTranslation } from "react-i18next"
import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"

import { OnboardingNavigationProps } from "~/components/navigators/OnboardingStackNavigator"
import {
  Container,
  ScreenLayoutOnboarding,
  SelectField,
  TextField,
  DateField
} from "~/components/ui"
import { fieldProps } from "~/constants/fieldProps"
import { PersonalDetailsForm, useValidationSchemas } from "~/constants"
import { OnboardingContext } from "~/context"

const dummyProfessions = [
  { label: "Accountant", value: "1" },
  { label: "Software Engineer", value: "2" }
]

const dummySourcesOfFund = [
  { label: "Employed", value: "1" },
  { label: "Allowance", value: "2" }
]

const PersonalDetails: React.FC<OnboardingNavigationProps> = ({
  navigation
}: OnboardingNavigationProps) => {
  const { t } = useTranslation()
  const { onboardingValues, setOnboardingValues } =
    useContext(OnboardingContext)
  const { personalDetailsSchema } = useValidationSchemas()
  const {
    control,
    handleSubmit,
    formState: { errors },
    getValues
  } = useForm<PersonalDetailsForm>({
    resolver: yupResolver(personalDetailsSchema),
    defaultValues: onboardingValues.personalDetails
  })

  const setFormState = () => {
    setOnboardingValues({
      ...onboardingValues,
      personalDetails: getValues()
    })
  }

  const onNext = () => {
    setFormState()
    navigation.dispatch(
      CommonActions.reset({
        index: 0,
        routes: [{ name: "Home" }]
      })
    )
  }

  return (
    <ScreenLayoutOnboarding
      title={t("Personal Details")}
      onSkip={onNext}
      content={
        <Container spacing={1}>
          <TextField
            label={t("First Name")}
            name="firstName"
            control={control}
            error={errors.firstName}
            placeholder={t("First Name")}
            {...fieldProps.firstName}
          />
          <TextField
            label={t("Last Name")}
            name="lastName"
            control={control}
            error={errors.lastName}
            placeholder={t("Last Name")}
            {...fieldProps.lastName}
          />
          <DateField
            label={t("Date of Birth")}
            name="dateOfBirth"
            control={control}
            error={errors.dateOfBirth}
          />

          <SelectField
            label={t("Profession")}
            name="profession"
            control={control}
            error={errors.profession}
            items={dummyProfessions}
            placeholder={t("Select Your Profession")}
          />
          <SelectField
            label={t("Source of Fund")}
            name="sourceOfFund"
            control={control}
            error={errors.sourceOfFund}
            items={dummySourcesOfFund}
            placeholder={t("Source of Fund")}
          />
        </Container>
      }
      onNext={handleSubmit(onNext)}
    />
  )
}

export default PersonalDetails
