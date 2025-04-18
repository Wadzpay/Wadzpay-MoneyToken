import React from "react"
import { Navigate, Outlet } from "react-router-dom"

import { RouteType } from "../constants/routeTypes"
import SignedInUser from "./SignedInUser"
import SignedOutUser from "./SignedOutUser"

const PrivateRoute: React.FC = () => {
  return (
    <>
      <SignedInUser>
        <Outlet />
      </SignedInUser>
      <SignedOutUser>
        <Navigate to={RouteType.SIGN_IN} />
      </SignedOutUser>
    </>
  )
}

export default PrivateRoute
