import React from "react"
import { useTranslation } from "react-i18next"
import { StyleSheet, TouchableOpacity, ViewProps, ViewStyle } from "react-native"

import LinearGradient from 'react-native-linear-gradient';

import theme, { ButtonColorVariant, FontFamilyVariant, FontWeightVariant, IconSizeVariant, TypographyVariant } from "./theme"
import Typography from "./Typography"
import Icon from "./Icon"
import Container from "./Container"
import LoadingSpinner from "./LoadingSpinner"

import { IconName } from "~/icons"
import { isIOS } from "~/utils"

const BUTTON_TYPOGRAPHY_VARIANT = "button"
const LABEL_TYPOGRAPHY_VARIANT = "label"

const createStyles = (colorVariant: ButtonColorVariant, disabled?: boolean) => {
  const padding = theme.spacing.md
  const fontHeight = theme.fontHeight[BUTTON_TYPOGRAPHY_VARIANT]
  return StyleSheet.create({
    buttonContainer: {
      borderRadius: theme.borderRadius.lg,
      borderColor: disabled ? theme.colors.black : theme.colors.orange,
      backgroundColor: disabled? theme.colors.white : theme.colors.orange,
      ...(colorVariant === "primary"
        ? theme.shadow.button.primary_shadow
        : theme.shadow.button.secondary_shadow),
    },
    button: {
      borderRadius: theme.borderRadius.lg,
      padding,
    }
  })
}

type Props = {
  text: string
  onPress: () => void
  variant?: ButtonColorVariant
  loading?: boolean
  disabled?: boolean
  iconName?: IconName
  iconSize?: IconSizeVariant
  fontWeight?: FontWeightVariant
  fontFamily?: FontFamilyVariant
  textVariant?:TypographyVariant
  color?:any

} & ViewProps

const Button: React.FC<Props> = ({
  text,
  onPress,
  variant = "primary",
  loading = false,
  disabled = false,
  iconName,
  iconSize = "sm",
  style,
  fontWeight = "regular",
  fontFamily = "Rubik-Medium",
  textVariant=BUTTON_TYPOGRAPHY_VARIANT, 
  color= theme.colors.transparent

}: Props) => {
  const { t } = useTranslation()
  const styles = createStyles(variant , disabled)

  return (
    <TouchableOpacity
    delayPressIn={0}
      onPress={onPress}
      disabled={disabled || loading}
      style={styles.buttonContainer}
    >
      <LinearGradient
        colors={
          variant === "primary"
            ? disabled
              ? [theme.colors.disabled.disable_primary, theme.colors.disabled.disabled_secondary]
              : [theme.colors.primary, theme.colors.primary]
            :  variant === "secondary" ?  disabled
            ? [theme.colors.disabled.gray, theme.colors.disabled.gray]
            : ["#EFEFEF", "#EFEFEF"]
            : [color,color]
        }
        style={[styles.button, style]}
      >
        <Container
          direction="row"
          justify="center"
          alignItems="center"
          noItemsStretch
        >
          <Typography
            variant={textVariant}
            //{textVariant?{LABEL_TYPOGRAPHY_VARIANT}:{BUTTON_TYPOGRAPHY_VARIANT}}
            fontWeight={fontWeight}
           fontFamily={fontFamily}
            color={
              variant === "primary"
                ? "black"
                : disabled
                ? "grayMedium"
                : "grayDark"
            }
          >
            {loading ? t("Loading") : text}
          </Typography>
          {loading ? (
            <LoadingSpinner
              color={variant === "primary" ? "black" : "regular"}
            />
          ) : (
            iconName && (
              <Icon
                name={loading ? "Spinner" : iconName}
                color={variant === "primary" ? "black" : "regular"}
                size={iconSize}
              />
            )
          )}
        </Container>
      </LinearGradient>
    </TouchableOpacity>
  )
}

export default Button
