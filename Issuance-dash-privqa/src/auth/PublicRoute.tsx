import React from "react"
import { Navigate, Outlet } from "react-router-dom"

import { RouteType } from "../constants/routeTypes"
import SignedInUser from "./SignedInUser"
import SignedOutUser from "./SignedOutUser"

const PublicRoute: React.FC = () => {
  return (
    <>
      <SignedInUser>
        <Navigate to={RouteType.HOME} />
      </SignedInUser>
      <SignedOutUser>
        <Outlet />
      </SignedOutUser>
    </>
  )
}

export default PublicRoute
