import React, { useEffect, ClipboardEvent, useState, useContext } from "react"
import { useTranslation } from "react-i18next"
import { useForm } from "react-hook-form"
import { ThreeDots } from "react-loader-spinner"
import { AiFillEyeInvisible, AiFillEye } from "react-icons/ai"
import { yupResolver } from "@hookform/resolvers/yup"
import { Link, useNavigate } from "react-router-dom"
import { Space, Spin } from "antd"
import { RouteType } from "src/constants/routeTypes"
import { UserContext } from "src/context/User"

import { useSignIn, useUserVerify, verifyToken } from "../api/user"
import { SignInForm } from "../constants/formTypes"
import { useValidationSchemas } from "../constants/validationSchemas"
import env from "../env"

const SignIn: React.FC = () => {
  const [showPwdIcon, setPwdIcon] = useState(false)
  const [pwdInputType, setPwdInputType] = useState("password")
  const [query, setQuery] = useState("")
  const [data, setData] = useState<SignInForm>()
  const navigate = useNavigate()
  const { t } = useTranslation()
  const { signInSchema } = useValidationSchemas()

  const {
    user,
    isLoading: loadingUser,
    error: usersError,
    verified
  } = useContext(UserContext)

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors }
  } = useForm<SignInForm>({
    resolver: yupResolver(signInSchema),
    defaultValues: {
      email: env.DEFAULT_USER_EMAIL,
      password: env.DEFAULT_USER_PASSWORD
    }
  })

  const { mutate: signIn, isLoading, error, isSuccess } = useSignIn()
  const { error: userError, isSuccess: userValid } = useUserVerify(query)

  useEffect(() => {
    if (isSuccess && verified) {
      localStorage.removeItem("location")
      navigate(RouteType.HOME)
    }
  }, [isSuccess, verified, usersError])

  useEffect(() => {
    if (error) {
      setValue("password", "")
    }
  }, [error])

  useEffect(() => {
    if (userValid && data) {
      signIn(data)
    }
  }, [userValid, data])

  const onLogin = (data: SignInForm) => {
    const { email } = data
    setData(data)
    setQuery("email=" + email)
  }

  const handleClipboardEvent = (e: ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault()
    return false
  }

  const changePWDIconType = () => {
    setPwdIcon(!showPwdIcon)
    if (pwdInputType == "text") {
      setPwdInputType("password")
    } else {
      setPwdInputType("text")
    }
  }

  return (
    <div className="signIn">
      <img src={"/images/login_bg.svg"} className="loginBG" />
      <div className="userAuth">
        <div className="userAuthForm">
          <form
            onSubmit={handleSubmit(onLogin)}
            role="form"
            autoComplete="off"
            noValidate
          >
            {userError && (
              <div
                style={{ width: "250px" }}
                className="alert alert-danger"
                role="alert"
              >
                {userError.message}
              </div>
            )}
            {(error || usersError) && (
              <div
                style={{ width: "250px" }}
                className="alert alert-danger"
                role="alert"
              >
                {error && error.message}
                {usersError && usersError}
              </div>
            )}
            <div className="userAuthWTKLogo">
              <img src={"/images/issuance_logo.svg"} />
            </div>
            <hr className="loginBorderLine" />
            <div className="userAuthLoginText mt-4">
              <h5>{t("Issuance Login")}</h5>
            </div>

            <div className="form-group mt-2">
              <input
                {...register("email")}
                name="email"
                placeholder={t("Email Address")}
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
            <div className="form-group mt-3">
              <div className="loginPassword">
                <input
                  {...register("password")}
                  name="password"
                  placeholder={t("Password")}
                  type={pwdInputType}
                  data-testid="password"
                  className={`userInput ${
                    errors.password?.message ? "is-invalid" : ""
                  }`}
                  aria-describedby="passwordError"
                  autoComplete="new-password"
                  onPaste={handleClipboardEvent}
                  onKeyDown={(event) => {
                    if (
                      (event.ctrlKey && event.key === "z") ||
                      (event.metaKey && event.key === "z")
                    ) {
                      event.preventDefault()
                      return false
                    }
                    if (event.ctrlKey && event.shiftKey && event.key === "z") {
                      event.preventDefault()
                      return false
                    }
                  }}
                />
                <div onClick={() => changePWDIconType()}>
                  {showPwdIcon ? <AiFillEye /> : <AiFillEyeInvisible />}
                </div>
              </div>
              {errors.password?.message && (
                <div
                  id="emailError"
                  className="signinForm-error"
                  style={{ textAlign: "left" }}
                >
                  {errors.password?.message}
                </div>
              )}
            </div>
            <div className="form-group mt-3">
              <button
                type="submit"
                className="btn btn-primary signInBtn"
                disabled={isLoading && verified}
                style={{ color: "#000000" }}
              >
                <div className="loader">
                  {t("Login")}&nbsp;
                  {isLoading ? (
                    <ThreeDots
                      height="20"
                      width="20"
                      color="#000"
                      ariaLabel="three-dots-loading"
                      visible={true}
                    />
                  ) : null}
                </div>
              </button>
              {/* <div className="col mt-4">
                <button
                  type="button"
                  className="btn btn-link"
                  disabled={isLoading}
                  onClick={() => navigate(RouteType.RESET_PASSWORD)}
                >
                  {t("Reset Password")}
                </button>
              </div>
              <div className="col mt-4">
                <button
                  type="button"
                  className="btn btn-link"
                  disabled={isLoading}
                  onClick={() => navigate(RouteType.CREATE_ACCOUNT)}
                >
                  {t("Create Account")}
                </button>
              </div> */}
              <div className="mt-4 mb-3">
                <a
                  onClick={() => navigate(RouteType.RESET_PASSWORD)}
                  className="forgotPassword"
                >
                  {t("Forgot Password")}
                </a>
              </div>
            </div>
            {/* <div className="form-group userAuthTerms">
              <p>
                <a href="">{t("Terms & Condition")}</a>
                {" | "}
                <a href="">{t("Privacy Policy")}</a>
              </p>
            </div> */}
          </form>
        </div>
        <div className="copyright">Copyright ©️ 2024 WadzPay Worldwide.</div>
      </div>
    </div>
  )
}

export default SignIn
