import React, { useContext } from "react"
import { Link, useLocation } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { MerchantContext } from "src/context/Merchant"

import Balances from "./Balances"
import MerchantName from "../../components/ui/MerchantName"

function Dashboard(): JSX.Element {
  const location = useLocation()
  const { merchantDetails } = useContext(MerchantContext)

  return (
    <div>
      <div className="d-flex justify-content-between">
        <h2>{merchantDetails?.merchant?.name}</h2>
        {/* <div>
          <Link
            to={RouteType.P2P_TRANSACTION}
            data-testid="p2pTransactionLink"
            className="btn btn-secondary"
            role="button"
            state={{ from: location.pathname + location.search }}
          >
            M2C Transaction
          </Link>
          {merchantDetails?.role === "MERCHANT_ADMIN" && (
            <Link
              to={RouteType.ADMIN}
              data-testid="adminLink"
              className="btn btn-secondary ms-1"
              role="button"
            >
              Admin
            </Link>
          )}
        </div> */}
      </div>
      <div className="mt-2">
        <Balances />
      </div>
    </div>
  )
}

export default Dashboard
