import React from "react"
import { Link, useNavigate } from "react-router-dom"
import { t } from "i18next"
import { RouteType } from "src/constants/routeTypes"
import { ATM_BANK_NAME } from "src/constants/Defaults"
export default function ATM(): JSX.Element {
  return (
    <div className="container" style={{ textAlign: "center", width: "400px" }}>
      <div className="atm">
        <h3>Welcome to Wadzpay {ATM_BANK_NAME}</h3>
      </div>
      <h5>What would you like to use?</h5>
      <div>Please select the payment mode for withdrawl</div>
      <div
        className="row justify-content-center"
        style={{ fontSize: "30px", fontWeight: 600, paddingTop: "20px" }}
      >
        <Link
          role="button"
          className="btn wdz-btn-primary"
          to={RouteType.ATM_ENTRY}
        >
          {t("CARD")}
        </Link>
      </div>
      <div
        className="row justify-content-center"
        style={{ fontSize: "30px", fontWeight: 600, paddingTop: "20px" }}
      >
        <Link
          role="button"
          className="btn wdz-btn-primary"
          to={RouteType.ATM_ENTRY}
        >
          {t("WALLET")}
        </Link>
      </div>
    </div>
  )
}
