import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function Calendar(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M20 10V8a1 1 0 00-1-1h-1v1a1 1 0 01-2 0V7H8v1a1 1 0 01-2 0V7H5a1 1 0 00-1 1v2h16zm0 2H4v6a1 1 0 001 1h14a1 1 0 001-1v-6zm-2-7h1a3 3 0 013 3v10a3 3 0 01-3 3H5a3 3 0 01-3-3V8a3 3 0 013-3h1V4a1 1 0 012 0v1h8V4a1 1 0 012 0v1z"
        fill={props.color}
      />
    </Svg>
  )
}

export default Calendar
