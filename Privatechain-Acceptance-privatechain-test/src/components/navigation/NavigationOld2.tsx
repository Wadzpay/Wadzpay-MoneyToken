import React, { useEffect, useState, useContext } from "react"
import { t } from "i18next"
import { UserContext } from "src/context/User"
import { Link, useLocation } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import "./Navigation.scss"
import { DISPLAY_ATM_MENU } from "src/constants/Defaults"

const Navigation: React.FC = () => {
  const { user } = useContext(UserContext)
  const location = useLocation()
  const [menuSelected, setMenuSelected] = useState("dashboard")

  useEffect(() => {
    if (location) {
      if (location.pathname == "/") {
        setMenuSelected("dashboard")
      }
      if (
        location.pathname.includes("transactions") ||
        location.pathname.includes("transaction-detail")
      ) {
        setMenuSelected("transactions")
      }
      if (location.pathname.includes("invoice")) {
        setMenuSelected("invoice")
      }
      if (location.pathname.includes("settings")) {
        setMenuSelected("settings")
      }
      if (location.pathname.includes("refund")) {
        setMenuSelected("refund")
      }
 
      if (location.pathname.includes("admin/institution-management")) {
        setMenuSelected("institutionManagement")
      }
      if (location.pathname.includes("admin/role-management")) {
        setMenuSelected("roleManagement")
      }
      if (location.pathname.includes("admin/user-management")) {
        setMenuSelected("userManagement")
      }
    }
  }, [location])

  const changeMenuSelect = (name: string) => {
    setMenuSelected(name)
  }

  return (
    <div className="Navigation-Menu">
      <div className="logo-container mt-2 mb-4">
        <img
          src={"/images/black_logo.svg"}
          alt="WadzPay Logo"
          title="WadzPay Logo"
          width="112px"
        />
      </div>
      
      <div className="division mt-3 mb-2"></div>
      <div className="user-nav-links">
        <div className="user-nav-links-body">
          <Link
            to={RouteType.HOME}
            className={menuSelected === "dashboard" ? "nav-active" : ""}
            onClick={() => changeMenuSelect("dashboard")}
          >
            <img
              src={
                menuSelected === "dashboard"
                  ? "/images/navigation/dashboard_active.svg"
                  : "/images/navigation/dashboard.svg"
              }
              alt="Dashboard Logo"
              title="Dashboard Logo"
              width="30px"
              className="mr-2"
            />
            <span>{t("Dashboard")}</span>
          </Link>
          {/* <Link to={RouteType.CLOSING_SALES}>
            <img
              src={"/images/navigation/closing_sales.svg"}
              alt="closingsales Logo"
              title="Closingsales Logo"
              width="30px"
              className="mr-2"
            />
            <span>{t("Closing Sales")}</span>
          </Link> */}
          <Link
            to={RouteType.TRANSACTIONS}
            className={menuSelected === "transactions" ? "nav-active" : ""}
            onClick={() => changeMenuSelect("transactions")}
          >
            <img
              src={
                menuSelected === "transactions"
                  ? "/images/navigation/transactions_active.svg"
                  : "/images/navigation/transactions.svg"
              }
              alt="transactions Logo"
              title="Transactions Logo"
              width="30px"
              className="mr-2"
            />
            <span>{t("Sales Transactions")}</span>
          </Link>
          <Link
            to={RouteType.INVOICE}
            className={menuSelected === "invoice" ? "nav-active" : ""}
            onClick={() => changeMenuSelect("invoice")}
          >
            <img
              src={
                menuSelected === "invoice"
                  ? "/images/navigation/invoice_active.svg"
                  : "/images/navigation/invoice.svg"
              }
              alt="invoice Logo"
              title="Invoice Logo"
              width="14px"
              className="mr-2"
              style={{ marginLeft: "8px" }}
            />
            <span style={{ marginLeft: "8px" }}>{t("Invoice")}</span>
          </Link>
          {/* <Link
            to={RouteType.RequestTransaction}
            className={
              menuSelected === "request-transaction" ? "nav-active" : ""
            }
            onClick={() => changeMenuSelect("request-transaction")}
          >
            <img
              src={
                menuSelected === "request-transaction"
                  ? "/images/navigation/invoice_active.svg"
                  : "/images/navigation/invoice.svg"
              }
              alt="amber Logo"
              title="amber Logo"
              width="14px"
              className="mr-2"
              style={{ marginLeft: "8px" }}
            />
            <span style={{ marginLeft: "8px" }}>{t("Request Payment")}</span>
          </Link>*/}
          {/* <Link to={RouteType.ANALYTICS}>
            <img
              src={"/images/navigation/analytics.svg"}
              alt="analytics Logo"
              title="Analytics Logo"
              width="30px"
              className="mr-2"
            />
            <span>{t("Analytics")}</span>
          </Link> */}
          <Link
            to={RouteType.SETTINGS}
            className={menuSelected === "settings" ? "nav-active" : ""}
            onClick={() => changeMenuSelect("settings")}
          >
            <img
              src={
                menuSelected === "settings"
                  ? "/images/navigation/settings_active.svg"
                  : "/images/navigation/settings.svg"
              }
              alt="settings Logo"
              title="Settings Logo"
              width="30px"
              className="mr-2"
            />
            <span>{t("Settings")}</span>
          </Link>
          <Link
            to={RouteType.AGGREGATOR_MANAGEMENT}
            className={menuSelected === "institutionManagement" ? "nav-active" : ""}
            onClick={() => changeMenuSelect("institutionManagement")}
          >
            <img
              src={
                menuSelected === "institutionManagement"
                  ? "/images/navigation/settings_active.svg"
                  : "/images/navigation/settings.svg"
              }
              alt="settings Logo"
              title="Settings Logo"
              width="30px"
              className="mr-2"
            />
            <span>{t("Institution Management")}</span>
          </Link>
          <Link
            to={RouteType.REFUND_DISPUTE}
            className={menuSelected === "refund" ? "nav-active" : ""}
            onClick={() => changeMenuSelect("refund")}
          >
            <img
              src={
                menuSelected === "refund"
                  ? "/images/navigation/refund_dispute_active.svg"
                  : "/images/navigation/refund_dispute.svg"
              }
              alt="refund Logo"
              title="Refund Logo"
              width="30px"
              className="mr-2"
            />
            <span>{t("Refund Transactions")}</span>
          </Link>
          {DISPLAY_ATM_MENU ? (
            <Link
              to={RouteType.ATM}
              className={menuSelected === "atm" ? "nav-active" : ""}
              onClick={() => changeMenuSelect("atm")}
            >
              <img
                src={
                  menuSelected === "atm"
                    ? "/images/navigation/invoice_active.svg"
                    : "/images/navigation/invoice.svg"
                }
                alt="atm Logo"
                title="Atm Logo"
                width="30px"
                className="mr-2"
              />
              <span>{t("ATM")}</span>
            </Link>
          ) : (
            ""
          )}
        </div>
      </div>
      <div className="nav-footer">
        {/* <Link to={RouteType.HOME}>
          <img
            src={"/images/navigation/faq.svg"}
            alt="settings Logo"
            title="Settings Logo"
            width="30px"
            className="mr-2"
          />
          <span>{t("FAQ’s & Forum")}</span>
        </Link> */}
      </div>
    </div>
  )
}

export default Navigation
