import React, { useContext, useEffect } from "react"
import { useNavigate } from "react-router-dom"
import { TIMER_MINUTES } from "src/api"
import { useSendPhoneOTP, useVerifyPhoneOTP } from "src/api/otp"
import { RouteType } from "src/constants/routeTypes"
import { OnboardingContext } from "src/context"

import VerifyCode from "./VerifyCode"

const VerifyPhoneCode: React.FC = () => {
  const navigate = useNavigate()

  const { onboardingValues } = useContext(OnboardingContext)

  useEffect(() => {
    if (!onboardingValues.createAccount.phoneNumber) {
      navigate(RouteType.CREATE_ACCOUNT)
    }
  }, [])

  const useVerifyCode = useVerifyPhoneOTP()
  const useRequestCode = useSendPhoneOTP()

  const onSuccess = () => {
    navigate(RouteType.ACCOUNT_DETAILS)
  }

  return (
    <>
      {onboardingValues.createAccount.phoneNumber && (
        <VerifyCode
          title="Verify Phone Number"
          useVerifyCode={useVerifyCode}
          useRequestCode={useRequestCode}
          onSuccess={onSuccess}
          onBack={() => navigate(RouteType.CREATE_ACCOUNT)}
          extraParams={{
            phoneNumber: onboardingValues.createAccount.phoneNumber
          }}
          timerMinutes={TIMER_MINUTES}
        />
      )}
    </>
  )
}

export default VerifyPhoneCode
