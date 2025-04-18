import React, { useState } from "react"
import { FieldError, useController } from "react-hook-form"
import DateTimePickerModal from "react-native-modal-datetime-picker"
import {
  StyleSheet,
  TextInput,
  TouchableWithoutFeedback,
  View
} from "react-native"
import { useTranslation } from "react-i18next"

import theme, { spacing } from "./theme"
import Typography from "./Typography"
import Icon from "./Icon"
import ErrorMessage from "./ErrorMessage"

import { FieldControl, FieldName } from "~/constants"
import { IconName } from "~/icons"

const createStyles = (hasIcon: boolean, hasError: boolean) =>
  StyleSheet.create({
    container: {
      flexDirection: "row",
      height: spacing(4.5),
      minWidth: 125,
      borderBottomWidth: theme.borderWidth.xs,
      borderBottomColor: hasError
        ? theme.colors.error
        : theme.colors.gray.medium
    },
    input: {
      marginLeft: hasIcon ? spacing(0.5) : 0,
      paddingVertical: theme.spacing.xs,
      fontSize: theme.fontSize.body,
      color: theme.colors.gray.dark
    },
    icon: {
      margin: spacing(0.5)
    }
  })

type Props = {
  name: FieldName
  control: FieldControl
  defaultValue?: Date
  label?: string
  error?: FieldError
  iconName?: IconName
  placeholder?: string
}

const DateField: React.FC<Props> = ({
  label,
  name,
  control,
  defaultValue = new Date(),
  error,
  iconName,
  placeholder
}: Props) => {
  const { t } = useTranslation()
  const { field } = useController({
    control,
    name,
    defaultValue
  })
  const [isVisible, setIsVisible] = useState(false)
  const styles = createStyles(!!iconName, !!error)

  const onCancel = () => {
    setIsVisible(false)
  }

  const onConfirm = (date: Date) => {
    setIsVisible(false)
    const newDate = date || defaultValue
    field.onChange(newDate)
  }

  return (
    <View>
      <TouchableWithoutFeedback onPress={() => setIsVisible(true)}>
        <View>
          <Typography variant="label" textAlign="left">
            {label}
          </Typography>
          <View style={styles.container}>
            {iconName && <Icon name={iconName} style={styles.icon} />}
            <TextInput
              onTouchStart={() => {
                setIsVisible(true)
              }}
              focusable={false}
              editable={false}
              value={
                field.value ? (field.value as Date).toLocaleDateString() : ""
              }
              placeholder={placeholder || t("Select...")}
              placeholderTextColor={theme.colors.gray.light}
              style={styles.input}
            />
          </View>
          <ErrorMessage text={error?.message} />
        </View>
      </TouchableWithoutFeedback>
      <DateTimePickerModal
        isVisible={isVisible}
        maximumDate={new Date()}
        mode="date"
        date={field.value}
        onConfirm={onConfirm}
        onCancel={onCancel}
        isDarkModeEnabled={false}
        textColor={theme.colors.black}
        style={{
          backgroundColor: theme.colors.white
        }}
        pickerContainerStyleIOS={{
          backgroundColor: theme.colors.white
        }}
      />
    </View>
  )
}

export default DateField
