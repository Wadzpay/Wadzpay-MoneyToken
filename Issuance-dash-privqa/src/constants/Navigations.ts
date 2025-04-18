import { RouteType } from "./routeTypes"
import isProduction from "./../utils/environmentUtils"
const productionMode = isProduction()

export const NavigationMenu = [
  {
    label: "Dashboard",
    icon: "dashboard.svg",
    route: RouteType.HOME,
    subMenu: []
  },
  {
    label: "Wallets",
    icon: "wallets_icon.svg",
    route: RouteType.WALLETSLIST,
    subMenu: []
  },
  {
    label: !productionMode ? "Admin" : undefined,
    key: ["sub1"],
    icon: "admin_icon.svg",
    route: "/admin",
    subMenu: [
      {
        label: "Institution Management",
        route: RouteType.INSTITUTION_MANAGEMENT
      },
      {
        label: "Role Management",
        route: RouteType.ROLE_MANAGEMENT
      },
      {
        label: "User Management",
        route: RouteType.USER_MANAGEMENT
      }
    ]
  },
  {
    label: "Configurations",
    key: ["sub2"],
    icon: "configuration_icon.svg",
    route: "/configurations",
    subMenu: [
      {
        label: "Wallet Parameter",
        route: RouteType.WALLET_PARAMETER
      },
      {
        label: "Transaction Limits",
        route: RouteType.TRANSACTION_LIMITS
      },
      {
        label: "Conversion Rates",
        route: RouteType.CONVERSION_RATES
      },
      {
        label: "Conversion Rates Adjustment",
        route: RouteType.CONVERSION_RATES_ADJUSTMENT
      }
      // {
      //   label: !productionMode ? "Languages" : undefined,
      //   route: RouteType.LANGUAGES
      // },
      // {
      //   label: !productionMode ? "Institution Languages" : undefined,
      //   route: RouteType.INSTITUTION_LANGUAGES,
      //   type: "institution"
      // },
      // {
      //   label: !productionMode ? "Logo" : undefined,
      //   route: RouteType.LOGO
      // }
    ]
  },
  {
    label: "Settings",
    icon: "settings_icon.svg",
    route: RouteType.SETTINGS
  }
]
