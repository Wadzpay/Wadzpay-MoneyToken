import React from "react"
import { StyleSheet } from "react-native"
import PickerSelect from "react-native-picker-select"

import Container from "./Container"
import Icon from "./Icon"
import { SelectOptionItem } from "./SelectField"
import theme, { spacing } from "./theme"
import Typography from "./Typography"

import { isIOS } from "~/utils"

const styles = StyleSheet.create({
  container: {
    margin: theme.spacing.md
  },
  row: {
    flexWrap: "wrap"
  },
  devSection: {
    marginTop: theme.spacing.lg
  },
  selectContainer: {
    height: spacing(4.5),
    borderBottomWidth: theme.borderWidth.xs,
    borderBottomColor: theme.colors.gray.medium
  },
  label: {
    marginHorizontal: spacing(0.5)
  }
})

type Props = {
  value: string
  placeHolderLabel: string
  onChange: (value: string) => void
  items: SelectOptionItem[]
  defaultValue?: string
}

const SelectFieldControlled: React.FC<Props> = ({
  value,
  onChange,
  items,
  defaultValue,
  placeHolderLabel
}: Props) => {
  // avoid null or undefined to be selected
  const _onChange = (value: string) => {
    if (value !== null && value !== undefined) onChange(value)
  }

  return (
    <PickerSelect placeholder={{
      label: placeHolderLabel,
      value: null,
      color: "#000000"
    }}
      value={value} onValueChange={_onChange} items={items}>
      <Container
        direction="row"
        justify="space-between"
        alignItems="center"
        noItemsStretch
        style={styles.selectContainer}
      >
        <Container direction="row" alignItems="center" noItemsStretch>
          <Typography style={styles.label}>
            {items.find((item) => item.value === value)?.label || defaultValue}
          </Typography>
        </Container>
        {isIOS && <Icon name="Caret" />}
      </Container>
    </PickerSelect>
  )
}

export default SelectFieldControlled
