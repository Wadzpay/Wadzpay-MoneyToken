import React, { useContext, useEffect, useState } from "react"
import { t } from "i18next"
import { UserContext } from "src/context/User"
import { useRequestResetPasswordCode } from "src/api/user"
import Card from "src/helpers/Card"

import RequestUpdatePasswordCode from "./RequestUpdatePasswordCode"

function ChangePassword(): JSX.Element {
  const { user } = useContext(UserContext)
  const [showOTPInput, setShowOTPInput] = useState(false)
  const [userEmail, setUserEmail] = useState("")

  const {
    mutate: requestResetPasswordCode,
    isLoading,
    isSuccess,
    error,
    reset: resetRequestResetPasswordCode
  } = useRequestResetPasswordCode()

  const onSendResetPasswordEmail = async (email: string) => {
    await requestResetPasswordCode({ email })
  }

  useEffect(() => {
    if (isSuccess) {
      setShowOTPInput(true)
      // resetRequestResetPasswordCode()
    }
  }, [isSuccess])

  useEffect(() => {
    if (user) {
      if (user?.attributes?.email) {
        // return
        setUserEmail(user?.attributes?.email)
        onSendResetPasswordEmail(user?.attributes?.email)
      }
    }
  }, [user])

  return (
    <Card>
      <h2>{t("Change Password")}</h2>
      <div>
        {showOTPInput ? <RequestUpdatePasswordCode email={userEmail} /> : null}
      </div>
    </Card>
  )
}

export default ChangePassword
