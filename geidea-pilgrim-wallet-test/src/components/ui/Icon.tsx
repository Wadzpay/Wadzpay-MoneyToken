import React from "react"
import { SvgProps } from "react-native-svg"
import { TouchableOpacity, StyleSheet } from "react-native"

import theme, { IconColorVariant, IconSizeVariant } from "./theme"

import { IconName, iconsComponentMap } from "~/icons"

const MINIMAL_TOUCH_AREA = 32

const iconColorsMap: { [key in IconColorVariant]: string } = {
  white: theme.colors.white,
  black: theme.colors.black,
  orange: theme.colors.orange,
  regular: theme.colors.gray.light,
  success: theme.colors.success,
  error: theme.colors.error,
  info: theme.colors.info,
  focused: theme.colors.iconFocused,
  iconRegulorColor: "#494949"
}

const getStyles = (size: IconSizeVariant, color: IconColorVariant) =>
  StyleSheet.create({
    icon: {
      padding: MINIMAL_TOUCH_AREA - theme.iconSize[size]
    },
    active: {
      backgroundColor: `${iconColorsMap[color]}25`,
      borderRadius: theme.borderRadius[size]
    }
  })

type Props = {
  name: IconName | any
  color?: IconColorVariant | any
  size?: IconSizeVariant
  onPress?: () => void
  isActive?: boolean
} & Omit<SvgProps, "color">

const Icon: React.FC<Props> = ({
  name,
  color = "regular",
  size = "md",
  onPress,
  isActive,
  ...props
}: Props) => {
  const Component = iconsComponentMap[name]
  const styles = getStyles(size, color)

  const renderComponent = (
    <Component
      {...props}
      width={theme.iconSize[size]}
      height={theme.iconSize[size]}
      color={isActive ? iconColorsMap.focused : iconColorsMap[color]}
    />
  )

  return onPress ? (
    <TouchableOpacity
      onPress={onPress}
      style={[styles.icon, isActive && styles.active]}
    >
      {renderComponent}
    </TouchableOpacity>
  ) : (
    renderComponent
  )
}

export default Icon
