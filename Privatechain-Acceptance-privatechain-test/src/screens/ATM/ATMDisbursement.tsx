import React, { useState } from "react"
import { t } from "i18next"
import Form from "react-bootstrap/esm/Form"
import Button from "react-bootstrap/esm/Button"
import { RouteType } from "src/constants/routeTypes"
import { Link } from "react-router-dom"

export default function ATMDisbursement(): JSX.Element {
  return (
    <div className="container" style={{ width: "400px" }}>
      <div className="atm">
        <h3>Disbursement Successful</h3>
      </div>
      <div>Please collect the requested fund</div>
      <div
        className="row justify-content-center"
        style={{ fontSize: "30px", fontWeight: 600, paddingTop: "20px" }}
      >
        <Link
          to={RouteType.HOME}
          role="button"
          className={`btn wdz-btn-primary`}
        >
          EXIT
        </Link>
      </div>
    </div>
  )
}
