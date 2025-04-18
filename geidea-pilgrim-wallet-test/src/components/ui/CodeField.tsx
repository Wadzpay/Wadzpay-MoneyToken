import React from "react"
import { TextInput, StyleSheet, View } from "react-native"
import { FieldError, useController } from "react-hook-form"

import theme, { spacing } from "./theme"
import Typography from "./Typography"
import Container from "./Container"
import ErrorMessage from "./ErrorMessage"

import { FieldControl, FieldName } from "~/constants"

const createStyles = (hasError: boolean) =>
  StyleSheet.create({
    cell: {
    //  width: theme.spacing.lg,
     // height: spacing(7),
      paddingVertical: theme.spacing.xs,
     // borderBottomWidth: theme.borderWidth.xs,
      borderColor: hasError
        ? theme.colors.error
        : theme.colors.gray.medium, 
        width: 50,
        height: 50,
        borderWidth: 0.8,
        borderRadius: 10,
        marginLeft: 10,
        textAlign:'center',
        fontSize: 18,
        fontWeight: "700",

    },
    input: {
      display: "none",
    }
  })

export type CodeFieldProps = {
  name: FieldName
  control: FieldControl
  codeLength: number
  label?: string
  error?: FieldError
}

const CodeField: React.FC<CodeFieldProps> = (props: CodeFieldProps) => {
  const { name, control, codeLength, label, error } = props
  const { field } = useController({ control, name })
  const styles = createStyles(!!error)

  const onChange = (value: string) => {
    if (value.length < codeLength) {
      field.onChange(value)
    }
  }

  return (
    <View>
      <Typography variant="label" textAlign="left" color="grayDark">
        {label}
      </Typography>
      <Container direction="row" justify="center">
        {Array.from(Array(codeLength).keys()).map((_, index) => (
          <View key={index} style={styles.cell}>
            <Typography textAlign="center" variant="button">
              {field?.value?.length >= index ? field.value[index] : ""}
            </Typography>
          </View>
        ))}
      </Container>
      <TextInput
        value={field.value}
        onChangeText={onChange}
        style={styles.input}
        textContentType="oneTimeCode"
        focusable={false}
        editable={false}
      />
      <ErrorMessage text={error?.message} textAlign="center" />
    </View>
  )
}

export default CodeField
