import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function EyeClosed(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M2.084 9.134c.862 1.706 2.227 3.152 3.95 4.182l-2.115 2.115a.781.781 0 001.105 1.105l2.48-2.481c1.17.484 2.425.775 3.715.859v3.54a.781.781 0 101.562 0v-3.54a11.696 11.696 0 003.714-.859l2.481 2.481a.781.781 0 101.105-1.105l-2.115-2.114c1.723-1.031 3.088-2.477 3.95-4.183a.781.781 0 00-1.395-.705c-1.449 2.87-4.678 4.948-8.521 4.948-3.846 0-7.073-2.08-8.521-4.948a.781.781 0 10-1.395.705z"
        fill={props.color}
        stroke={props.color}
      />
    </Svg>
  )
}

export default EyeClosed
