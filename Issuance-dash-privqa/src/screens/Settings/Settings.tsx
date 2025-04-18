import React, { useContext } from "react"
import { Link, useLocation } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { t } from "i18next"
import Card from "src/helpers/Card"

import Profile from "./profile/Profile"
import DefaultPreferences from "./defaultPreferences/DefaultPreferences"
import About from "../About"
import P2pConfiguration from "./p2pConfiguration/P2pConfiguration"
import PageHeading from "../../components/ui/PageHeading"

function Settings(): JSX.Element {
  const location = useLocation()

  return (
    <Card>
      <PageHeading title="Settings" />
      <div className="p-2 ms-1">
        {/* <div className="row bg-white boxShadow rounded">
          <div className="col-xl-12 col-lg-12 col-sm-6 mt-2">
            <div className="card-body">
              <Profile />
            </div>
          </div>
        </div>
        <div className="row bg-white boxShadow rounded mt-2">
          <div className="col-xl-12 col-lg-12 col-sm-6">
            <div className="card-body">
              <DefaultPreferences />
            </div>
          </div>
        </div>
        <div className="row bg-white boxShadow rounded mt-2">
          <div className="col-xl-12 col-lg-12 col-sm-6 mt-30">
            <div className="card-body">
              <p>
                <b>Admin</b>
              </p>
              <Link
                to={RouteType.ADMIN_USERS_INVITE}
                className="nav-link ps-0"
                data-testid="apiKeysLink"
              >
                {t("Create Role")}
              </Link>
              <Link
                to={RouteType.ADMIN_USERS_INVITE}
                className="nav-link ps-0"
                data-testid="apiKeysLink"
              >
                {t("Create User")}
              </Link>
              <Link
                to={RouteType.FIAT_CURRENT_LIMIT}
                className="nav-link ps-0"
                data-testid="apiKeysLink"
              >
                {t("Fiat Current Limits")}
              </Link>
            </div>
          </div>
        </div> */}
        {/* <div className="row bg-white boxShadow rounded mt-2">
          <div className="col-xl-12 col-lg-12 col-sm-6">
            <div className="card-body">
              <About />
            </div>
          </div>
        </div> */}
        <div className="row bg-white boxShadow rounded mt-2">
          <div className="col-xl-12 col-lg-12 col-sm-6">
            <div className="card-body">
              <P2pConfiguration />
            </div>
          </div>
        </div>
      </div>
    </Card>
  )
}

export default Settings
