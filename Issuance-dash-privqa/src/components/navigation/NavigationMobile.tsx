import React, { useState, useEffect, useContext } from "react"
import { Link, useLocation } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { Menu, Drawer, DrawerProps, Space } from "antd"
import { BankOutlined, CloseOutlined } from "@ant-design/icons"
import type { MenuProps } from "antd/es/menu"
import { signOutAsync } from "src/auth/AuthManager"
import SignedInUser from "src/auth/SignedInUser"
import { IssuanceContext } from "src/context/Merchant"

type MenuItem = Required<MenuProps>["items"][number]

function getItem(
  label: React.ReactNode,
  key?: React.Key | null,
  icon?: React.ReactNode,
  children?: MenuItem[]
): MenuItem {
  return {
    key,
    icon,
    children,
    label
  } as MenuItem
}

type Props = {
  showMenu: boolean
  setShowMenu: (value?: boolean) => void
}

const App: React.FC<Props> = ({ showMenu, setShowMenu }: Props) => {
  const { issuanceDetails } = useContext(IssuanceContext)
  const [placement] = useState<DrawerProps["placement"]>("bottom")
  const location = useLocation()
  const [menuSelected, setMenuSelected] = useState("dashboard")

  useEffect(() => {
    if (location) {
      if (location.pathname == "/") {
        setMenuSelected("dashboard")
      }
      if (location.pathname.includes("wallets")) {
        setMenuSelected("walletslist")
      }
      if (location.pathname.includes("configurations/wallet-parameter")) {
        setMenuSelected("walletParameter")
      }
      if (location.pathname.includes("configurations/transaction-limits")) {
        setMenuSelected("transactionLimits")
      }
      if (location.pathname.includes("configurations/conversion-rates")) {
        setMenuSelected("conversionRates")
      }
      if (
        location.pathname.includes("configurations/conversion-rates-adjustment")
      ) {
        setMenuSelected("conversionRatesAdjustment")
      }
      if (
        location.pathname.includes("settings") ||
        location.pathname.includes("changepassword")
      ) {
        setMenuSelected("settings")
      }
    }
  }, [location])

  const changeMenuSelect = (name: string) => {
    setMenuSelected(name)
    setShowMenu(false)
  }

  const items: MenuItem[] = [
    getItem(
      "Dashboard",
      "1",
      <Link to={RouteType.HOME} onClick={() => changeMenuSelect("dashboard")}>
        <img
          className={
            menuSelected === "dashboard" ? "blue-nav-icon" : "grey-nav-icon"
          }
          src={"/images/navigation/grey_home_icon.svg"}
          alt="Dashboard Logo"
          title="Dashboard"
          width={24}
          style={{ marginLeft: "0px" }}
        />
      </Link>
    ),
    getItem(
      "Wallets",
      "2",
      <Link
        to={RouteType.WALLETSLIST}
        onClick={() => changeMenuSelect("walletslist")}
      >
        <img
          // className={menuSelected === "walletslist" ? "blue-nav-icon" : ""}
          src={
            menuSelected === "walletslist"
              ? "/images/navigation/blue_wallets_icon.svg"
              : "/images/navigation/grey_wallets_icon.svg"
          }
          alt="Wallets Icon"
          title="Wallets"
          width={22}
          style={{ marginLeft: "0px" }}
        />
      </Link>
    ),
    // getItem(
    //   "Admin",
    //   "sub1",
    //   <img
    //     className={
    //       menuSelected === "admin" ||
    //       menuSelected === "institutionManagement" ||
    //       menuSelected === "roleManagement" ||
    //       menuSelected === "userManagement"
    //         ? "blue-nav-icon"
    //         : "grey-nav-icon"
    //     }
    //     src={"/images/navigation/admin_icon.svg"}
    //     alt="Admin Logo"
    //     title="Admin"
    //     width={20}
    //     style={{ marginLeft: "5px" }}
    //   />,
    //   [
    //     getItem(
    //       "Institution Management",
    //       "3",
    //       <Link
    //         to={RouteType.INSTITUTION_MANAGEMENT}
    //         onClick={() => changeMenuSelect("institutionManagement")}
    //         title="Institution Management"
    //         style={{
    //           color:
    //             menuSelected === "institutionManagement" ? "#26a6e0" : "#7f7886"
    //         }}
    //       ></Link>
    //     ),
    //     getItem(
    //       "Role Management",
    //       "4",
    //       <Link
    //         to={RouteType.ROLE_MANAGEMENT}
    //         onClick={() => changeMenuSelect("roleManagement")}
    //         title="Role Management"
    //         style={{
    //           color: menuSelected === "roleManagement" ? "#26a6e0" : "#7f7886"
    //         }}
    //       ></Link>
    //     ),
    //     getItem(
    //       "User Management",
    //       "5",
    //       <Link
    //         to={RouteType.USER_MANAGEMENT}
    //         onClick={() => changeMenuSelect("userManagement")}
    //         title="User Management"
    //         style={{
    //           color: menuSelected === "userManagement" ? "#26a6e0" : "#7f7886"
    //         }}
    //       ></Link>
    //     )
    //   ]
    // ),
    getItem(
      "Configurations",
      "sub2",
      <a>
        <img
          className={
            menuSelected === "configurations" ||
            menuSelected === "walletParameter" ||
            menuSelected === "transactionLimits" ||
            menuSelected === "conversionRates" ||
            menuSelected === "conversionRatesAdjustment"
              ? "blue-nav-icon"
              : "grey-nav-icon"
          }
          src={"/images/navigation/configuration_icon.svg"}
          alt="configurations Logo"
          title="Configurations"
          width={24}
          style={{ marginLeft: "5px" }}
        />
      </a>,
      [
        getItem(
          "Wallet Parameter",
          "6",
          <Link
            to={RouteType.WALLET_PARAMETER}
            onClick={() => changeMenuSelect("walletParameter")}
            title="Wallet Parameter"
            style={{
              color: menuSelected === "walletParameter" ? "#26a6e0" : "#7f7886"
            }}
          ></Link>
        ),
        getItem(
          "Transaction Limits",
          "7",
          <Link
            to={RouteType.TRANSACTION_LIMITS}
            onClick={() => changeMenuSelect("transactionLimits")}
            title="Transaction Limit"
            style={{
              color:
                menuSelected === "transactionLimits" ? "#26a6e0" : "#7f7886"
            }}
          ></Link>
        ),
        getItem(
          "Conversion Rates",
          "8",
          <Link
            to={RouteType.CONVERSION_RATES}
            onClick={() => changeMenuSelect("conversionRates")}
            title="Conversion Rates"
            style={{
              color: menuSelected === "conversionRates" ? "#26a6e0" : "#7f7886"
            }}
          ></Link>
        ),
        getItem(
          "Conversion Rates Adjustment",
          "9",
          <Link
            to={RouteType.CONVERSION_RATES_ADJUSTMENT}
            className="nav-link py-3 px-2"
            onClick={() => changeMenuSelect("conversionRatesAdjustment")}
            title="Conversion Rates Adjustment"
            style={{
              color:
                menuSelected === "conversionRatesAdjustment"
                  ? "#26a6e0"
                  : "#7f7886"
            }}
          ></Link>
        )
      ]
    ),
    getItem("", "10")
  ]

  return (
    <>
      <Drawer
        title={
          <>
            <img
              className="wdzp-logo"
              src={
                issuanceDetails?.bankLogo
                  ? issuanceDetails?.bankLogo
                  : "/images/logo_mbsb.svg"
              }
              alt="Institution Logo"
              width={100}
              style={{ left: "15px" }}
            />
          </>
        }
        placement={placement}
        closable={false}
        onClose={() => setShowMenu(false)}
        open={showMenu}
        key={placement}
        extra={
          <Space>
            <CloseOutlined onClick={() => setShowMenu(false)} />
          </Space>
        }
        width={100}
      >
        <Menu mode={"inline"} items={items} />

        <SignedInUser>
          <span onClick={() => signOutAsync()}>
            <span
              style={{
                cursor: "pointer",
                display: "flex",
                justifyContent: "center",
                marginTop: "20px",
                color: "red"
              }}
            >
              Sign Out
            </span>
          </span>
        </SignedInUser>
      </Drawer>
    </>
  )
}

export default App
