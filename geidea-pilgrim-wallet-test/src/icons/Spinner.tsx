import * as React from "react"
import Svg, { SvgProps, G, Path } from "react-native-svg"
/* SVGR has dropped some elements not supported by react-native-svg: animateTransform */

function Spinner(props: SvgProps) {
  return (
    <Svg width={20} height={20} viewBox="0 0 128 128" {...props}>
      <G>
        <Path
          d="M64 9.75A54.25 54.25 0 009.75 64H0a64 64 0 01128 0h-9.75A54.25 54.25 0 0064 9.75z"
          fill={props.color}
        />
      </G>
    </Svg>
  )
}

export default Spinner
