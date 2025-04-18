import React from "react"
import { t } from "i18next"
import { Link } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import AdminUserList from "src/components/ui/AdminUserList"
import Card from "src/helpers/Card"

function Users(): JSX.Element {
  return (
    <Card>
      <h2>{t("Users")}</h2>
      <ul className="nav flex-column">
        {/* <li className="nav-item">
          <Link
            to={RouteType.ADMIN_USERS_INVITE}
            className="nav-link ps-0"
            data-testid="inviteUserLink"
          >
            {t("Invite User")}
          </Link>
        </li> */}
        <li className="nav-item">
          <div className="nav-link ps-0">
            <Link
              to={RouteType.ADMIN}
              data-testid="backButton"
              className="btn btn-secondary wdz-btn-grey wdz-btn-xl"
              role="button"
            >
              {t("Back")}
            </Link>
          </div>
        </li>
      </ul>
      <AdminUserList />
    </Card>
  )
}

export default Users
