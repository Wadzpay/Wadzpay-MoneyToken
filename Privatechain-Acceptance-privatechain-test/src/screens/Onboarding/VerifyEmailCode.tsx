import React, { useEffect, useContext } from "react"
import { useNavigate } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { TIMER_MINUTES } from "src/api/index"
import {
  useUserDetailsAndEmailOTP,
  useVerifyEmailOTPAndCreateUser
} from "src/api/user"
import { OnboardingContext } from "src/context/Onboarding"

import VerifyCode from "./VerifyCode"
interface Timer {
  test?: boolean
}
const VerifyEmailCode: React.FC<Timer> = () => {
  const navigate = useNavigate()

  const { onboardingValues, resetOnboardingValues } =
    useContext(OnboardingContext)

  useEffect(() => {
    if (
      !onboardingValues.createAccount.phoneNumber ||
      !onboardingValues.accountDetails.email
    ) {
      navigate(RouteType.CREATE_ACCOUNT)
    }
  }, [])

  const useVerifyCode = useVerifyEmailOTPAndCreateUser()
  const useRequestCode = useUserDetailsAndEmailOTP()

  const onSuccess = () => {
    resetOnboardingValues()
    navigate(RouteType.ONBOARDING_SUCCESS)
  }

  return (
    <>
      {onboardingValues.createAccount.phoneNumber && (
        <VerifyCode
          title="Verify Email"
          useVerifyCode={useVerifyCode}
          useRequestCode={useRequestCode}
          onSuccess={onSuccess}
          onBack={() => navigate(RouteType.ACCOUNT_DETAILS)}
          extraParams={{
            email: onboardingValues.accountDetails.email,
            phoneNumber: onboardingValues.createAccount.phoneNumber,
            password: onboardingValues.accountDetails.newPassword,
            isMerchantAdmin: onboardingValues.isMerchantAdmin
          }}
          timerMinutes={TIMER_MINUTES}
        />
      )}
    </>
  )
}

export default VerifyEmailCode
