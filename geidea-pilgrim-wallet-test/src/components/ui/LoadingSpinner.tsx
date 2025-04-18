import React from "react"
import { Animated, Easing } from "react-native"

import Icon from "./Icon"
import theme, { IconColorVariant, IconSizeVariant } from "./theme"

type Props = {
  color?: IconColorVariant
  size?: IconSizeVariant
}

const LoadingSpinner: React.FC<Props> = ({
  color = "regular",
  size = "md"
}: Props) => {
  const spinValue = new Animated.Value(0)

  Animated.loop(
    Animated.timing(spinValue, {
      toValue: 1,
      duration: 1000,
      easing: Easing.linear, // Easing is an additional import from react-native
      useNativeDriver: true // To make use of native driver for performance
    })
  ).start()

  const spin = spinValue.interpolate({
    inputRange: [0, 1],
    outputRange: ["0deg", "360deg"]
  })

  return (
    <Animated.View
      style={{
        transform: [{ rotate: spin }],
        width: theme.iconSize[size],
        height: theme.iconSize[size]
      }}
    >
      <Icon name="Spinner" color={color} size={size} />
    </Animated.View>
  )
}

export default LoadingSpinner
