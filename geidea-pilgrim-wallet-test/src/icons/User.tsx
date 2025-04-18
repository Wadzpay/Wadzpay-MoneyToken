import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function User(props: SvgProps) {
  return (
      <Svg
      width="17"
      height="20"
      fill="none"
      viewBox="0 0 17 20"
      {...props}
    >
      <Path
       fill={props.color}
        d="M11.03 12H5a5.002 5.002 0 00-4.995 5.22C.072 18.79 1.429 20 3 20h10.03c1.57 0 2.928-1.21 2.995-2.78A5.002 5.002 0 0011.03 12zM8.015 10a5 5 0 100-10 5 5 0 000 10z"
      ></Path>
    </Svg>
  )
}

export default User
