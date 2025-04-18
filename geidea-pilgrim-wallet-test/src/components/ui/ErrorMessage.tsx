import React from "react"
import { StyleSheet, TextStyle } from "react-native"

import Typography from "./Typography"
import theme from "./theme"

const styles = StyleSheet.create({
  error: {
    paddingVertical: theme.spacing.xs / 2,
    minHeight: theme.fontHeight.error + theme.spacing.xs
  }
})

type Props = {
  text?: string
  textAlign?: TextStyle["textAlign"]
}

const ErrorMessage: React.FC<Props> = ({
  text = "",
  textAlign = "left"
}: Props) => {
  return (
    <Typography
      variant="error"
      color="error"
      textAlign={textAlign}
      style={styles.error}
    >
      {text}
    </Typography>
  )
}

export default ErrorMessage
