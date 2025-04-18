import React, { useEffect } from "react"
import { useTranslation } from "react-i18next"
import { RouteType } from "src/constants/routeTypes"
import { useNavigate } from "react-router-dom"
import { signOutAsync } from "src/auth/AuthManager"

const UpdateSuccess: React.FC = () => {
  const { t } = useTranslation()
  const navigate = useNavigate()

  useEffect(() => {
    setTimeout(() => {
      signOutAsync()
      navigate(RouteType.SIGN_IN)
    }, 3000)
  }, [])

  return (
    <div className="updatePasswordOTPForm">
      <div className="col-md-4">
        <h4>{t("Password update successful")}</h4>
        <h6>{t("You will be logged out automatically...")}</h6>
      </div>
    </div>
  )
}

export default UpdateSuccess
