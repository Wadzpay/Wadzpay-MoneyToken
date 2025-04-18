import { TextStyle } from "react-native"

import { isIOS } from "~/utils"

const SPACING_UNIT = 8

export const spacing = (multiplier: number): number =>
  Math.floor(multiplier * SPACING_UNIT)

export type ButtonColorVariant = "primary" | "secondary" | "other"
export type TypographyVariant =
  | "title"
  | "subtitle"
  | "body"
  | "button"
  | "label"
  | "error"
  | "chip"
  | "tabbarlabel"
  | "heading"
export type FontWeightVariant = "regular" | "bold"
export type FontFamilyVariant = 
| "Arial" 
| "helvetica" 
| "Montserrat-Light" 
| "helvetica" 
| "Montserrat-Regular" 
| "Montserrat-LightItalic" 
| "Helvetica-Bold" 
| "HelveticaNeue-Light"
| "HelveticaNeue-Medium"
| "Rubik-Light"
| "Rubik-Medium"
| "Rubik-Regular"
export type TypographyColorVariant =
  | "white"
  | "black"
  | "orange"
  | "grayLight"
  | "grayMedium"
  | "grayDark"
  | "success"
  | "error"
  | "info"
  | "BlackLight"
  | "mediumDark"
  | "green"
  | "success"
  | "iconFocused"
  | "darkBlack"
  | "disclaimerColor"
  | "primary"
  | "grayDarkest"
  |  "blackishGray"
  | "midDarkToneGray"
  | "darkBlackBold"
  | "blueLinkColor"
  | "iconRegulorColor"
  | "dateChipColor"
export type ChipColorVariant = "black" | "orange" | "success" | "error" | "info"
export type IconColorVariant =
  | "white"
  | "black"
  | "orange"
  | "regular"
  | "success"
  | "error"
  | "info"
  | "grayMedium"
  | "focused"
  | "disclaimerColor"
  | "darkGray"
  | "iconRegulorColor"
  
export type IconSizeVariant = "xxs" | "xs" | "sm" | "md" | "lg" | "xl"

const theme = {
  spacing: {
    xs: spacing(1),
    sm: spacing(1.5),
    md: spacing(2),
    lg: spacing(3),
    xl: spacing(4),
    xxl: spacing(5)
  },
  colors: {
    blueLinkColor:"#1687F0",
    iconRegulorColor:"#494949",
    midDarkToneGray:"#636363",
    blackishGray: "#494949",
    darkBlackBold: "#1E1E1E",
    transparent: "transparent",
    white: "#FFFFFF",
    black: "#000000",
    primary:"#FFC235",
    secondary: "#FFA600",
    iconFocused : "#FFA600",
    yellow: "#F8A83B",
    orange: "#FFA600",
    mediumDark: "#7A7A7A",
    grayDarkest: "#7C7A7A",
    dateChipColor: "#3D3D3D",
    gray: {
      dark: "#2E3033",
      medium: "#979899",
      light: "#BFC3C9"
    },
    darkBlack: "#363636",
    darkGray: "#515151",
    disclaimerColor:"#F8A83B",
    success: "#14AA37",
    error: "#F53F32",
    info: "#FFA600",
    disabled: {
      disable_primary:"#FFA600",
      disabled_secondary:"#FFA600",
      yellow: "#F8A83B",
      orange: "#FFA600",
    },
    crypto: {
      WTK: {
        primary: "orange",
        secondary: "#FFCB23"
      },
      ETH: {
        primary: "#92A9FF",
        secondary: "#627EEA"
      },
      SAR: {
        primary: "#FFC428",
        secondary: "#FFA63C"
      },
      BTC: {
        primary: "#51504E",
        secondary: "#000000"
      },
      USDT: {
        primary: "#50AF95",
        secondary: "#387967"
      },
      GENERAL: {
        primary: "#F8F8F8",
        secondary: "#F8F8F8"
      },
      FIAT: {
        primary: "#1B5992",
        secondary: "#121E30"
      },
      blackLight: "#1D2D3A",
      green: "#27AE60"
    }
  },
  borderRadius: {
    xxs: 2,
    xs: 4,
    sm: 6,
    md: 8,
    lg: 12,
    xl: 20,
    xxl: 32
  },
  borderWidth: {
    none: 0,
    xs: 1,
    sm: 2,
    md: 3
  },
  iconSize: {
    xxs: 12,
    xs: 16,
    sm: 20,
    md: 24,
    lg: 32,
    xl: 48
  } as { [key in IconSizeVariant]: number },
  fontFamily: "Rubik",
  fontFamilyhelvetica : "helvetica",
  fontSize: {
    title: 30,
    headerTitle:20,
    subtitle: 24,
    body: 18,
    heading:16,
    button: 18,
    label: 14,
    error: 14,
    chip: 12,
    tabbarlabel:10
  } as { [key in TypographyVariant]: number },
  fontHeight: {
    title: 36.5,
    subtitle: 29,
    body: 20,
    button: isIOS ? 22 : 16,
    label: 17.5,
    error: 18.5
  } as { [key in TypographyVariant]: number },
  fontWeight: {
    regular: "400",
    bold: "600"
  } as { [key in FontWeightVariant]: TextStyle["fontWeight"] },
  shadow: {
    button: {
      primary_shadow: {
        shadowColor: "#FFA600",
        shadowOffset: {
          width: 0,
          height: 5
        },
        shadowOpacity: 0.34,
        shadowRadius: 6.27,

        elevation: 10
      },
      secondary_shadow: {
        shadowColor: "#DADADA",
        shadowOffset: {
          width: 0,
          height: 2
        },
        shadowOpacity: 0.25,
        shadowRadius: 3.84,

        elevation: 5
      }
    },
    card: {
      shadowColor: "#000",
      shadowOffset: {
        width: 0,
        height: 3
      },
      shadowOpacity: 0.29,
      shadowRadius: 4.65,

      elevation: 7
    },
    tabBar: {
      shadowColor: "#000",
      shadowOffset: {
        width: 0,
        height: 7
      },
      shadowOpacity: 0.41,
      shadowRadius: 9.11,

      elevation: 14
    }
  },
  headerHeight: 65, //TODO:- Nandani
  modalFullScreenHeight: "85%",
  modalMidScreenHeight: "40%",
  appTabBar: {
    height: isIOS ? 80 : 70
  }
}

export default theme
