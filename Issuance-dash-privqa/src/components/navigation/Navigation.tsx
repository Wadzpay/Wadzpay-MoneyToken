import React, { useState, useEffect } from "react"
import { Menu } from "antd"
import { Link, useLocation } from "react-router-dom"
import type { MenuProps } from "antd/es/menu"
import { useSelector } from "react-redux"
import "./Navigation.scss"
import { ROOTSTATE } from "src/utils/modules"

import { NavigationMenu } from "./../../constants/Navigations"
import SignOut from "../../components/ui/SignOut"

type MenuItem = Required<MenuProps>["items"][number]

// submenu keys of first level
const rootSubmenuKeys = NavigationMenu.map(
  (menuItem, index) => `sub${index + 1}`
)

const Navigation: React.FC = () => {
  const location = useLocation()
  const currentLocation = localStorage.getItem("location")

  // Selector
  const activeNavigation = useSelector(
    (store: ROOTSTATE) =>
      location.pathname || currentLocation || store.appConfig.activeNavigation
  )
  const issuanceType = useSelector(
    (store: ROOTSTATE) => store.appConfig.issuanceType
  )
  const buttonColor = useSelector(
    (store: ROOTSTATE) => store.appConfig.buttonColor
  )

  const [openKeys, setOpenKeys] = useState<string[]>([])

  // current Location
  const [menuSelected, setMenuSelected] = useState<string>(
    currentLocation || "/"
  )

  const onOpenChange: MenuProps["onOpenChange"] = (keys) => {
    const latestOpenKey = keys.find((key) => openKeys.indexOf(key) === -1)
    setOpenKeys(latestOpenKey ? [latestOpenKey] : [])
  }

  const changeMenuSelect = (location: string) => {
    setMenuSelected(location)
    localStorage.setItem("location", location)
  }

  const items: MenuItem[] | any = NavigationMenu.filter(
    (menuItem) => menuItem.label !== "Admin" // Hides the Admin menu
  ).map((menuItem, index) =>
    menuItem.subMenu && menuItem.subMenu.length > 0 && menuItem.label
      ? {
          label: menuItem.label,
          key: `sub${index + 1}`,
          className: menuSelected.includes(menuItem.route)
            ? `activeMainMenu`
            : "",
          icon: (
            <img
              src={`/images/navigation/${menuItem.icon}`}
              alt={`${menuItem.label} Icon`}
              className={
                menuSelected.includes(menuItem.route)
                  ? `${issuanceType}-activeTab`
                  : ""
              }
            />
          ),
          children: menuItem.subMenu.map((subMenuItem, subIndex) => {
            return (
              subMenuItem.label && {
                key: `${index + 1}-${subIndex + 1}`,
                label: (
                  <Link
                    to={subMenuItem.route}
                    onClick={() => changeMenuSelect(subMenuItem.route)}
                    title={subMenuItem.label}
                  >
                    {subMenuItem.label}
                  </Link>
                ),
                className:
                  menuSelected === subMenuItem.route
                    ? `${issuanceType}-activeTab`
                    : ""
              }
            )
          })
        }
      : menuItem.label && {
          label: menuItem.label,
          key: `${index + 1}`,
          icon: (
            <Link
              to={menuItem.route}
              onClick={() => changeMenuSelect(menuItem.route)}
              title={menuItem.label}
            >
              <img
                src={`/images/navigation/${menuItem.icon}`}
                alt={`${menuItem.label} Icon`}
              />
            </Link>
          ),
          className:
            menuSelected === menuItem.route ? `${issuanceType}-activeTab` : ""
        }
  )

  // update selected menu item
  useEffect(() => {
    // Menu Select Function
    changeMenuSelect(activeNavigation)
  }, [activeNavigation])

  return (
    <>
      <div className="custom-navigation d-flex flex-sm-column flex-row flex-nowrap bg-light align-items-center sticky-top bg-white">
        <span className="less-screen-size-768 less-screen-size-600">
          <img
            src="/images/logo_mbsb.svg"
            alt="MBSB Logo"
            className="mbsb-logo-sm"
          />
        </span>
        <Menu
          mode="inline"
          items={items}
          openKeys={openKeys}
          onOpenChange={onOpenChange}
        />
        <div
          className="dropdown py-sm-4 mt-sm-auto ms-auto ms-sm-0 flex-shrink-1"
          style={{ position: "fixed", bottom: "0", left: "17px" }}
        >
          <SignOut />
        </div>
      </div>
    </>
  )
}

export default Navigation
