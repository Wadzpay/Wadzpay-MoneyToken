import React, { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { useTranslation } from "react-i18next"
import { useLocation } from "react-router-dom"
import { yupResolver } from "@hookform/resolvers/yup"
import { AiFillEyeInvisible, AiFillEye } from "react-icons/ai"
import { VerifyCodeForm } from "src/constants/formTypes"
import { useValidationSchemas } from "src/constants/validationSchemas"
import { addMinutes } from "date-fns"
import { calculateTimeLeft } from "src/api/constants"
import { allowOnlyNumber } from "src/utils"

export type UseCustomMutation<T> = {
  /* eslint-disable-next-line @typescript-eslint/no-explicit-any */
  mutate: (body: T) => void | Promise<any>
  isSuccess: boolean
  isLoading: boolean
  error: Error | null
}

type Props<T> = {
  title: string
  timerMinutes: number
  extraParams: T
  useVerifyCode: UseCustomMutation<T & { code: string }>
  useRequestCode: UseCustomMutation<T>
  onSuccess: () => void
  onBack?: () => void
  message?: string
}

// Can't specify React.FC<Props> return type because of generics
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
const VerifyCode = <T extends Record<string, unknown>>({
  title,
  message,
  timerMinutes,
  extraParams,
  useVerifyCode,
  useRequestCode,
  onSuccess,
  onBack
}: Props<T>) => {
  const { t } = useTranslation()
  const { verifyCodeSchema } = useValidationSchemas()
  const location = useLocation()

  const [showPwdIcon, setPwdIcon] = useState(false)
  const [showConfirmPwdIcon, setConfirmPwdIcon] = useState(false)
  const [pwdInputType, setPwdInputType] = useState("password")
  const [confirmPwdInputType, setConfirmPwdInputType] = useState("password")
  const [isInputReadOnly, setIsInputReadOnly] = useState<boolean>(true)
  const { mutate: verifyCode, isLoading, error, isSuccess } = useVerifyCode

  const {
    mutate: requestCode,
    isSuccess: isRequestCodeSuccess,
    error: requestCodeError
  } = useRequestCode

  const {
    handleSubmit,
    getValues,
    register,
    reset,
    formState: { errors }
  } = useForm<VerifyCodeForm>({
    resolver: yupResolver(verifyCodeSchema)
  })

  useEffect(() => {
    const timer = setTimeout(() => {
      setTimeLeft(calculateTimeLeft(targetDate))
    }, 1000)
    return () => clearTimeout(timer)
  })

  const [targetDate, setTargetDate] = useState(
    addMinutes(new Date(), timerMinutes)
  )

  const [timeLeft, setTimeLeft] = useState(timerMinutes * 60)
  const [showCodeStatus, setShowCodeStatus] = useState<boolean>(false)
  const [codeTitle, setCodeTitle] = useState<string>(title)
  const [codeMessage, setCodeMessage] = useState<any>(message)

  useEffect(() => {
    if (isRequestCodeSuccess) {
      showMessage()
      // Reset timer
      setTargetDate(addMinutes(new Date(), timerMinutes))
      setTimeLeft(timerMinutes * 60)
    }
  }, [isRequestCodeSuccess])

  function showMessage() {
    setShowCodeStatus(true)
    setTimeout(() => {
      setShowCodeStatus(false)
    }, 2000)
  }
  const resendOTP = () => {
    reset()
    requestCode(extraParams)
    setCodeMessage(
      "Please enter the new code that has been re-sent to your registered email address."
    )
    setCodeTitle("Enter New Code")
  }

  const onNext = () => {
    verifyCode({
      code: getValues().code,
      newPassword: getValues().newPassword,
      ...extraParams
    })
  }

  useEffect(() => {
    if (isSuccess) {
      onSuccess()
    }
  }, [isSuccess])

  const errorMessage =
    errors.code?.message || error?.message || requestCodeError

  const handleClickEvent = () => {
    setIsInputReadOnly(false)
  }

  const changePWDIconType = () => {
    setPwdIcon(!showPwdIcon)
    if (pwdInputType == "text") {
      setPwdInputType("password")
    } else {
      setPwdInputType("text")
    }
  }

  const changeConfirmPWDIconType = () => {
    setConfirmPwdIcon(!showConfirmPwdIcon)
    if (confirmPwdInputType == "text") {
      setConfirmPwdInputType("password")
    } else {
      setConfirmPwdInputType("text")
    }
  }

  return (
    <div className="signIn">
      <img src={"/images/login_bg.svg"} className="loginBG" />
      <div className="userAuth">
        <div className="userAuthForm">
          <form
            onSubmit={handleSubmit(onNext)}
            role="form"
            noValidate
            autoComplete="off"
          >
            {error && (
              <div
                style={{ width: "250px", lineHeight: "20px" }}
                className="alert alert-danger"
                role="alert"
              >
                {error.message}
              </div>
            )}
            {showCodeStatus && (
              <div
                style={{ width: "250px", lineHeight: "20px" }}
                className="alert alert-success"
              >
                {t("Code Sent")}
              </div>
            )}
            <div className="userAuthWTKLogo">
              <img src={"/images/issuance_logo.svg"} />
            </div>
            <div className="userAuthLoginText">
              <h5>{t(codeTitle)}</h5>
              <p style={{ width: "250px", lineHeight: "20px" }}>
                {codeMessage && (
                  <span className="mb-2">
                    {codeMessage}
                    <b>{location.state.email}</b>
                  </span>
                )}
              </p>
            </div>
            <div className="form-group mt-4">
              <input
                type="text"
                data-testid="codeInput"
                placeholder={t("Code")}
                className={`userInput ${errorMessage ? "is-invalid" : ""}`}
                aria-describedby="codeError"
                autoComplete="off"
                readOnly={isInputReadOnly}
                onKeyPress={allowOnlyNumber}
                onClick={handleClickEvent}
                {...register("code")}
              />
              {errors.code?.message && (
                <div
                  id="codeError"
                  className="invalid-feedback"
                  style={{ textAlign: "left" }}
                >
                  {errors.code?.message}
                </div>
              )}
            </div>
            <div className="form-group mt-3">
              <input
                type={pwdInputType}
                {...register("newPassword")}
                placeholder={`${t("New Password")}`}
                data-testid="newPassword"
                className={`userInput ${
                  errors.newPassword?.message ? "is-invalid" : ""
                }`}
                aria-describedby="newPasswordError"
                autoComplete="off"
              />
              <div
                className="passwordShowIcon"
                onClick={() => changePWDIconType()}
              >
                {showPwdIcon ? <AiFillEye /> : <AiFillEyeInvisible />}
              </div>
              {errors.newPassword?.message && (
                <div
                  style={{ width: "250px", textAlign: "left" }}
                  id="newPasswordError"
                  className="invalid-feedback"
                >
                  {errors.newPassword?.message}
                </div>
              )}
            </div>
            <div className="form-group mt-3">
              <input
                type={confirmPwdInputType}
                {...register("confirmPassword")}
                placeholder={`${t("Confirm New Password")}`}
                data-testid="confirmPassword"
                className={`userInput ${
                  errors.confirmPassword?.message ? "is-invalid" : ""
                }`}
                aria-describedby="confirmPasswordError"
                autoComplete="off"
                // onChange={(e) =>
                //   localStorage.setItem("newPassword", e.target.value)
                // }
              />
              <div
                className="passwordShowIcon"
                onClick={() => changeConfirmPWDIconType()}
              >
                {showConfirmPwdIcon ? <AiFillEye /> : <AiFillEyeInvisible />}
              </div>
              {errors.confirmPassword?.message && (
                <div
                  style={{ width: "250px", textAlign: "left" }}
                  id="confirmPasswordError"
                  className="invalid-feedback"
                >
                  {errors.confirmPassword?.message}
                </div>
              )}
            </div>
            <div className="form-group row row-cols-center">
              {onBack && (
                <div className="col mt-4">
                  <button
                    type="button"
                    className="btn btn-secondary signInBtnGrey"
                    disabled={isLoading}
                    onClick={() => onBack()}
                  >
                    {t("Back")}
                  </button>
                </div>
              )}
              <div className="col mt-4">
                <button
                  type="submit"
                  className="btn btn-primary signInBtn"
                  disabled={isLoading}
                  style={{ color: "#000000" }}
                >
                  {t("Next")}
                </button>
              </div>
            </div>
            <div className="col  mt-4 d-flex align-items-center justify-content-center">
              {timeLeft > 0 ? (
                t("No code? Wait", {
                  timeLeft
                })
              ) : (
                <button
                  type="reset"
                  data-testid="resendOTP"
                  onClick={() => resendOTP()}
                  className="btn resendOTP signInBtn"
                  style={{ color: "#ffffff", width: "100%" }}
                >
                  {t("Resend code")}
                </button>
              )}
            </div>
          </form>
        </div>
        <div className="copyright">Copyright ©️ 2024 WadzPay Worldwide.</div>
      </div>
    </div>
  )
}

export default VerifyCode
