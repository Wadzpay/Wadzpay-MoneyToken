import React from "react"
import { FieldError, useController } from "react-hook-form"
import { StyleSheet, TouchableOpacity, View } from "react-native"

import Icon from "./Icon"
import theme, { IconColorVariant } from "./theme"
import Typography from "./Typography"
import ErrorMessage from "./ErrorMessage"

import { FieldControl, FieldName } from "~/constants"
import { IconSizeVariant } from "./theme"

const styles = StyleSheet.create({
  container: {
    flexDirection: "column"
  },
  checkboxContainer: {
    flexDirection: "row"
  },
  label: {
    flex: 1,
    marginLeft: theme.spacing.sm
  }
})

type Props = {
  label: React.ReactNode
  name: FieldName
  control: FieldControl
  error?: FieldError
  size?:IconSizeVariant
}

const Checkbox: React.FC<Props> = ({ label, name, control, error, size }: Props) => {
  const { field } = useController({ control, name })

  return (
    <TouchableOpacity
      onPress={() => field.onChange(!field.value)}
      style={styles.container}
    >
      <View style={styles.checkboxContainer}>
        <Icon
          name={field.value ? "CheckboxChecked" : "CheckboxUnchecked"}
          size={size}
          color={error ? "focused" : "regular"}
        />
        <Typography textAlign="left" style={styles.label}>
          {label}
        </Typography>
      </View>
      {error && <ErrorMessage text={error?.message} />}
    </TouchableOpacity>
  )
}

export default Checkbox
