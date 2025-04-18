import { t } from "i18next"
import React from "react"
import { Link } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import Card from "src/helpers/Card"

function APIKeys(): JSX.Element {
  return (
    <Card>
      <h2>{t("API Key")}</h2>
      <ul className="nav flex-column">
        <li className="nav-item">
          <Link
            to={RouteType.ADMIN_API_KEYS_CREATE}
            className="nav-link ps-0"
            data-testid="createKeyLink"
          >
            {t("Generate API Key")}
          </Link>
        </li>
        <li className="nav-item">
          <div className="nav-link ps-0">
            <Link
              to={RouteType.ADMIN}
              data-testid="backButton"
              className="btn btn-secondary wdz-btn-grey wdz-btn-md"
              role="button"
            >
              {t("Back")}
            </Link>
          </div>
        </li>
      </ul>
    </Card>
  )
}

export default APIKeys
