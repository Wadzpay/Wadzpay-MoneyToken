import * as React from "react"
import Svg, { SvgProps, Path, Circle } from "react-native-svg"

function WalletBalanceFee(props: SvgProps) {
  return (
    <Svg
    width="58"
    height="58"
    fill="none"
    viewBox="0 0 58 58"
    >
    <Circle cx="29" cy="29" r="24" fill="#FFC235"></Circle>
    <Circle
      cx="29"
      cy="29"
      r="26.5"
      stroke="#FFC235"
      strokeOpacity="0.5"
      strokeWidth="5"
    ></Circle>
    <Path
      fill="#2D2D2D"
      d="M31.069 30.578v1.655a4.14 4.14 0 004.138 4.138h3.31v.828a2.49 2.49 0 01-2.483 2.483H19.484A2.49 2.49 0 0117 37.199V25.613a2.485 2.485 0 012.483-2.483h16.552a2.485 2.485 0 012.482 2.483v.827h-3.31a4.14 4.14 0 00-4.138 4.138zm9.931 0v1.655a2.49 2.49 0 01-2.483 2.483h-3.31a2.49 2.49 0 01-2.483-2.483v-1.655a2.485 2.485 0 012.483-2.482h3.31c.63 0 1.217.24 1.655.637.505.447.828 1.109.828 1.845zm-4.138.828a.83.83 0 00-.828-.828.83.83 0 00-.827.828.83.83 0 00.828.828.83.83 0 00.827-.828zm-2.805-11.603a2.508 2.508 0 00-1.358-1.58 2.513 2.513 0 00-2.085.008l-6.944 3.244h10.867l-.48-1.672z"
    ></Path>
    </Svg>
  )
}

export default WalletBalanceFee

