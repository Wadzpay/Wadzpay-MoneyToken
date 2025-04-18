import React, { PropsWithChildren } from "react"
import { SafeAreaView, StyleSheet, StatusBar } from "react-native"

import DismissKeyboard from "./DismissKeyboard"
import theme from "./theme"

import { isIOS } from "~/utils"

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.white,
    //paddingTop: !isIOS ? StatusBar.currentHeight : 0
  }
})
 
type Props = PropsWithChildren<{
  dismissKeyboard?: boolean
}>

const Screen: React.FC<Props> = ({
  dismissKeyboard = false,
  children
}: Props) => {
  return (
    <SafeAreaView style={styles.container}>
      {dismissKeyboard ? (
        <DismissKeyboard>{children}</DismissKeyboard>
      ) : (
        children
      )}
    </SafeAreaView>
  )
}

export default Screen
