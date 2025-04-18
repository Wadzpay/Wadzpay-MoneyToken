import React, { useEffect, useState } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { TIMER_MINUTES } from "src/api/index"
import {
  useRequestResetPasswordCode,
  useSubmitResetPasswordCode
} from "src/api/user"

import VerifyCode from "../Onboarding/VerifyCode"
interface Timer {
  test?: boolean
}

const SubmitResetPasswordCode: React.FC<Timer> = () => {
  // const [newPassword, setNewPassword] = useState(
  // localStorage.getItem("newPassword") || ""
  // )
  const { state } = useLocation()
  // const { email, newPassword } = state // Read values passed on state
  const { email } = state // Read values passed on state
  const navigate = useNavigate()

  const useVerifyCode = useSubmitResetPasswordCode()
  const useRequestCode = useRequestResetPasswordCode()

  useEffect(() => {
    // if (!email || !newPassword) {
    //   navigate(RouteType.RESET_PASSWORD)
    // }
    if (!email) {
      navigate(RouteType.FORGOT_PASSWORD)
    }
  }, [])

  const onSuccess = () => {
    navigate(RouteType.RESET_PASSWORD_SUCCESS)
  }

  return (
    <VerifyCode
      title="Enter Code"
      message="Please enter the code that has been sent to your registered email address."
      useVerifyCode={useVerifyCode}
      useRequestCode={useRequestCode}
      onSuccess={onSuccess}
      onBack={() => navigate(RouteType.FORGOT_PASSWORD)}
      extraParams={{
        email
      }}
      // extraParams={{
      //   email
      // }}
      timerMinutes={TIMER_MINUTES}
    />
  )
}

export default SubmitResetPasswordCode
