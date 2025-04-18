import React, { useEffect, useRef, useState } from "react"
import {
  TextInput,
  StyleSheet,
  View,
  TouchableWithoutFeedback
} from "react-native"
import { useController } from "react-hook-form"

import theme, { spacing } from "./theme"
import Typography from "./Typography"
import { TextFieldProps } from "./TextField"
import ErrorMessage from "./ErrorMessage"

const createStyles = (hasError: boolean) =>
  StyleSheet.create({
    container: {
      flexDirection: "row",
      height: spacing(4.5)
    },
    input: {
      flex: 5,
      paddingVertical: theme.spacing.xs,
      fontSize: theme.fontSize.body,
      borderBottomWidth: theme.borderWidth.xs,
      color: theme.colors.gray.dark,
      borderBottomColor: hasError
        ? theme.colors.error
        : theme.colors.gray.medium
    },
    prefixInput: {
      flex: 1,
      marginRight: theme.spacing.sm,
      color: theme.colors.gray.dark
    },
    icon: {
      margin: spacing(0.5)
    }
  })

type Props = { phoneNumberPrefix: string | undefined } & TextFieldProps

const PhoneNumberField: React.FC<Props> = (props: Props) => {
  const { phoneNumberPrefix, label, name, control, error, ...textInputProps } =
    props
  const { field } = useController({ control, name })
  const inputRef = useRef<TextInput>(null)
  const splitNumberOnPrefix =
    phoneNumberPrefix &&
    field.value &&
    (field.value as string).split(phoneNumberPrefix)
  const [phoneNumber, setPhoneNumber] = useState(
    splitNumberOnPrefix && splitNumberOnPrefix?.length >= 2
      ? splitNumberOnPrefix[1]
      : ""
  )
  const styles = createStyles(!!error)

  useEffect(() => {
    _onChange(phoneNumber)
  }, [phoneNumberPrefix])

  const _onChange = (value: string) => {
    setPhoneNumber(value)
    field.onChange(phoneNumberPrefix + value)
  }

  return (
    <TouchableWithoutFeedback onPress={() => inputRef?.current?.focus()}>
      <View>
        <Typography variant="label" textAlign="left" color="grayDark">
          {label}
        </Typography>
        <View style={styles.container}>
          <TextInput
            value={phoneNumberPrefix}
            style={[styles.input, styles.prefixInput]}
            editable={false}
          />
          <TextInput
            value={phoneNumber}
            onChangeText={(value) => _onChange(value)}
            ref={inputRef}
            style={styles.input}
            {...textInputProps}
          />
        </View>
        <ErrorMessage text={error?.message} />
      </View>
    </TouchableWithoutFeedback>
  )
}

export default PhoneNumberField
