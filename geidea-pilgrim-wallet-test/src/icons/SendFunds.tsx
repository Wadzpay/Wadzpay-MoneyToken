import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function SendFunds(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M10.46 6l.54-.59V9a1 1 0 002 0V5.41l.54.55A1 1 0 0015 6a1.001 1.001 0 000-1.42l-2.29-2.29a1 1 0 00-.33-.21 1 1 0 00-.76 0 1 1 0 00-.33.21L9 4.54A1.032 1.032 0 1010.46 6zM12 12a3 3 0 100 5.999A3 3 0 0012 12zm0 4a1 1 0 110-2.002A1 1 0 0112 16zm-7-1a1 1 0 102 0 1 1 0 00-2 0zm14 0a1 1 0 10-2 0 1 1 0 002 0zm1-7h-4a1 1 0 100 2h4a1 1 0 011 1v8a1 1 0 01-1 1H4a1 1 0 01-1-1v-8a1 1 0 011-1h4a1 1 0 000-2H4a3 3 0 00-3 3v8a3 3 0 003 3h16a3 3 0 003-3v-8a3 3 0 00-3-3z"
        fill={props.color}
      />
    </Svg>
  )
}

export default SendFunds
