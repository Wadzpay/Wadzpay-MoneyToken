import React from "react"
import { Link } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"

function Page404(): JSX.Element {
  return (
    <div>
      <div>
        <h2 data-testid="title">
          The page you were looking for does not exist.
        </h2>
        <p>You may have mistyped the address or the page may have moved</p>
      </div>
      <Link to={RouteType.HOME}>Back to home</Link>
    </div>
  )
}

export default Page404
