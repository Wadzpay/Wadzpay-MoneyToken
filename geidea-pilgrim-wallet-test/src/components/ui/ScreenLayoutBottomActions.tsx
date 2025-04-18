import React from "react"
import { StyleSheet, View } from "react-native"
import { isIOS } from "~/utils"

import Screen from "./Screen"
import theme, { spacing } from "./theme"

const createStyles = (fullWidthContent: boolean) =>
  StyleSheet.create({
    contentContainer: {
      flex: 4,
    justifyContent: "center",
    marginTop: isIOS ?  spacing(2) : spacing(2),
 marginHorizontal: fullWidthContent ? 0 : spacing(3)
    },
    actionsContainer: {
      justifyContent: "center",
      marginTop: theme.spacing.xl,
      marginBottom: theme.spacing.xl,
      marginHorizontal: spacing(4)
    },
    disclaimerContainer: {
      flex: 1,
      justifyContent: "flex-end",
      marginTop: theme.spacing.xxl,
    }

  })

type Props = {
  content: React.ReactNode
  actions: React.ReactNode
  header?: React.ReactNode
  disclaimer?: React.ReactNode
  fullWidthContent?: boolean
  dismissKeyboard?: boolean
}

const ScreenLayoutBottomActions: React.FC<Props> = ({
  content,
  actions,
  header,
  disclaimer,
  fullWidthContent = false,
  dismissKeyboard = false
}: Props) => {
  const styles = createStyles(fullWidthContent)
  return (
    <Screen dismissKeyboard={dismissKeyboard}>
      {header && <View>{header}</View>}
      <View style={styles.contentContainer}>{content}</View>
      <View style={styles.actionsContainer}>{actions}</View>
     {/* {disclaimer && <View style={styles.disclaimerContainer}>{disclaimer}</View>} */}
    </Screen>
  )
}

export default ScreenLayoutBottomActions
