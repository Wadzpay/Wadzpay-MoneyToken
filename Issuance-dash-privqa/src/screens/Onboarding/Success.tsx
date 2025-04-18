import React from "react"
import { useTranslation } from "react-i18next"
import { RouteType } from "src/constants/routeTypes"
import { Link } from "react-router-dom"

const Success: React.FC = () => {
  const { t } = useTranslation()

  return (
    <div className="wp-form">
      <h2>{t("Your account was created")}</h2>
      <p>
        <Link to={RouteType.SIGN_IN}>{t("Sign In")}</Link>
        {t(" to access the dashboard")}
      </p>
    </div>
  )
}

export default Success
