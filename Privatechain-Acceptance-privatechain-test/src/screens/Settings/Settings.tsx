import React, { useContext } from "react"
import { Link, useLocation } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { MerchantContext } from "src/context/Merchant"
import Tab from "react-bootstrap/Tab"
import Tabs from "react-bootstrap/Tabs"
import { t } from "i18next"
import Card from "src/helpers/Card"
import AdminUserList from "src/components/ui/AdminUserList"

import Admin from "../Admin"
import Profile from "./profile/Profile"
import DefaultCurrency from "./defaultCurrency/DefaultCurrency"
import DefaultTimezone from "./defaultTimezone/DefaultTimezone"
import About from "../About"

function Settings(): JSX.Element {
  const location = useLocation()
  const { merchantDetails } = useContext(MerchantContext)

  return (
    <Card>
      <h3>Settings</h3>
      <Tabs defaultActiveKey="profile" className="mt-3 setings-tabs">
        <Tab eventKey="profile" title={t("Profile")}>
          <Profile />
        </Tab>
        <Tab eventKey="defaults" title={t("Default Preferences")}>
          <DefaultCurrency />
          {/* <br /> */}
          <DefaultTimezone />
        </Tab>
        {merchantDetails && merchantDetails.role == "MERCHANT_ADMIN" ? (
          <Tab eventKey="admin" title={t("Admin")}>
            <Admin />
          </Tab>
        ) : null}
        {merchantDetails && merchantDetails.role == "MERCHANT_READER" ? (
          <Tab eventKey="admin" title={t("Users")}>
            <AdminUserList />
          </Tab>
        ) : null}
        <Tab eventKey="about" title={t("About")}>
          <About />
        </Tab>
      </Tabs>
    </Card>
  )
}

export default Settings
