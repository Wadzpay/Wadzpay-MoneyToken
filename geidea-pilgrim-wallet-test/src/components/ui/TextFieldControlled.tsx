import React, { useRef, useState } from "react"
import {
  TextInput,
  StyleSheet,
  TextInputProps,
  View,
  TouchableWithoutFeedback,
  TouchableOpacity
} from "react-native"
import { FieldError } from "react-hook-form"

import Icon from "./Icon"
import theme, { spacing } from "./theme"
import Typography from "./Typography"
import ErrorMessage from "./ErrorMessage"

import { IconName } from "~/icons"

const createStyles = (hasIcon: boolean, hasError: boolean) =>
  StyleSheet.create({
    container: {
      flexDirection: "row",
      height: spacing(4.5),
      borderBottomWidth: 0,
      borderBottomColor: hasError
        ? theme.colors.error
        : theme.colors.gray.medium
    },
    input: {
      flex: 1,
      marginLeft: hasIcon ? spacing(0.5) : 0,
      paddingVertical: theme.spacing.xs,
      fontSize: theme.fontSize.label
    },
    icon: {
      margin: spacing(0.5)
    },
    iconContainer: {
      padding: theme.spacing.xs
    }
  })

type Props = {
  value: string
  onChange: (value: string) => void
  label?: string
  error?: FieldError
  iconName?: IconName
  isPassword?: boolean
  isClearable?: boolean
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
>

const TextFieldControlled: React.FC<Props> = (props: Props) => {
  const {
    value,
    onChange,
    label,
    error,
    iconName,
    isPassword,
    isClearable,
    ...textInputProps
  } = props
  const inputRef = useRef<TextInput>(null)
  const styles = createStyles(!!iconName, !!error)
  const [showPassword, setShowPassword] = useState(false)

  return (
    <TouchableWithoutFeedback onPress={() => inputRef?.current?.focus()}>
      <View>
       {label && <Typography variant="label" textAlign="left" color="grayDark">
          {label} 
        </Typography> }
        <View style={styles.container}>
          {iconName && <Icon name={iconName} style={styles.icon}  color="iconRegulorColor"/>}
          <TextInput
            value={value}
            onChangeText={onChange}
            ref={inputRef}
            style={styles.input}
            maxLength={50}
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
          {isClearable && (
            <TouchableOpacity
              onPress={() => onChange("")}
              style={styles.iconContainer}
            >
              <Icon name={"Close"} size="sm" color="iconRegulorColor"/>
            </TouchableOpacity>
          )}
        </View>
       {error?.message &&  <ErrorMessage text={error?.message} />}
      </View>
    </TouchableWithoutFeedback>
  )
}

export default TextFieldControlled
