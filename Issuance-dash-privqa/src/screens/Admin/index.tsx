import React from "react"
import { Link } from "react-router-dom"
import { t } from "i18next"
import { RouteType } from "src/constants/routeTypes"

function Admin(): JSX.Element {
  return (
    <div className="mt-4 ml-2">
      <ul className="nav flex-column">
        <li className="nav-item">
          <Link
            to={RouteType.ADMIN_USERS}
            className="nav-link ps-0"
            data-testid="usersLink"
          >
            {t("Users")}
          </Link>
        </li>
        <li className="nav-item">
          <Link
            to={RouteType.ADMIN_API_KEYS_CREATE}
            className="nav-link ps-0"
            data-testid="apiKeysLink"
          >
            {t("API Keys")}
          </Link>
        </li>
        {/* <li className="nav-item">
          <div className="nav-link ps-0">
            <Link
              to={RouteType.HOME}
              data-testid="backButton"
              className="btn btn-secondary"
              role="button"
            >
              {t("Back")}
            </Link>
          </div>
        </li> */}
      </ul>
    </div>
  )
}

export default Admin
