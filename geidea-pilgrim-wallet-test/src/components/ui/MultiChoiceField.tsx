import React from "react"
import { useTranslation } from "react-i18next"
import { TouchableOpacity, StyleSheet, View } from "react-native"
import { useController } from "react-hook-form"

import Container from "./Container"
import Typography from "./Typography"
import theme, { ChipColorVariant } from "./theme"
import { SelectOptionItem } from "./SelectField"
import Icon from "./Icon"

import { IconName } from "~/icons"
import { FieldControl, FieldName } from "~/constants"

const styles = StyleSheet.create({
  container: {
    marginBottom: theme.spacing.xs
  },
  title: {
    marginBottom: theme.spacing.xs
  },
  choicesContainer: {
    flexDirection: "row",
    flexWrap: "wrap"
  }
})

const getChoiceStyles = (
  isSelected: boolean,
  color: ChipColorVariant,
  hasIcon: boolean
) =>
  StyleSheet.create({
    choiceContainer: {
      borderRadius: theme.borderRadius.md,
      borderWidth: theme.borderWidth.sm,
      borderColor: isSelected ? theme.colors[color] : theme.colors.gray.light,
      marginRight: theme.spacing.xs,
      marginBottom: theme.spacing.xs
    },
    choice: {
      paddingVertical: theme.spacing.xs / 2,
      paddingRight: theme.spacing.xs,
      paddingLeft: hasIcon ? 0 : theme.spacing.xs
    },
    icon: {
      marginHorizontal: theme.spacing.xs / 2
    }
  })

type Props = {
  name: FieldName
  control: FieldControl
  items: SelectOptionItem[]
  label?: string
  iconName?: IconName
  color?: ChipColorVariant
  useEmptyValue?: boolean
}

const MultiChoiceField: React.FC<Props> = ({
  name,
  control,
  items,
  label,
  iconName,
  color = "orange",
  useEmptyValue
}: Props) => {
  const { t } = useTranslation()
  const { field } = useController({ name, control, shouldUnregister: false })
  const choices = useEmptyValue
    ? [{ label: t("Any"), value: "" }, ...items]
    : items

  const onChoiceToggle: (value: string) => void = (value) => {
    if (Array.isArray(field.value)) {
      if (value === "") {
        field.onChange([])
      } else if (field.value.includes(value)) {
        field.onChange([...field.value.filter((val) => val !== value)])
      } else {
        field.onChange([...field.value, value])
      }
    } else {
      if (value === field.value) {
        field.onChange("")
      } else {
        field.onChange(value)
      }
    }
  }

  return (
    <View style={styles.container}>
      {!!label && (
        <Typography variant="label" textAlign="left" style={styles.title}>
          {label}
        </Typography>
      )}
      <View style={styles.choicesContainer}>
        {choices.map(({ label, value }, index) => {
          const isSelected = Array.isArray(field.value)
            ? field.value.includes(value) ||
              (value === "" && (!field.value || field.value.length === 0))
            : field.value === value || (value === "" && !field.value)
          const choiceStyles = getChoiceStyles(isSelected, color, !!iconName)
          return (
            <TouchableOpacity
              key={index}
              onPress={() => onChoiceToggle(value)}
              style={choiceStyles.choiceContainer}
            >
              <Container
                direction="row"
                alignItems="center"
                noItemsStretch
                spacing={0.5}
              >
                {iconName && (
                  <Icon
                    name={iconName}
                    size="sm"
                    color={isSelected ? color : "regular"}
                    style={choiceStyles.icon}
                  />
                )}
                <Typography
                  color={isSelected ? color : "grayLight"}
                  style={choiceStyles.choice}
                >
                  {label}
                </Typography>
              </Container>
            </TouchableOpacity>
          )
        })}
      </View>
    </View>
  )
}

export default MultiChoiceField
