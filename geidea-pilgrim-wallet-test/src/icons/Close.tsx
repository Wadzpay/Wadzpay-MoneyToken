import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function Close(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        fillRule="evenodd"
        clipRule="evenodd"
        d="M13.617 12l6.048-6.05a1.142 1.142 0 10-1.616-1.615L12 10.383 5.951 4.335A1.144 1.144 0 004.335 5.95L10.383 12l-6.048 6.049a1.143 1.143 0 001.616 1.616L12 13.617l6.049 6.048a1.14 1.14 0 001.616 0 1.143 1.143 0 000-1.616l-6.048-6.05z"
        fill={props.color}
        stroke={props.color}
        strokeWidth={0.5}
      />
    </Svg>
  )
}

export default Close
