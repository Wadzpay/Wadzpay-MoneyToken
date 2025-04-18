import React, { useContext, useEffect } from "react"
import { Controller, useForm } from "react-hook-form"
import { useNavigate } from "react-router-dom"
import { yupResolver } from "@hookform/resolvers/yup"
import { useTranslation } from "react-i18next"
import "react-phone-number-input/style.css"
import PhoneInput, {
  isValidPhoneNumber,
  getCountries
} from "react-phone-number-input"
import { RouteType } from "src/constants/routeTypes"
import { MerchantDetailsForm } from "src/constants/formTypes"
import { OnboardingContext } from "src/context/Onboarding"
import { useValidationSchemas } from "src/constants/validationSchemas"
import { useMerchant } from "src/api/user"
import { countryCodes, industryTypes } from "src/constants/selectOptionTypes"
import DOMPurify from "dompurify"

const MerchantDetails: React.FC = () => {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const { onboardingValues } = useContext(OnboardingContext)
  const { merchantSchema } = useValidationSchemas()
  const {
    control,
    register,
    handleSubmit,
    formState: { errors },
    getValues
  } = useForm<MerchantDetailsForm>({
    resolver: yupResolver(merchantSchema)
  })

  const { mutate: merchantDetails, isLoading, error, isSuccess } = useMerchant()

  const defaultCountryCode = getCountries().find(
    (cc) => cc === onboardingValues.createAccount.country
  )

  useEffect(() => {
    if (isSuccess) {
      navigate(RouteType.HOME)
    }
  }, [isSuccess])

  const onNext = () => {
    let {
      name,
      countryOfRegistration,
      registrationCode,
      primaryContactFullName,
      primaryContactEmail,
      companyType,
      industryType,
      primaryContactPhoneNumber,
      merchantId,
      defaultRefundableFiatValue,
      tnc
    } = getValues()
    
     name = DOMPurify.sanitize(name)
     countryOfRegistration = DOMPurify.sanitize(countryOfRegistration)
     registrationCode = DOMPurify.sanitize(registrationCode)
     primaryContactFullName = DOMPurify.sanitize(primaryContactFullName)
     primaryContactEmail = DOMPurify.sanitize(primaryContactEmail)
     companyType = DOMPurify.sanitize(companyType)
     industryType = DOMPurify.sanitize(industryType)
     primaryContactPhoneNumber = DOMPurify.sanitize(primaryContactPhoneNumber)
     merchantId = DOMPurify.sanitize(merchantId)
     tnc = DOMPurify.sanitize(tnc)

    merchantDetails({
      name,
      countryOfRegistration,
      registrationCode,
      primaryContactFullName,
      primaryContactEmail,
      companyType,
      industryType,
      primaryContactPhoneNumber,
      merchantId,
      defaultRefundableFiatValue,
      tnc
    })
  }

  return (
    <>
      <div className="wp-form">
        <h2>{t("Merchant Details")}</h2>
        <p>{t("Few more details")}</p>
        <form autoComplete="off" onSubmit={handleSubmit(onNext)}   role="form" noValidate>
          {error && (
            <div className="alert alert-danger" role="alert">
              {error.message}
            </div>
          )}
          <div className="form-group mt-4">
            <label htmlFor="name">{t("Merchant Name")}</label>
            <input
              {...register("name")}
              placeholder={t("Merchant Name")}
              data-testid="nameInput"
              type="text"
              aria-autocomplete='both' aria-haspopup="false"
              className={`form-control ${
                errors.name?.message ? "is-invalid" : ""
              }`}
              aria-describedby="nameError"
              //autoComplete="true" 
            />
            {errors.name?.message && (
              <div id="nameError" className="invalid-feedback">
                {errors.name?.message}
              </div>
            )}
          </div>
          <div className="form-group mt-4">
            <label htmlFor="countryOfRegistration">
              {t("Country of Registration")}
            </label>
            <select
              {...register("countryOfRegistration")}
              defaultValue={onboardingValues.createAccount.country}
              placeholder={t("Country of Registration")}
              data-testid="countryOfRegistrationInput"
              className={`form-control ${
                errors.countryOfRegistration?.message ? "is-invalid" : ""
              }`}
              aria-describedby="countryOfRegistrationError"
            >
              <option></option>
              {Object.entries(countryCodes)
                .sort((a, b) => (a[1] > b[1] ? 1 : -1))
                .map((countryCode) => (
                  <option key={countryCode[0]} value={countryCode[0]}>
                    {t(countryCode[1])}
                  </option>
                ))}
            </select>
            {errors.countryOfRegistration?.message && (
              <div id="countryOfRegistrationError" className="invalid-feedback">
                {errors.countryOfRegistration?.message}
              </div>
            )}
          </div>
          <div className="form-group mt-4">
            <label htmlFor="registrationCode">{t("Registration Code")}</label>
            <input aria-autocomplete='both' aria-haspopup="false"
              {...register("registrationCode")}
              placeholder={t("Registration Code")}
              data-testid="registrationCodeInput"
              type="text"
                          //  autoComplete="true" 
              className={`form-control ${
                errors.registrationCode?.message ? "is-invalid" : ""
              }`}
              aria-describedby="registrationCodeError"
            />
            {errors.registrationCode?.message && (
              <div id="registrationCodeError" className="invalid-feedback">
                {errors.registrationCode?.message}
              </div>
            )}
          </div>
          <div className="form-group mt-4">
            <label htmlFor="primaryContactFullName">
              {t("Primary Contact Full Name")}
            </label>
            <input aria-autocomplete='both' aria-haspopup="false"
              {...register("primaryContactFullName")}
              placeholder={t("Primary Contact Full Name")}
              data-testid="primaryContactFullNameInput"
              type="text"
            //  autoComplete="true" 
              className={`form-control ${
                errors.primaryContactFullName?.message ? "is-invalid" : ""
              }`}
              aria-describedby="primaryContactFullNameError"
            />
            {errors.primaryContactFullName?.message && (
              <div
                id="primaryContactFullNameError"
                className="invalid-feedback"
              >
                {errors.primaryContactFullName?.message}
              </div>
            )}
          </div>
          <div className="form-group mt-4">
            <label htmlFor="primaryContactEmail">
              {t("Primary Contact Email")}
            </label>
            <input aria-autocomplete='both' aria-haspopup="false"
              {...register("primaryContactEmail")}
              placeholder={t("Primary Contact Email")}
              data-testid="primaryContactEmailInput"
              type="email"
              //autoComplete="true" 
              className={`form-control ${
                errors.primaryContactEmail?.message ? "is-invalid" : ""
              }`}
              aria-describedby="primaryContactEmailError"
            />
            {errors.primaryContactEmail?.message && (
              <div id="primaryContactEmailError" className="invalid-feedback">
                {errors.primaryContactEmail?.message}
              </div>
            )}
          </div>
          <div className="form-group mt-4">
            <label htmlFor="primaryContactPhoneNumber">
              {t("Primary Contact Phone Number")}
            </label>
            <Controller
              name="primaryContactPhoneNumber"
              control={control}
              rules={{
                validate: (value) => isValidPhoneNumber(value)
              }}
              render={({ field: { onChange, value } }) => (
                <PhoneInput
                  value={value}
                  onChange={onChange}
                  //autoComplete="true" 
                  defaultCountry={defaultCountryCode}
                  placeholder={t("Primary Contact Phone Number")}
                  id="primaryContactPhoneNumberError"
                  data-testid="primaryContactPhoneNumberInput"
                  aria-describedby="primaryContactPhoneNumberError"
                  className={`form-control ${
                    errors.primaryContactPhoneNumber?.message
                      ? "is-invalid"
                      : ""
                  }`}
                />
              )}
            />
            {errors.primaryContactPhoneNumber?.message && (
              <div id="primaryContactPhoneNumber" className="invalid-feedback">
                {errors.primaryContactPhoneNumber?.message}
              </div>
            )}
          </div>
          <div className="form-group mt-4">
            <label htmlFor="companyType">{t("Company Type")}</label>
            <input aria-autocomplete='both' aria-haspopup="false"
              {...register("companyType")}
              placeholder={t("Company Type")}
              //autoComplete="true" 
              data-testid="companyTypeInput"
              type="text"
              className={`form-control ${
                errors.companyType?.message ? "is-invalid" : ""
              }`}
              aria-describedby="companyTypeError"
            />
            {errors.companyType?.message && (
              <div id="companyTypeError" className="invalid-feedback">
                {errors.companyType?.message}
              </div>
            )}
          </div>
          <div className="form-group mt-4">
            <label htmlFor="industryType">{t("Industry Type")}</label>
            <select
              {...register("industryType")}
              placeholder={t("Industry Type")}
              data-testid="industryTypeInput"
              className={`form-control ${
                errors.industryType?.message ? "is-invalid" : ""
              }`}
              aria-describedby="industryTypeError"
            >
              <option></option>
              {Object.entries(industryTypes).map((industryType) => (
                <option key={industryType[0]} value={industryType[0]}>
                  {industryType[1]}
                </option>
              ))}
            </select>
            {errors.industryType?.message && (
              <div id="industryTypeError" className="invalid-feedback">
                {errors.industryType?.message}
              </div>
            )}
          </div>
          <div className="form-group mt-4">
            <label htmlFor="merchantId">{t("Merchant Id")}</label>
            <input aria-autocomplete='both' aria-haspopup="false"
              {...register("merchantId")}
              placeholder={t("Merchant Id")}
              //autoComplete="true" 
              data-testid="merchantIdeInput"
              type="text"
              className={`form-control ${
                errors.merchantId?.message ? "is-invalid" : ""
              }`}
              aria-describedby="merchantIdError"
            />
            {errors.merchantId?.message && (
              <div id="merchantIdError" className="invalid-feedback">
                {errors.merchantId?.message}
              </div>
            )}
          </div>
          <div className="form-group row row-cols-auto">
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
    </>
  )
}

export default MerchantDetails
