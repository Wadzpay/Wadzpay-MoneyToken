import React from "react"
import { StyleSheet, TouchableWithoutFeedback, View } from "react-native"

import Container from "./Container"
import theme, { spacing } from "./theme"

const styles = StyleSheet.create({
  container: {
    marginVertical: theme.spacing.sm
  },
  handle: {
    width: spacing(6),
    height: spacing(0.5),
    backgroundColor: theme.colors.gray.light,
    borderRadius: theme.borderRadius.sm
  }
})

type Props = {
  onPress?: () => void
}

const ModalHandle: React.FC<Props> = ({ onPress }: Props) => (
  <Container
    justify="center"
    alignItems="center"
    noItemsStretch
    style={styles.container}
  >
    <TouchableWithoutFeedback onPress={onPress}>
      <View style={styles.handle} />
    </TouchableWithoutFeedback>
  </Container>
)

export default ModalHandle
