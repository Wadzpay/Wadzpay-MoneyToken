import React, { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { useNavigate } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { Input } from "antd"
import { ResetPasswordForm } from "src/constants/formTypes"
import { useValidationSchemas } from "src/constants/validationSchemas"
import { yupResolver } from "@hookform/resolvers/yup"
import { useTranslation } from "react-i18next"
import {
  useRequestResetPasswordCode,
  useUserVerifyResetIssuance
} from "src/api/user"
import env from "src/env"

const RequestResetPasswordCode: React.FC = () => {
  const navigate = useNavigate()
  const { t } = useTranslation()

  const { resetPasswordSchema } = useValidationSchemas()
  const {
    register,
    handleSubmit,
    formState: { errors },
    getValues
  } = useForm<ResetPasswordForm>({
    resolver: yupResolver(resetPasswordSchema),
    defaultValues: {
      email: env.DEFAULT_USER_EMAIL
    }
  })

  const [query, setQuery] = useState("")
  const {
    data: userData,
    error: emailError,
    isSuccess: emailValid
  } = useUserVerifyResetIssuance(query)

  const {
    mutate: requestResetPasswordCode,
    isLoading,
    isSuccess,
    error,
    reset: resetRequestResetPasswordCode
  } = useRequestResetPasswordCode()

  const onSendResetPasswordEmail = async () => {
    const { email } = getValues()
    setQuery("email=" + email)
  }

  useEffect(() => {
    async function fetchMyAPI() {
      const { email } = getValues()
      if (email != "") {
        await requestResetPasswordCode({ email })
      }
    }
    fetchMyAPI()
  }, [userData])

  useEffect(() => {
    if (isSuccess) {
      // const { email, newPassword } = getValues()
      const { email } = getValues()
      navigate(RouteType.SUBMIT_RESET_PASSWORD, {
        // state: { email: email, newPassword: newPassword }
        state: { email: email }
      })
      resetRequestResetPasswordCode()
    }
  }, [isSuccess])

  return (
    // <div className="wp-form">
    //   <h2>{t("Reset Password")}</h2>
    //   <form
    //     onSubmit={handleSubmit(onSendResetPasswordEmail)}
    //     role="form"
    //     noValidate
    //   >
    //     {error && (
    //       <div className="alert alert-danger" role="alert">
    //         {error.message}
    //       </div>
    //     )}
    //     <div className="form-group mt-4">
    //       <label htmlFor="email">{t("Email")}</label>
    //       <input
    //         {...register("email")}
    //         placeholder={t("Email")}
    //         type="email"
    //         data-testid="email"
    //         className={`form-control ${
    //           errors.email?.message ? "is-invalid" : ""
    //         }`}
    //         aria-describedby="emailError"
    //       />

    //       {errors.email?.message && (
    //         <div id="emailError" className="invalid-feedback">
    //           {errors.email?.message}
    //         </div>
    //       )}
    //     </div>
    //     <div className="form-group mt-4">
    //       <label htmlFor="newPassword">{t("Password")}</label>
    //       <input
    //         type="password"
    //         {...register("newPassword")}
    //         placeholder={`${t("Password")}`}
    //         data-testid="newPassword"
    //         className={`form-control ${
    //           errors.newPassword?.message ? "is-invalid" : ""
    //         }`}
    //         aria-describedby="newPasswordError"
    //       />
    //       {errors.newPassword?.message && (
    //         <div id="newPasswordError" className="invalid-feedback">
    //           {errors.newPassword?.message}
    //         </div>
    //       )}
    //     </div>
    //     <div className="form-group mt-4">
    //       <label htmlFor="confirmPassword">{t("Confirm Password")}</label>
    //       <input
    //         type="password"
    //         {...register("confirmPassword")}
    //         placeholder={`${t("Re-enter Password")}`}
    //         data-testid="confirmPassword"
    //         className={`form-control ${
    //           errors.confirmPassword?.message ? "is-invalid" : ""
    //         }`}
    //         aria-describedby="confirmPasswordError"
    //       />
    //       {errors.confirmPassword?.message && (
    //         <div id="confirmPasswordError" className="invalid-feedback">
    //           {errors.confirmPassword?.message}
    //         </div>
    //       )}
    //     </div>
    //     <div className="form-group row row-cols-auto">
    //       <div className="col mt-4">
    //         <button
    //           type="button"
    //           className="btn btn-secondary"
    //           disabled={isLoading}
    //           onClick={() => navigate(RouteType.SIGN_IN)}
    //         >
    //           {t("Cancel")}
    //         </button>
    //       </div>

    //       <div className="col mt-4">
    //         <button
    //           type="submit"
    //           className="btn btn-primary"
    //           disabled={isLoading}
    //         >
    //           Next
    //         </button>
    //       </div>
    //     </div>
    //   </form>
    // </div>

    <div className="signIn">
      <img src={"/images/login_bg.svg"} className="loginBG" />
      <div className="userAuth">
        <div className="userAuthForm">
          <form
            onSubmit={handleSubmit(onSendResetPasswordEmail)}
            role="form"
            noValidate
            autoComplete="off"
          >
            {emailError && (
              <div
                style={{ width: "250px", lineHeight: "20px" }}
                className="alert alert-danger"
                role="alert"
              >
                {emailError.message}
              </div>
            )}
            {error && (
              <div
                style={{ width: "250px", lineHeight: "20px" }}
                className="alert alert-danger"
                role="alert"
              >
                {error.message}
              </div>
            )}
            <div className="userAuthWTKLogo">
              <img src={"/images/issuance_logo.svg"} />
            </div>
            <hr className="loginBorderLine" />
            <div className="userAuthLoginText mt-4">
              <h5>{t("Reset Password")}</h5>
            </div>
            <div className="form-group mt-2">
              <input
                {...register("email")}
                name={`email`}
                placeholder={t("Email")}
                // type="email"
                data-testid="email"
                className={`userInput ${
                  errors.email?.message ? "is-invalid" : ""
                }`}
                aria-describedby="emailError"
                autoComplete="off"
              />

              {errors.email?.message && (
                <div
                  id="emailError"
                  className="invalid-feedback"
                  style={{ textAlign: "left" }}
                >
                  {errors.email?.message}
                </div>
              )}
            </div>
            {/* <div className="form-group mt-3">
              <input
                type="password"
                {...register("newPassword")}
                placeholder={`${t("Password")}`}
                data-testid="newPassword"
                className={`userInput ${
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
            <div className="form-group mt-3">
              <input
                type="password"
                {...register("confirmPassword")}
                placeholder={`${t("Re-enter Password")}`}
                data-testid="confirmPassword"
                className={`userInput ${
                  errors.confirmPassword?.message ? "is-invalid" : ""
                }`}
                aria-describedby="confirmPasswordError"
              />
              {errors.confirmPassword?.message && (
                <div id="confirmPasswordError" className="invalid-feedback">
                  {errors.confirmPassword?.message}
                </div>
              )}
            </div> */}
            <div className="form-group row row-cols-center mb-4">
              <div className="col mt-4">
                <button
                  type="button"
                  className="btn btn-secondary signInBtnGrey"
                  disabled={isLoading}
                  onClick={() => navigate(RouteType.SIGN_IN)}
                >
                  {t("Cancel")}
                </button>
              </div>
              <div className="col mt-4">
                <button
                  type="submit"
                  className="btn btn-primary signInBtn"
                  disabled={isLoading}
                  style={{ color: "#000000" }}
                >
                  Next
                </button>
              </div>
            </div>
          </form>
        </div>
        <div className="copyright">Copyright ©️ 2024 WadzPay Worldwide.</div>
      </div>
    </div>
  )
}

export default RequestResetPasswordCode
