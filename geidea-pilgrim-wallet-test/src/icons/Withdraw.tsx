import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function Withdraw(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M12 12a3 3 0 100 5.999A3 3 0 0012 12zm0 4a1 1 0 110-2 1 1 0 010 2zm-.71-6.29a1 1 0 00.33.21.94.94 0 00.76 0 1 1 0 00.33-.21L15 7.46A1.032 1.032 0 1013.54 6l-.54.59V3a1 1 0 00-2 0v3.59L10.46 6A1.032 1.032 0 109 7.46l2.29 2.25zM19 15a1 1 0 10-2 0 1 1 0 002 0zm1-7h-3a1 1 0 100 2h3a1 1 0 011 1v8a1 1 0 01-1 1H4a1 1 0 01-1-1v-8a1 1 0 011-1h3a1 1 0 000-2H4a3 3 0 00-3 3v8a3 3 0 003 3h16a3 3 0 003-3v-8a3 3 0 00-3-3zM5 15a1 1 0 102 0 1 1 0 00-2 0z"
        fill={props.color}
      />
    </Svg>
  )
}

export default Withdraw
