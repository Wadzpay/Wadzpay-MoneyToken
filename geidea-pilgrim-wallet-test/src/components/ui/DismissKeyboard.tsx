import React, { PropsWithChildren } from "react"
import {
  Keyboard,
  TouchableWithoutFeedback,
  View,
  StyleSheet
} from "react-native"

const styles = StyleSheet.create({
  container: {
    flex: 1
  }
})

type Props = PropsWithChildren<{}>

const DismissKeyboard: React.FC<Props> = ({ children }: Props) => (
  <TouchableWithoutFeedback
    onPress={() => Keyboard.dismiss()}
    accessible={false}
  >
    <View style={styles.container}>{children}</View>
  </TouchableWithoutFeedback>
)

export default DismissKeyboard
