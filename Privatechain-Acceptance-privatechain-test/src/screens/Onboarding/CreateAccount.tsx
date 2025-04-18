import React, { useContext, useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { Link, useNavigate } from "react-router-dom"
import { useSendPhoneOTP } from "src/api/otp"
import { CreateAccountForm } from "src/constants/formTypes"
import { RouteType } from "src/constants/routeTypes"
import { OnboardingContext } from "src/context/Onboarding"
import { formatPhoneNumber } from "src/utils"
import { useValidationSchemas } from "src/constants/validationSchemas"
import { yupResolver } from "@hookform/resolvers/yup"
import PhoneNumberField from "src/components/ui/PhoneNumberField"
import { t } from "i18next"
import { availableCountries } from "src/constants/types"

const CreateAccount: React.FC = () => {
  const navigate = useNavigate()
  const { onboardingValues, setOnboardingValues } =
    useContext(OnboardingContext)

  const {
    mutate: sendPhoneOTP,
    isLoading,
    error,
    isSuccess
  } = useSendPhoneOTP()
  const [phoneNumberPrefix, setPhoneNumberPrefix] = useState(
    availableCountries.find(
      (c) => c.value === onboardingValues.createAccount.country
    )?.phoneNumberPrefix || ""
  )

  const { createAccountSchema } = useValidationSchemas()
  const {
    control,
    handleSubmit,
    getValues,
    register,
    formState: { errors }
  } = useForm<CreateAccountForm>({
    resolver: yupResolver(createAccountSchema),
    defaultValues: onboardingValues.createAccount
  })

  const onNext = () => {
    sendPhoneOTP({
      phoneNumber: formatPhoneNumber(getValues().phoneNumber)
    })
  }

  useEffect(() => {
    if (isSuccess) {
      setFormState()
      navigate(RouteType.VERIFY_PHONE_CODE)
    }
  }, [isSuccess])

  const setFormState = () => {
    setOnboardingValues({
      ...onboardingValues,
      createAccount: getValues()
    })
  }

  const errorMessage = error?.message || errors?.phoneNumber?.message

  return (
    <div className="wp-form">
      <h2>Create Your Account</h2>
      <form  autoComplete="off"  /* autoComplete="true" */  onSubmit={handleSubmit(onNext)} role="form" noValidate>
        <div className="form-group mt-4">
          <label htmlFor="country">Country</label>
          <select
            {...register("country")}
            className="form-control"
            data-testid="country"
            defaultValue={onboardingValues.createAccount.country}
            onChange={(x) => {
              setPhoneNumberPrefix(
                availableCountries.find((c) => c.value === x.target.value)
                  ?.phoneNumberPrefix || ""
              )
            }}
          >
            {availableCountries.map((country) => (
              <option key={country.value} value={country.value}>
                {country.label}
              </option>
            ))}
          </select>
        </div>
        <PhoneNumberField
          control={control}
          errorMessage={errorMessage}
          label="Phone Number"
          name="phoneNumber"          
          phoneNumberPrefix={phoneNumberPrefix}
          register={register}
        />
        <div className="form-group row row-cols-auto">
          <div className="col mt-4">
            <Link
              to={RouteType.HOME}
              role="button"
              className={`btn btn-secondary ${isLoading ? "disabled" : ""}`}
            >
              {t("Cancel")}
            </Link>
          </div>
          <div className="col mt-4">
            <button
              type="submit"
              className="btn btn-primary"
              disabled={isLoading}
            >
              Next
            </button>
          </div>
        </div>
      </form>
    </div>
  )
}

export default CreateAccount
