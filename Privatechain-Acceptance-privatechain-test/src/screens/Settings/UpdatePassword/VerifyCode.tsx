import React, { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { useTranslation } from "react-i18next"
import { useLocation } from "react-router-dom"
import { yupResolver } from "@hookform/resolvers/yup"
import { VerifyCodeForm } from "src/constants/formTypes"
import { useValidationSchemas } from "src/constants/validationSchemas"
import { addMinutes } from "date-fns"
import { calculateTimeLeft } from "src/api/constants"
import DOMPurify from "dompurify"

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

  return (
    <div className="updatePasswordOTPForm">
      <div className="col-md-4">
        <div className="">
          <form autoComplete="off" onSubmit={handleSubmit(onNext)} role="form" noValidate>
            {error && (
              <div className="alert alert-danger" role="alert">
                {error.message}
              </div>
            )}
            {showCodeStatus && (
              <div className="alert alert-success">{t("Code Sent")}</div>
            )}
            <div className="userAuthLoginText">
              <h5>{t("Enter Code")}</h5>
              <p style={{ lineHeight: "20px" }}>
                {message && (
                  <span className="mb-2">
                    {message}
                    {/* <b>{location.state.email}</b> */}
                    {/* <b>{extraParams && extraParams.e}</b> */}
                  </span>
                )}
              </p>
            </div>
            <div className="form-group mt-4">
              <input aria-autocomplete='both' aria-haspopup="false"
                type="text"
                data-testid="codeInput"
                placeholder={t("Code")}
/*                 autoComplete="true"
 */                className={`userInput ${errorMessage ? "is-invalid" : ""}`}
                aria-describedby="codeError"
                {...register("code")}
              />
              {errors.code?.message && (
                <div id="codeError" className="invalid-feedback">
                  {errors.code?.message}
                </div>
              )}
            </div>
            <div className="form-group mt-3">
              <input aria-autocomplete='both' aria-haspopup="false"
                type="password"
/*                 autoComplete="true"
 */                {...register("newPassword")}
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
              <input aria-autocomplete='both' aria-haspopup="false"
/*               autoComplete="true"
 */                type="password"
                {...register("confirmPassword")}
                placeholder={`${t("Re-enter Password")}`}
                data-testid="confirmPassword"
                className={`userInput ${
                  errors.confirmPassword?.message ? "is-invalid" : ""
                }`}
                aria-describedby="confirmPasswordError"
                onChange={(e) =>{
                  const sanitizedInput = DOMPurify.sanitize(e.target.value)
                  localStorage.setItem("newPassword", sanitizedInput)
                }
                }
              />
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
                  className="btn btn-light signInBtn wdz-btn-md"
                  style={{ padding: 0 }}
                >
                  {t("Resend code")}
                </button>
              )}
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}

export default VerifyCode
