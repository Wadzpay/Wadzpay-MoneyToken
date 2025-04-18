import React, { PropsWithChildren } from "react"
import { useTranslation } from "react-i18next"
import RNModal, { Direction } from "react-native-modal"
import {
  StyleProp,
  StyleSheet,
  View,
  ViewStyle,
  TouchableOpacity
} from "react-native"

import theme from "./theme"
import Icon from "./Icon"
import ModalHandle from "./ModalHandle"
import Container from "./Container"
import Typography from "./Typography"

type ModalVariant = "bottom" | "center"

const createStyles = (variant: ModalVariant , modalBgColor: string) =>
  StyleSheet.create({
    modal:
      variant === "center"
        ? {}
        : {
            margin: 0,
            justifyContent: "flex-end"
          },
    contentContainer: {
      backgroundColor: modalBgColor,
      ...(variant === "center"
        ? { borderRadius: theme.borderRadius.xl }
        : {
            borderTopLeftRadius: theme.borderRadius.xl,
            borderTopRightRadius: theme.borderRadius.xl
          })
    },
    header: {
      width: "100%",
      padding: theme.spacing.xs,
      paddingLeft: theme.spacing.lg
    },
    cancel: {
      padding: theme.spacing.md
    },
    content: {
      ...(variant === "center"
        ? {
            padding: theme.spacing.xl,
            paddingTop: 0
          }
        : {})
    }
  })

type Props = PropsWithChildren<{
  isVisible: boolean
  onDismiss: () => void
  variant: ModalVariant
  title?: string
  modalBgColor?: string
  dismissButtonVariant?: "handle" | "button" | "cancel" | "none"
  swipeDirection?: Direction | Direction[]
  modalStyle?: StyleProp<ViewStyle>
  contentStyle?: StyleProp<ViewStyle>
}>

const Modal: React.FC<Props> = ({
  isVisible,
  onDismiss,
  variant,
  title,
  dismissButtonVariant = "none",
  swipeDirection,
  modalStyle,
  contentStyle,
  children,
  modalBgColor = theme.colors.white
}: Props) => {
  const { t } = useTranslation()
  const styles = createStyles(variant, modalBgColor)
  return (
    <RNModal
      onBackdropPress={onDismiss}
      isVisible={isVisible}
      swipeDirection={undefined}
      onSwipeComplete={onDismiss}
      style={[styles.modal, modalStyle]}
      onRequestClose={onDismiss}
    >
      <View style={styles.contentContainer}>
        {dismissButtonVariant === "handle" && (
          <ModalHandle onPress={onDismiss} />
        )}
        <Container
          direction="row"
          justify={title ? "space-between" : "flex-end"}
          alignItems="center"
          noItemsStretch
          style={styles.header}
        >
          {title && <Typography variant="subtitle">{title}</Typography>}
          {dismissButtonVariant === "button" && (
            <Icon name="Close" onPress={onDismiss} />
          )}
          {dismissButtonVariant === "cancel" && (
            <TouchableOpacity onPress={onDismiss} style={styles.cancel}>
              <Typography variant="button" color="black">
                {t("Cancel")}
              </Typography>
            </TouchableOpacity>
          )}
        </Container>
        <View style={[styles.content, contentStyle]}>{children}</View>
      </View>
    </RNModal>
  )
}

export default Modal
