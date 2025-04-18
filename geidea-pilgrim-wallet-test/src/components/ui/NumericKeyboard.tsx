import React from "react"
import { StyleSheet, Dimensions, TouchableOpacity } from "react-native"

import Container from "./Container"
import Icon from "./Icon"
import theme, { spacing } from "./theme"
import Typography from "./Typography"

const { width } = Dimensions.get("window")

export enum KEY {
  ONE = "1",
  TWO = "2",
  THREE = "3",
  FOUR = "4",
  FIVE = "5",
  SIX = "6",
  SEVEN = "7",
  EIGHT = "8",
  NINE = "9",
  DECIMAL_POINT = ".",
  ZERO = "0",
  BACKSPACE = "BACKSPACE"
}

const keys = [
  [KEY.ONE, KEY.TWO, KEY.THREE],
  [KEY.FOUR, KEY.FIVE, KEY.SIX],
  [KEY.SEVEN, KEY.EIGHT, KEY.NINE],
  [KEY.DECIMAL_POINT, KEY.ZERO, KEY.BACKSPACE]
]

const styles = StyleSheet.create({
  container: {
    display: "flex"
  },
  key: {
    width: (width - spacing(12)) / 3,
    height: theme.fontHeight.title + theme.spacing.sm * 2,
    padding: theme.spacing.sm
  }
})

type NumericKeyboardProps = {
  value: string
  onChange: (value: string) => void
  isValueAmount?: boolean
  maxValueLength?: number
  hideDecimalPoint?: boolean
  disabledKeys?: KEY[]
}

const NumericKeyboard: React.FC<NumericKeyboardProps> = ({
  value,
  onChange,
  isValueAmount,
  maxValueLength,
  hideDecimalPoint = false,
  disabledKeys = []
}: NumericKeyboardProps) => {
  const _onChange = (key: KEY) => {
    if (disabledKeys && !disabledKeys.includes(key)) {
      if (key === KEY.BACKSPACE) {
        if (isValueAmount && value.length === 1) {
          onChange("0")
        } else {
          onChange(value.substr(0, value.length - 1))
        }
      } else if (!maxValueLength || value.length < maxValueLength) {
        if (
          key === KEY.DECIMAL_POINT &&
          value.indexOf(KEY.DECIMAL_POINT) > -1
        ) {
          onChange(value)
        } else {
          if (isValueAmount && value === "0" && key !== KEY.DECIMAL_POINT) {
            onChange(key)
          } else {
            onChange(value + key)
          }
        }
      }
    }
  }

  return (
    <Container style={styles.container}>
      {keys.map((row, rowIndex) => (
        <Container direction="row" key={rowIndex} justify="space-between">
          {row.map((key) => (
            <TouchableOpacity
              key={key}
              onPress={
                hideDecimalPoint && key === KEY.DECIMAL_POINT
                  ? // eslint-disable-next-line @typescript-eslint/no-empty-function
                    () => {}
                  : () => _onChange(key)
              }
            >
              {key === KEY.BACKSPACE ? (
                <Container
                  justify="center"
                  alignItems="center"
                  noItemsStretch
                  style={styles.key}
                >
                  <Icon name="Backspace" color="black" size="lg" />
                </Container>
              ) : (
                <Typography variant="title" style={styles.key}>
                  {hideDecimalPoint && key === KEY.DECIMAL_POINT ? "" : key}
                </Typography>
              )}
            </TouchableOpacity>
          ))}
        </Container>
      ))}
    </Container>
  )
}

export default NumericKeyboard
