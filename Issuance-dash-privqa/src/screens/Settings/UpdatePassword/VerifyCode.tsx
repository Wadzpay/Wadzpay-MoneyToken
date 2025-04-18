import React, { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { useTranslation } from "react-i18next"
import { useLocation } from "react-router-dom"
import { yupResolver } from "@hookform/resolvers/yup"
import { AiFillEyeInvisible, AiFillEye } from "react-icons/ai"
import { message as antMessage } from "antd"
import { VerifyCodeForm } from "src/constants/formTypes"
import { useValidationSchemas } from "src/constants/validationSchemas"
import { addMinutes } from "date-fns"
import { calculateTimeLeft } from "src/api/constants"

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
  const [codeText, setCodeText] = useState<boolean>(false)
  const [timeLeft, setTimeLeft] = useState<number>(0)
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
    if (timeLeft) {
      const timer = setTimeout(() => {
        setTimeLeft(calculateTimeLeft(targetDate))
      }, 1000)
      return () => clearTimeout(timer)
    }
  }, [timeLeft])

  const [targetDate, setTargetDate] = useState(
    addMinutes(new Date(), timerMinutes)
  )

  useEffect(() => {
    if (isRequestCodeSuccess) {
      setCodeText(true)
      antMessage.success("Code sent to registered email address!")
      // Reset timer
      setTargetDate(addMinutes(new Date(), timerMinutes))
      setTimeLeft(timerMinutes * 60)
    }
    if (requestCodeError) {
      reset()
      antMessage.error(requestCodeError.message)
    }
  }, [isRequestCodeSuccess, requestCodeError])

  const resendOTP = () => {
    reset()
    requestCode(extraParams)
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

  const handleClickEvent = () => {
    setIsInputReadOnly(false)
  }

  return (
    <div className="updatePasswordOTPForm">
      <div className="col-lg-4 col-sm-6">
        <div className="">
          <form
            style={{ position: "relative" }}
            onSubmit={handleSubmit(onNext)}
            role="form"
            noValidate
          >
            {error && (
              <div className="alert alert-danger" role="alert">
                {error.message}
              </div>
            )}
            <div className="userAuthLoginText">
              <h5>{t("Enter Code")}</h5>
              <p style={{ lineHeight: "20px" }}>
                {message && (
                  <span className="mb-2">
                    {codeText
                      ? message
                      : "Click on send code button to send code on registered email address."}
                    {/* <b>{location.state.email}</b> */}
                    {/* <b>{extraParams && extraParams.e}</b> */}
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
                {...register("code")}
                autoComplete="off"
                readOnly={isInputReadOnly}
                onClick={handleClickEvent}
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
              <span
                className="custom-password-icon"
                onClick={() => changePWDIconType()}
              >
                {showPwdIcon ? <AiFillEye /> : <AiFillEyeInvisible />}
              </span>
              {errors.newPassword?.message && (
                <div id="newPasswordError" className="invalid-feedback">
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
                onChange={(e) =>
                  localStorage.setItem("newPassword", e.target.value)
                }
              />
              <span
                className="custom-password-icon"
                onClick={() => changeConfirmPWDIconType()}
              >
                {showConfirmPwdIcon ? <AiFillEye /> : <AiFillEyeInvisible />}
              </span>
              {errors.confirmPassword?.message && (
                <div id="confirmPasswordError" className="invalid-feedback">
                  {errors.confirmPassword?.message}
                </div>
              )}
            </div>
            <div className="form-group row row-cols-center">
              {onBack && (
                <div className="col mt-4">
                  <button
                    type="button"
                    className="btn btn-secondary signInBtnGrey wdz-grey-bg-color"
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
                  className="btn signInBtn"
                  disabled={isLoading || !codeText}
                >
                  {t("Submit")}
                </button>
              </div>
            </div>
            <div className="col mt-4 d-flex align-items-center justify-content-center">
              {timeLeft > 0 ? (
                t("No code? Wait", {
                  timeLeft
                })
              ) : (
                <button
                  type="reset"
                  data-testid="resendOTP"
                  onClick={() => resendOTP()}
                  className="btn btn-light resendOTP"
                  style={{ padding: 0 }}
                >
                  {t(codeText ? "Resend code" : "Send code")}
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
