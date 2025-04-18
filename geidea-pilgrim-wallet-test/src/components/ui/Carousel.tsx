import React, { useRef } from "react"
import { View, Animated, Dimensions, StyleSheet } from "react-native"

import CarouselItem, { CarouselItemProps } from "./CarouselItem"
import Container from "./Container"
import theme, { spacing } from "./theme"

const { width } = Dimensions.get("window")

const styles = StyleSheet.create({
  controlsContainer: {
    marginTop: theme.spacing.xl
  },
  controls: {
    width: theme.spacing.md,
    height: theme.borderWidth.md,
    // backgroundColor: theme.colors.black,
    marginHorizontal: spacing(0.5)
  }
})

type Props = {
  data: CarouselItemProps[]
}

const Carousel: React.FC<Props> = ({ data }: Props) => {
  const scrollX = useRef(new Animated.Value(0)).current
  const position = Animated.divide(scrollX, width)
  return (
    <View>
      <Animated.FlatList
        horizontal
        pagingEnabled
        showsHorizontalScrollIndicator={false}
        data={data}
        keyExtractor={(item) => item.id.toString()}
        snapToInterval={width}
        snapToAlignment="center"
        decelerationRate="fast"
        renderItem={({ item }) => {
          return <CarouselItem {...item} />
        }}
        onScroll={Animated.event(
          [{ nativeEvent: { contentOffset: { x: scrollX } } }],
          { useNativeDriver: false }
        )}
      />
      {/*   <Container
        direction="row"
        justify="center"
        noItemsStretch
        style={styles.controlsContainer}
      >
        {data.map((_, index) => {
          const backgroundColor = position.interpolate({
            inputRange: [index - 1, index, index + 1],
            outputRange: ["#BFC3C9", "#FFA63C", "#BFC3C9"],
            extrapolate: "clamp"
          })
          return (
            <Animated.View
              key={index}
              style={{ backgroundColor, ...styles.controls }}
            />
          )
        })}
       {data.map((_, index) => {
          const opacity = position.interpolate({
            inputRange: [index - 1, index, index + 1],
            outputRange: [0.2, 0.6, 0.2],
            extrapolate: "clamp"
          })
          return (
            <Animated.View
              key={index}
              style={{ opacity, ...styles.controls }}
            />
          )
        })} 
      </Container>*/}
    </View>
  )
}

export default Carousel
