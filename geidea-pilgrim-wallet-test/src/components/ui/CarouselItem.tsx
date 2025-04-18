import React from "react"
import {
  View,
  Image,
  StyleSheet,
  ImageSourcePropType,
  Dimensions,
  ImageBackground
} from "react-native"
import { isIOS } from "~/utils"

import theme, { spacing } from "./theme"
import Typography from "./Typography"

const { width } = Dimensions.get("window")

const styles = StyleSheet.create({
  container: {
    width: width, // Corelates with horizontal margin
    alignItems: "center"
  },
  image: {
    marginTop: 100,
    margin: theme.spacing.lg,
    width:300,
    height:220
  },
  header: {
    padding: theme.spacing.xs,
    marginTop: 40,
    fontWeight: "bold"
  },
  body: {
    marginTop: isIOS ? -280 : -330,
    fontWeight: "400",
    color: "#4E4E4E"
  },
  header2: {
    padding: theme.spacing.lg,
    fontWeight: "bold"
  },
  header3: {
    padding: theme.spacing.lg,
    marginTop: 175,
    fontWeight: "bold"
  },
  imagePager: {
    marginTop: -330,
    alignItems: "center"
  }
})

export type CarouselItemProps = {
  id: number
  image: ImageSourcePropType
  imagePager: ImageSourcePropType
  header: string
  header2: string
  header3: string
  body: string
}

const CarouselItem: React.FC<CarouselItemProps> = ({
  id,
  image,
  imagePager,
  header,
  header2,
  header3,
  body
}: CarouselItemProps) => {
  return (
    <View key={id} style={styles.container}>
      <ImageBackground
        source={image}
        style={{ width: "100%", height: "100%", alignItems: "center" }}
      >
        <Image source={imagePager} style={styles.image} />
        <Typography variant="subtitle" style={styles.header}>
          {header}
        </Typography>
      </ImageBackground>

      {/* <Typography variant="subtitle" style={styles.header2}>
        {header2}
      </Typography>
      <Typography variant="subtitle" style={styles.header3}>
        {header3}
      </Typography>
      <Typography color="grayMedium" style={styles.body}>
        {body}
      </Typography> */}
    </View>
  )
}

export default CarouselItem
