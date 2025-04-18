import React, { useContext, useEffect } from "react"
import { useForm } from "react-hook-form"
import { Link, useNavigate } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { AccountDetailsForm } from "src/constants/formTypes"
import { OnboardingContext } from "src/context/Onboarding"
import { useValidationSchemas } from "src/constants/validationSchemas"
import { yupResolver } from "@hookform/resolvers/yup"
import { useTranslation } from "react-i18next"
import { useUserDetailsAndEmailOTP } from "src/api/user"

const AccountDetails: React.FC = () => {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const { onboardingValues, setOnboardingValues } =
    useContext(OnboardingContext)
  const { accountDetailsSchema } = useValidationSchemas()
  const {
    register,
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

  const setFormState = () => {
    setOnboardingValues({
      ...onboardingValues,
      accountDetails: getValues()
    })
  }

  useEffect(() => {
    if (!onboardingValues.createAccount.phoneNumber) {
      navigate(RouteType.CREATE_ACCOUNT)
    }
  }, [])

  useEffect(() => {
    if (isSuccess) {
      setFormState()
      navigate(RouteType.VERIFY_EMAIL_CODE)
    }
  }, [isSuccess])

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
    <>
      {onboardingValues.createAccount.phoneNumber && (
        <div className="wp-form">
          <h2>{t("Account Details")}</h2>
          <form  autoComplete="off" onSubmit={handleSubmit(onNext)} role="form" noValidate>
            {error && (
              <div className="alert alert-danger" role="alert">
                {error.message}
              </div>
            )}
            <div className="form-group mt-4">
              <label htmlFor="email">{t("Email Address")}</label>
              <input aria-autocomplete='both' aria-haspopup="false"
                {...register("email")}
/*                 autoComplete="true" 
 */                placeholder={t("Email Address")}
                data-testid="email"
                type="email"
                className={`form-control ${
                  errors.email?.message ? "is-invalid" : ""
                }`}
                aria-describedby="emailError"
              />
              {errors.email?.message && (
                <div id="emailError" className="invalid-feedback">
                  {errors.email?.message}
                </div>
              )}
            </div>
            <div className="form-group mt-4">
              <label htmlFor="newPassword">{t("Password")}</label>
              <input aria-autocomplete='both' aria-haspopup="false"
                type="password"
/*                 autoComplete="true" 
 */                {...register("newPassword")}
                placeholder={`${t("Password")}`}
                data-testid="newPassword"
                className={`form-control ${
                  errors.newPassword?.message ? "is-invalid" : ""
                }`}
                aria-describedby="newPasswordError"
              />
              {errors.newPassword?.message && (
                <div id="newPasswordError" className="invalid-feedback">
                  {errors.newPassword?.message}
                </div>
              )}
            </div>
            <div className="form-group mt-4">
              <label htmlFor="confirmPassword">{t("Confirm Password")}</label>
              <input aria-autocomplete='both' aria-haspopup="false"
                type="password"
/*                 autoComplete="true" 
 */                {...register("confirmPassword")}
                placeholder={`${t("Re-enter Password")}`}
                data-testid="confirmPassword"
                className={`form-control ${
                  errors.confirmPassword?.message ? "is-invalid" : ""
                }`}
                aria-describedby="confirmPasswordError"
              />
              {errors.confirmPassword?.message && (
                <div id="confirmPasswordError" className="invalid-feedback">
                  {errors.confirmPassword?.message}
                </div>
              )}
            </div>
            <div className="form-group row row-cols-auto">
              <div className="col mt-4">
                <Link
                  to={RouteType.CREATE_ACCOUNT}
                  role="button"
                  className={`btn btn-secondary ${isLoading ? "disabled" : ""}`}
                >
                  {t("Back")}
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
      )}
    </>
  )
}

export default AccountDetails
