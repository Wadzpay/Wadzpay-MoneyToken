import React from "react"
import { Link } from "react-router-dom"
import { Button } from "antd"
import PageHeading from "src/components/ui/PageHeading"
import { RouteType } from "src/constants/routeTypes"
import { useSelector } from "react-redux"
import { ROOTSTATE } from "src/utils/modules"

const RoleManagement = () => {
  // Selector
  const buttonColor = useSelector(
    (store: ROOTSTATE) => store.appConfig.buttonColor
  )

  return (
    <div>
      <PageHeading title="Role Management" />
      <hr style={{ height: "1px", background: "#ECECEC", border: "none" }} />
      {/* <div className="d-flex justify-content-between">
        <div>left</div>
        <div>right</div>
      </div> */}

      <div className="text-center vertical-center">
        <img
          className="mb-3"
          style={{
            border: "1px solid #000",
            padding: "5px",
            borderRadius: "5px"
          }}
          src="/images/role-management.svg"
        />
        <h5
          style={{
            fontWeight: 600,
            fontSize: "19px",
            font: "Rubik",
            lineHeight: "21.33px"
          }}
        >
          It seems to be no roles to manage
        </h5>
        <p style={{ fontSize: "14px" }}>
          Write here lorem ipsum text - dummy text
        </p>
        <Link to={RouteType.ROLE_CREATE}>
          <Button
            style={{
              background: buttonColor,
              color: "#000"
            }}
          >
            Create Role
          </Button>
        </Link>
      </div>
    </div>
  )
}

export default RoleManagement
