import React from "react"
import { Image, StyleSheet } from "react-native"

import { Container, LoadingSpinner, Screen } from "~/components/ui"

const styles = StyleSheet.create({
  container: { flex: 1 }
})

const Loading: React.FC = () => {
  return (
    <Screen>
      <Container
        justify="center"
        alignItems="center"
        noItemsStretch
        spacing={4}
        style={styles.container}
      >
        <Image source={require("~images/splash_image_logo.png")} />
        <LoadingSpinner size="xl" color="orange" />
      </Container>
    </Screen>
  )
}

export default Loading
