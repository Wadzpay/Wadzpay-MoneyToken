import * as React from "react"
import Svg, { SvgProps, Path, Rect } from "react-native-svg"

function DebitArrow(props: SvgProps) {
  return (
    <Svg
      width={32}
      height={32}
      fill="none"
      viewBox="0 0 32 32"
      {...props}
    >
     <Rect width="32" height="32" fill="#D80027" rx="16"></Rect>
      <Path
        fill="#fff"
        d="M16.833 22.774v-10.1l4.659 4.64 1.175-1.18L16 9.496l-6.667 6.64 1.175 1.17 4.659-4.631v10.1h1.666z"
      ></Path>
    </Svg>
  )
}

export default DebitArrow
