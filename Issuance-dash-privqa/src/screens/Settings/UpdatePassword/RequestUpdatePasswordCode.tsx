import React, { useEffect, useState } from "react"
import { useNavigate, useLocation } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { TIMER_MINUTES } from "src/api/index"
import {
  useRequestResetPasswordCode,
  useSubmitResetPasswordCode
} from "src/api/user"

import VerifyCode from "./VerifyCode"

type Props = {
  email: string
}

export const RequestUpdatePasswordCode: React.FC<Props> = ({
  email
}: Props) => {
  const [newPassword, setNewPassword] = useState(
    localStorage.getItem("newPassword") || ""
  )
  const navigate = useNavigate()

  const useVerifyCode = useSubmitResetPasswordCode()
  const useRequestCode = useRequestResetPasswordCode()

  useEffect(() => {
    // if (!email || !newPassword) {
    //   navigate(RouteType.RESET_PASSWORD)
    // }
    if (!email) {
      return
      // navigate(RouteType.RESET_PASSWORD)
    }
  }, [])

  const onSuccess = () => {
    navigate(RouteType.CHANGE_PASSWORD_SUCCESS)
  }

  return (
    <VerifyCode
      title="Enter Code"
      message="Please enter the code that has been sent to your registered email address."
      useVerifyCode={useVerifyCode}
      useRequestCode={useRequestCode}
      onSuccess={onSuccess}
      onBack={() => navigate(RouteType.ADMIN)}
      // extraParams={{
      //   email,
      //   newPassword
      // }}
      extraParams={{
        email
      }}
      timerMinutes={TIMER_MINUTES}
    />
  )
}

export default RequestUpdatePasswordCode
