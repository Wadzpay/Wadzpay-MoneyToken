import React, { useEffect, useState, useContext } from "react";
import { t } from "i18next";
import { UserContext } from "src/context/User";
import { LevelContext } from "src/context/Level";

import { Link, useLocation } from "react-router-dom";
import { RouteType } from "src/constants/routeTypes";
import { DISPLAY_ATM_MENU } from "src/constants/Defaults";
import { Menu, Tooltip, Drawer, DrawerProps, Space } from "antd";
import type { MenuProps } from "antd/es/menu";
import "./Navigation.scss";
type MenuItem = Required<MenuProps>["items"][number];

function getItem(
  label: React.ReactNode,
  key?: React.Key | null,
  icon?: React.ReactNode,
  // expandIcon?: React.ReactNode,
  children?: MenuItem[]
): MenuItem {
  return {
    key,
    icon,
    // expandIcon,
    children,
    label,
  } as MenuItem;
}

const Navigation: React.FC = () => {
  const { user } = useContext(UserContext);
  const location = useLocation();
  const [menuSelected, setMenuSelected] = useState("dashboard");
  const { levelNumber, setLevelNumber } = useContext(LevelContext);
  const items: MenuItem[] = [
    getItem(
      "Dashboard",
      "1",
      <Link to={RouteType.HOME} onClick={() => changeMenuSelect("dashboard")}>
        <img
          className={menuSelected === "dashboard" ? "nav-active" : ""}
          src={
            menuSelected === "dashboard"
              ? "/images/navigation/dashboard_active.svg"
              : "/images/navigation/dashboard.svg"
          }
          alt="Dashboard Logo"
          title="Dashboard Logo"
          width={24}
          style={{ marginLeft: "4px" }}
        />
      </Link>
    ),
    getItem(
      "Transactions ",
      "2",
      <Link
        to={RouteType.TRANSACTIONS}
        className={
          menuSelected === "transactions" ? "nav-active transactions" : ""
        }
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
          style={{ marginLeft: "1px" }}
        />
      </Link>
    ),
    // getItem(
    //   "Invoice",
    //   "3",
    //   <Link
    //     to={RouteType.INVOICE}
    //     className={menuSelected === "invoice" ? "nav-active" : ""}
    //     onClick={() => changeMenuSelect("invoice")}
    //   >
    //     <img
    //       src={
    //         menuSelected === "invoice"
    //           ? "/images/navigation/invoice_active.svg"
    //           : "/images/navigation/invoice.svg"
    //       }
    //       alt="invoice Logo"
    //       title="Invoice Logo"
    //       width="14px"
    //       className="mr-2"
    //       style={{ marginLeft: "9px" }}
    //     />
    //   </Link>
    // ),
    // getItem(
    //   "Admin",
    //   "sub1",
    //   <img
    //     className={
    //       menuSelected === "admin" ||
    //       menuSelected === "aggregatorManagement" ||
    //       menuSelected === "institutionManagement" ||
    //       menuSelected === "roleManagement" ||
    //       menuSelected === "userManagement"
    //         ? "nav-active"
    //         : "nav-active"
    //     }
    //     src={"/images/navigation/admin_icon.svg"}
    //     alt="Admin Logo"
    //     title="Admin"
    //     width={20}
    //     style={{ marginLeft: "6px" }}
    //   />,
    //   [
    //     getItem(
    //       "Aggregator Management",
    //       "4",
    //       <Link
    //         to={RouteType.AGGREGATOR_MANAGEMENT}
    //         onClick={() => changeMenuSelect("aggregatorManagement")}
    //         title="Aggregator Management"
    //         style={{
    //           color:
    //             menuSelected === "aggregatorManagement" ? "#26a6e0" : "#7f7886",
    //         }}
    //       ></Link>
    //     ),
    //     getItem(
    //       "Role Management",
    //       "5",
    //       <Link
    //         to={RouteType.ROLE_MANAGEMENT + `/${levelNumber}`}
    //         onClick={() => changeMenuSelect("roleManagement")}
    //         title="Role Management"
    //         style={{
    //           color: menuSelected === "roleManagement" ? "#26a6e0" : "#7f7886",
    //         }}
    //       ></Link>
    //     ),
    //     getItem(
    //       "User Management",
    //       "6",
    //       <Link
    //         to={RouteType.USER_MANAGEMENT + `/${levelNumber}`}
    //         onClick={() => changeMenuSelect("userManagement")}
    //         title="User Management"
    //         style={{
    //           color: menuSelected === "userManagement" ? "#26a6e0" : "#7f7886",
    //         }}
    //       ></Link>
    //     ),
    //     getItem(
    //       "Level Management",
    //       "7",
    //       <Link
    //         to={RouteType.LEVEL_MANAGEMENT}
    //         onClick={() => changeMenuSelect("levelManagement")}
    //         title="Level Management"
    //         style={{
    //           color: menuSelected === "levelManagement" ? "#26a6e0" : "#7f7886",
    //         }}
    //       ></Link>
    //     ),
    //     getItem(
    //       "Module Management",
    //       "8",
    //       <Link
    //         to={RouteType.MODULE_MANAGEMENT}
    //         onClick={() => changeMenuSelect("moduleManagement")}
    //         title="Module Management"
    //         style={{
    //           color:
    //             menuSelected === "moduleManagement" ? "#26a6e0" : "#7f7886",
    //         }}
    //       ></Link>
    //     ),
    //   ]
    // ),
    getItem(
      "Refund",
      "9",
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
          style={{ marginLeft: "1px" }}
        />
      </Link>
    ),
    getItem(
      "Settings",
      "10",
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
          style={{ marginLeft: "1px" }}
        />
      </Link>
    ),
  ];

  useEffect(() => {
    if (location) {
      if (location.pathname == "/") {
        setMenuSelected("dashboard");
      }
      if (
        location.pathname.includes("transactions") ||
        location.pathname.includes("transaction-detail")
      ) {
        setMenuSelected("transactions");
      }
      if (location.pathname.includes("invoice")) {
        setMenuSelected("invoice");
      }
      if (location.pathname.includes("refund")) {
        setMenuSelected("refund");
      }
      if (location.pathname.includes("admin/aggregator-management")) {
        setMenuSelected("aggregatorManagement");
      }
      if (location.pathname.includes("admin/institution-management")) {
        setMenuSelected("institutionManagement");
      }
      if (location.pathname.includes("admin/role-management")) {
        setMenuSelected("roleManagement");
      }
      if (location.pathname.includes("admin/user-management")) {
        setMenuSelected("userManagement");
      }
      if (location.pathname.includes("settings")) {
        setMenuSelected("settings");
      }
    }
  }, [location, levelNumber]);

  const changeMenuSelect = (name: string) => {
    setMenuSelected(name);
  };

  return (
    <div className="custom-navigation d-flex flex-sm-column flex-row flex-nowrap bg-light align-items-center sticky-top bg-white">
      <Menu mode={"inline"} items={items} />
      <div
        className="dropdown py-sm-4 mt-sm-auto ms-auto ms-sm-0 flex-shrink-1 more-screen-size-600"
        style={{ position: "fixed", bottom: "0", left: "19px" }}
      ></div>
    </div>
  );
};

export default Navigation;
