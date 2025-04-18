import * as React from "react"
import Svg, { SvgProps, G, Path, Circle } from "react-native-svg"
/* SVGR has dropped some elements not supported by react-native-svg: animateTransform */

function Success(props: SvgProps) {
  return (
    <Svg width="58"
    height="58"
    fill="none"
    viewBox="0 0 58 58"
     {...props}>
     <Circle cx="29" cy="29" r="24" fill="#27AE60"></Circle>
      <Circle
        cx="29"
        cy="29"
        r="26.5"
        stroke="#27AE60"
        strokeOpacity="0.7"
        strokeWidth="5"
      ></Circle>
      <Path
        fill="#fff"
        d="M37.78 20.627L25.573 32.835l-5.355-5.355a2.135 2.135 0 00-3.023 0 2.135 2.135 0 000 3.023l6.845 6.844c.814.866 2.204.864 3.04.026L40.802 23.65a2.135 2.135 0 000-3.023 2.135 2.135 0 00-3.023 0z"
      ></Path>
    </Svg>
  )
}

export default Success
