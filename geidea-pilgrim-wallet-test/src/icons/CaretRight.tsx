import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function CaretRight(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M9.125 21.244a1.125 1.125 0 01-.796-1.92l6.705-6.706-6.705-6.704a1.125 1.125 0 011.591-1.59l7.5 7.5a1.125 1.125 0 010 1.59l-7.5 7.5a1.121 1.121 0 01-.795.33z"
        fill={props.color}
      />
    </Svg>
  )
}

export default CaretRight
