import * as React from "react"
import Svg, { SvgProps, Path, Rect } from "react-native-svg"

function CreditArrow(props: SvgProps) {
  return (
    <Svg
      width={32}
      height={32}
      fill="none"
      viewBox="0 0 32 32"
      {...props}
    >
      <Rect
        width="32"
        height="32"
        x="32"
        y="32"
        fill="#00AF5B"
        rx="16"
        transform="rotate(-180 32 32)"
      ></Rect>
      <Path
        fill="#fff"
        d="M15.167 11.128v10.1l-4.659-4.64-1.175 1.18L16 24.406l6.667-6.64-1.175-1.17-4.659 4.631v-10.1h-1.666z"
      ></Path>
    </Svg>
  )
}

export default CreditArrow
