import * as React from "react"
import Svg, { SvgProps, Rect } from "react-native-svg"

function CheckboxUnchecked(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Rect
        x={1}
        y={1}
        width={22}
        height={22}
        rx={2}
        stroke={props.color}
        strokeWidth={2}
      />
    </Svg>
  )
}

export default CheckboxUnchecked
