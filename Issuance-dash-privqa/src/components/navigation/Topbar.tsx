import { t } from "i18next"
import React, { useContext, useState } from "react"
import dayjs from "dayjs"
import utc from "dayjs/plugin/utc"
import tz from "dayjs/plugin/timezone"
import { IssuanceContext } from "src/context/Merchant"
import NavigationMobile from "src/components/navigation/NavigationMobile"
import { Dropdown, Tooltip } from "antd"
import { MenuOutlined } from "@ant-design/icons"

import { timeZone, hasImageExtension } from "./../../../src/utils"

const Topbar: React.FC = () => {
  const { issuanceDetails, institutionDetails } = useContext(IssuanceContext)
  const [showMenu, setShowMenu] = useState<boolean>(false)
  dayjs.extend(utc)
  dayjs.extend(tz)

  console.log("topbar", institutionDetails)
  const dateFormat = () => {
    const label = timeZone.label
    const str = label.slice(11)
    return (
      dayjs(Date()).tz(timeZone.value).format("MMMM D, YYYY h:mm A") +
      " " +
      `(${str})`
    )
  }

  const items = [
    {
      key: "1",
      label: <h5>{issuanceDetails?.bankName}</h5>
    },
    {
      key: "2",
      label: dateFormat()
    }
  ]

  return (
    <>
      <nav id="main-navbar" className="navbar navbar-expand-lg navbar-light">
        <div className="container-fluid">
          <div
            onClick={() => setShowMenu(true)}
            className="mobile-menu"
            style={{ display: "none" }}
          >
            <MenuOutlined />
          </div>
          <div>
            <img
              className="wdzp-logo"
              src="/images/logo_wadzpay.svg"
              alt="Wadzpay Logo"
            />
            <span className="issuance-logo">
              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              {/* {institutionDetails?.institutionFullName} */}
              <img
                src={
                  institutionDetails?.institutionLogo
                    ? institutionDetails?.institutionLogo
                    : ""
                }
                alt="Institution Logo"
                className="mbsb-logo-sm"
              />
              {/* {hasImageExtension(issuanceDetails?.bankLogo) ? (
                <img
                  src={
                    issuanceDetails?.bankLogo
                      ? issuanceDetails?.bankLogo
                      : "/images/logo_mbsb.svg"
                  }
                  alt="Institution Logo"
                  className="mbsb-logo-sm"
                />
              ) : (
                `${issuanceDetails?.bankLogo}ff`
              )} */}
            </span>
          </div>
          <ul className="navbar-nav ms-auto d-flex flex-row">
            <li
              className="nav-item dropdown top-nav-desktop"
              style={{ padding: "3px" }}
            >
              {institutionDetails && (
                // <h5 className="walletUser">{issuanceDetails?.bankName}</h5>
                <h5 className="walletUser">
                  {institutionDetails?.institutionName}
                </h5>
              )}
              <p className="walletUserType">{dateFormat()}</p>
            </li>
            <li
              className="nav-item dropdown top-nav-desktop"
              style={{ padding: "3px" }}
            >
              <a className="hidden-arrow d-flex align-items-center">
                <img
                  src="/images/user_avatar.svg"
                  className="rounded-circle user-profile-img"
                  alt="Avatar"
                  height="40"
                />
              </a>
            </li>
            <li
              className="nav-item dropdown top-nav-mobile"
              style={{ padding: "3px", display: "none" }}
            >
              <Dropdown menu={{ items }} trigger={["click"]} arrow>
                <a
                  onClick={(e) => e.preventDefault()}
                  className="hidden-arrow d-flex align-items-center"
                >
                  <img
                    src="/images/user_avatar.svg"
                    className="rounded-circle user-profile-img"
                    alt="Avatar"
                    height="40"
                  />
                </a>
              </Dropdown>
            </li>
            <li className="nav-item dropdown" style={{ padding: "3px" }}>
              <a
                className="me-lg-0 hidden-arrow"
                id="navbarDropdownMenuLink"
                role="button"
                data-mdb-toggle="dropdown"
                aria-expanded="false"
              >
                <Tooltip placement="bottomRight" title="No notifications">
                  <img
                    className="user-notification-img"
                    src="/images/bell_icon.svg"
                    alt="Bell Icon"
                  />
                </Tooltip>
                {/* <span className="badge rounded-pill badge-notification bg-danger">
                1
              </span> */}
              </a>
            </li>
          </ul>
        </div>
      </nav>
      {/* mobile nav bar */}
      <NavigationMobile
        showMenu={showMenu}
        setShowMenu={(value?: boolean) => setShowMenu(value || false)}
      />
    </>
  )
}

export default Topbar
