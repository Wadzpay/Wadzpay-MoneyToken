import React, { PropsWithChildren } from "react"
import { StyleSheet, Text, TextProps, TextStyle } from "react-native"
import { Theme } from "react-native-elements"

import theme, {
  FontFamilyVariant,
  FontWeightVariant,
  TypographyColorVariant,
  TypographyVariant
} from "./theme"
import { th } from "date-fns/locale"

const typographyColorsMap: { [key in TypographyColorVariant]: string } = {
  white: theme.colors.white,
  black: theme.colors.black,
  orange: theme.colors.orange,
  grayLight: theme.colors.gray.light,
  grayMedium: theme.colors.gray.medium,
  grayDark: theme.colors.gray.dark,
  success: theme.colors.success,
  error: theme.colors.error,
  info: theme.colors.info,
  iconFocused: theme.colors.iconFocused,
  darkBlack: theme.colors.darkBlack,
  disclaimerColor:theme.colors.disclaimerColor,
  primary: theme.colors.primary,
  grayDarkest: theme.colors.grayDarkest,
  blackishGray: theme.colors.blackishGray,
  midDarkToneGray:theme.colors.midDarkToneGray,
  darkBlackBold : theme.colors.darkBlackBold,
  blueLinkColor : theme.colors.blueLinkColor,
  iconRegulorColor :  theme.colors.iconRegulorColor,
  dateChipColor : theme.colors.dateChipColor
}

const createStyles = (
  variant: TypographyVariant,
  textAlign: TextStyle["textAlign"],
  fontWeight: FontWeightVariant,
  color: TypographyColorVariant,
  fontFamily: FontFamilyVariant
) =>
  StyleSheet.create({
    typography: {
      textAlign,
      fontSize: theme.fontSize[variant],
      color: typographyColorsMap[color],
     // fontWeight: theme.fontWeight[fontWeight],
      fontFamily: fontFamily,
      minHeight: theme.fontHeight[variant]
    },
    uppercase: {
      textTransform: "uppercase"
    }
  })

type Props = PropsWithChildren<
  {
    variant?: TypographyVariant
    textAlign?: TextStyle["textAlign"]
    fontWeight?: FontWeightVariant
    color?: TypographyColorVariant
    uppercase?: boolean
    fontFamily?: FontFamilyVariant
  } & TextProps
>

const Typography: React.FC<Props> = ({
  variant = "label",
  textAlign = "center",
  fontWeight = "regular",
  color = "blackishGray",
  uppercase = false,
  fontFamily = "helvetica",
  style,
  children,
  ...props
}: Props) => {
  const styles = createStyles(variant, textAlign, fontWeight, color,fontFamily)
  return (
    <Text
      style={[styles.typography, uppercase && styles.uppercase, style]}
      {...props}
    >
      {children}
    </Text>
  )
}

export default Typography
