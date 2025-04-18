import React, { useContext, useEffect, useState } from "react"
import { t } from "i18next"
import { Button } from "antd"
import { OnboardingContext } from "src/context/Onboarding"
import { useRequestResetPasswordCode } from "src/api/user"

import RequestUpdatePasswordCode from "./RequestUpdatePasswordCode"

const AcceptInvitations: React.FC = () => {
  const { onboardingValues, resetOnboardingValues } =
    useContext(OnboardingContext)
  const [showOTPInput, setShowOTPInput] = useState(false)
  const [userEmail, setUserEmail] = useState("")
  const [loadings, setLoadings] = useState<boolean>(false)

  const {
    mutate: requestResetPasswordCode,
    isLoading,
    isSuccess,
    error,
    reset: resetRequestResetPasswordCode
  } = useRequestResetPasswordCode()

  const onSendResetPasswordEmail = async (email: string) => {
    requestResetPasswordCode({ email })
  }

  useEffect(() => {
    if (isSuccess) {
      setLoadings(false)
      setShowOTPInput(true)
      resetRequestResetPasswordCode()
    }
  }, [isSuccess])

  const onNext = () => {
    if (onboardingValues) {
      if (onboardingValues?.accpetInvitations?.email) {
        setLoadings(true)
        // return
        setUserEmail(onboardingValues?.accpetInvitations?.email)
        onSendResetPasswordEmail(onboardingValues?.accpetInvitations?.email)
      }
    }
  }

  return (
    <>
      <div className="signIn">
        <img src={"/images/login_bg.svg"} className="loginBG" />
        <div className="userAuth">
          <div className="userAuthForm">
            <div className="userAuthWTKLogo">
              <img src={"/images/black_logo.svg"} />
            </div>
            <hr className="loginBorderLine" />
            <div className="userAuthLoginText mt-4">
              <h5>{t("Set New Password")}</h5>
            </div>
            <div className="form-group mt-2">
              <input
                readOnly
                placeholder={t("Email Address")}
                type="email"
                data-testid="email"
                className={"userInput"}
                aria-describedby="emailError"
                value={onboardingValues.accpetInvitations.email}
              />
              {onboardingValues.accpetInvitations.email && <></>}
            </div>
            {showOTPInput ? (
              <RequestUpdatePasswordCode email={userEmail} />
            ) : (
              <div className="form-group mt-3">
                <Button
                  style={{ background: "#00599d" }}
                  onClick={onNext}
                  type="primary"
                  loading={loadings}
                >
                  {t("Create Password")}
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  )
}

export default AcceptInvitations
