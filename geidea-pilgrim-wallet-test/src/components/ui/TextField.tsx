import React, { useRef, useState } from "react"
import {
  TextInput,
  StyleSheet,
  TextInputProps,
  View,
  TouchableWithoutFeedback,
  TouchableOpacity,
  Dimensions
} from "react-native"
import { FieldError, useController } from "react-hook-form"

import Icon from "./Icon"
import theme, { spacing } from "./theme"
import Typography from "./Typography"
import ErrorMessage from "./ErrorMessage"
import Container from "./Container"
import LoadingSpinner from "./LoadingSpinner"

import { IconName } from "~/icons"
import { FieldControl, FieldName } from "~/constants"
const { width  } = Dimensions.get("window")
const createStyles = (
  hasIcon: boolean,
  hasError: boolean,
  hasPassword: boolean
) =>
  StyleSheet.create({
    container: {
      flexDirection: "row",
      height: spacing(7),
      // borderBottomWidth: theme.borderWidth.xs,
      // borderBottomColor: hasError
      //   ? theme.colors.error
      //   : theme.colors.gray.medium
    },
    input: {
      flex: hasPassword ? 1 : 1,
      marginLeft: hasIcon ? spacing(0.5) : 0,
      //paddingVertical: theme.spacing.sm,
      fontSize: theme.fontSize.heading,
      fontFamily:"Rubik-Regular",
      color: theme.colors.black

    },
    icon: {
      margin: spacing(1),
    },
    iconContainer: {
      padding: theme.spacing.xs
    },
    textinput: {
      width: width - 50,
      alignItems: "flex-start",
      paddingHorizontal:theme.spacing.xs,
      marginVertical:theme.spacing.xs,
      paddingVertical:5,
      borderRadius: 5,
      borderWidth: 1,
      borderColor: "#c4c4c3",
      backgroundColor: "white"
    }
  })

export type TextFieldProps = {
  name: FieldName
  control: FieldControl
  onChange?: (value: string) => void
  label?: string
  error?: FieldError
  iconName?: IconName
  isPassword?: boolean
  isLoading?: boolean
  isLowerCase?: boolean
} & Pick<
  TextInputProps,
  | "placeholder"
  | "autoCapitalize"
  | "autoCompleteType"
  | "autoCorrect"
  | "secureTextEntry"
  | "keyboardType"
  | "returnKeyType"
  | "textContentType"
  | "autoFocus"
  | "selectTextOnFocus"
  | "clearTextOnFocus"
  | "onBlur"
  | "maxLength"

>

const TextField: React.FC<TextFieldProps> = (props: TextFieldProps) => {
  const {
    label,
    name,
    control,
    onChange,
    error,
    iconName,
    isPassword,
    isLoading,
    isLowerCase,
    ...textInputProps
  } = props
  const { field } = useController({ control, name })
  const inputRef = useRef<TextInput>(null)
  const styles = createStyles(!!iconName, !!error, isPassword)
  const [showPassword, setShowPassword] = useState(false)

  return (
    <TouchableWithoutFeedback onPress={() => inputRef?.current?.focus()}>
      <View>
        <Typography variant="label" textAlign="left" fontFamily="Rubik-Regular" color="grayDark">
          {label}
        </Typography>
        <View style={[styles.container, styles.textinput]}>
          {!isLoading && iconName && (
            <Icon name={iconName} size="md" style={styles.icon} />
          )}
          {isLoading && (
            <Container style={styles.icon}>
              <LoadingSpinner color="orange" />
            </Container>
          )}

          <TextInput
            value={field?.value}
            onChangeText={onChange ? onChange : field.onChange}
            ref={inputRef}
            style={styles.input}
            placeholderTextColor={theme.colors.gray.light}
            {...textInputProps}
            secureTextEntry={
              isPassword ? !showPassword : textInputProps.secureTextEntry
            }
            
          />
          {isPassword && (
            <TouchableOpacity
              onPress={() => setShowPassword(!showPassword)}
              style={styles.iconContainer}
            >
              <Icon name={showPassword ? "EyeClosed" : "EyeOpen"} />
            </TouchableOpacity>
          )}
        </View>
        {error && <ErrorMessage text={error?.message} />}
      </View>
    </TouchableWithoutFeedback>
  )
}

export default TextField
