import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function Faq(props: SvgProps) {
  return (
    <Svg width={20} height={20} viewBox="0 0 20 20" fill="none" {...props}>
      <Path
        d="M3.333 2.5a1.67 1.67 0 00-1.666 1.667v10l2.5-2.5h7.5A1.67 1.67 0 0013.333 10V4.167A1.67 1.67 0 0011.667 2.5H3.333zM15 6.667V10a3.336 3.336 0 01-3.333 3.333h-5v.834a1.67 1.67 0 001.666 1.666h7.5l2.5 2.5v-10a1.67 1.67 0 00-1.666-1.666H15z"
        fill="#FFC235"
      />
    </Svg>
  )
}

export default Faq
