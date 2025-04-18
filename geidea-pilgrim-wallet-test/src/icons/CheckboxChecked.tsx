import * as React from "react"
import Svg, { SvgProps, Rect, Path } from "react-native-svg"

function CheckboxChecked(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Rect
        x={1}
        y={1}
        width={22}
        height={22}
        rx={2}
        fill="#FFBC04"
        stroke="#FFBC04"
        strokeWidth={2}
      />
      <Path
        d="M7.111 12.611l3.217 3.056 2.09-1.986 2.09-1.986 3.539-3.362"
        stroke="#fff"
        strokeWidth={2}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </Svg>
  )
}

export default CheckboxChecked
