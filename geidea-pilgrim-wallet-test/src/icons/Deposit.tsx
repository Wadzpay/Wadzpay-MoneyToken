import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function Deposit(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M12 12a3 3 0 100 5.999A3 3 0 0012 12zm0 4a1 1 0 110-2 1 1 0 010 2zm-7-1a1 1 0 102 0 1 1 0 00-2 0zm14 0a1 1 0 10-2 0 1 1 0 002 0zm1-7h-1a1 1 0 100 2h1a1 1 0 011 1v8a1 1 0 01-1 1H4a1 1 0 01-1-1v-8a1 1 0 011-1h8a1 1 0 100-2H4a3 3 0 00-3 3v8a3 3 0 003 3h16a3 3 0 003-3v-8a3 3 0 00-3-3z"
        fill={props.color}
      />
      <Path
        d="M14.794 8.707A1 1 0 0114.501 8V3s-.058-.455.293-.727C15.146 2 15.501 2 15.501 2s.387 0 .708.273c.32.272.293.727.293.727v5a1 1 0 01-1.708.707z"
        fill={props.color}
      />
      <Path
        d="M12.293 4.794A1 1 0 0113 4.502h5s.455-.059.727.292c.273.352.273.708.273.708s0 .386-.273.707c-.272.32-.727.293-.727.293h-5a1 1 0 01-.707-1.708z"
        fill={props.color}
      />
    </Svg>
  )
}

export default Deposit
