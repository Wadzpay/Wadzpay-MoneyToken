import React from "react"
import { Button, Tooltip } from "antd"
import PageHeading from "src/components/ui/PageHeading"
import { RouteType } from "src/constants/routeTypes"
import { useSelector } from "react-redux"
import { ROOTSTATE } from "src/utils/modules"

const UserManagement = () => {
  // Selector
  const buttonColor = useSelector(
    (store: ROOTSTATE) => store.appConfig.buttonColor
  )

  return (
    <div>
      <PageHeading title="MBSB - User Management" />
      <hr style={{ height: "1px", background: "#ECECEC", border: "none" }} />

      <div className="text-center vertical-center">
        <img
          className="mb-3"
          style={{
            border: "1px solid #000",
            padding: "5px",
            borderRadius: "5px"
          }}
          src="/images/user-management.svg"
        />
        <h5
          style={{
            fontWeight: 600,
            fontSize: "19px",
            font: "Rubik",
            lineHeight: "21.33px"
          }}
        >
          It seems to be no users to manage
        </h5>
        <p style={{ fontSize: "14px" }}>
          Write here lorem ipsum text - dummy text
        </p>
        <Tooltip title="comming soon">
          <Button
            style={{
              background: buttonColor,
              color: "#000"
            }}
          >
            Create User
          </Button>
        </Tooltip>
      </div>
    </div>
  )
}

export default UserManagement
