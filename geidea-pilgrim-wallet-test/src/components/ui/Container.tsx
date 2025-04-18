import React, { PropsWithChildren } from "react"
import { View, ViewStyle, StyleProp } from "react-native"

import { spacing as themeSpacing } from "./theme"

const createStyles: (
  direction: ViewStyle["flexDirection"],
  justify: ViewStyle["justifyContent"],
  alignItems: ViewStyle["alignItems"]
) => ViewStyle = (direction, justify, alignItems) => ({
  flexDirection: direction,
  justifyContent: justify,
  alignItems
})

const createItemStyle: (
  spacing: number,
  direction: ViewStyle["flexDirection"],
  noItemsStretch: boolean,
  totalItemsNumber: number,
  index: number
) => ViewStyle = (
  spacing,
  direction,
  noItemsStretch,
  totalItemsNumber,
  index
) => {
  const halfSpacing = themeSpacing(spacing / 2)
  const isLastItem = !direction?.includes("reverse")
    ? totalItemsNumber - 1 === index
    : index === 0
  const isFirstItem = direction?.includes("reverse")
    ? totalItemsNumber - 1 === index
    : index === 0
  return {
    ...(direction?.includes("row")
      ? {
          ...(noItemsStretch ? {} : { height: "100%" }),
          marginRight: !isLastItem ? halfSpacing : 0,
          marginLeft: !isFirstItem ? halfSpacing : 0
        }
      : {
          ...(noItemsStretch ? {} : { width: "100%" }),
          marginBottom: !isLastItem ? halfSpacing : 0,
          marginTop: !isFirstItem ? halfSpacing : 0
        })
  }
}

type Props = PropsWithChildren<{
  spacing?: number
  noItemsStretch?: boolean
  direction?: ViewStyle["flexDirection"]
  justify?: ViewStyle["justifyContent"]
  alignItems?: ViewStyle["alignItems"]
  style?: StyleProp<ViewStyle>
}>

const Container: React.FC<Props> = ({
  spacing = 0,
  noItemsStretch = false,
  direction = "column",
  justify = "flex-start",
  alignItems = "flex-start",
  style,
  children
}: Props) => {
  const containerStyle = createStyles(direction, justify, alignItems)
  return (
    <View style={[containerStyle, style]}>
      {Array.isArray(children) &&
      children.filter((child) => !!child).length > 1 ? (
        children.map((child, i) => (
          <View
            key={`${child}-${i}`}
            style={createItemStyle(
              spacing,
              direction,
              noItemsStretch,
              children.length,
              i
            )}
          >
            {child}
          </View>
        ))
      ) : (
        <View style={createItemStyle(spacing, direction, noItemsStretch, 1, 0)}>
          {children}
        </View>
      )}
    </View>
  )
}

export default Container
