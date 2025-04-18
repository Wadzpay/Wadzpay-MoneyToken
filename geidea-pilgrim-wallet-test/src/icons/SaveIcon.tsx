import * as React from "react"
import Svg, { SvgProps, Path, Rect, Circle } from "react-native-svg"

function SaveIcon(props: SvgProps) {
  return (
    <Svg
    width="29"
    height="29"
    fill="none"
    viewBox="0 0 24 24"
  >
    <Circle cx="12" cy="12" r="11.5" fill="#FFC235" stroke="#000"></Circle>
    <Path
      stroke="#0F151B"
      d="M13.559 10.235v.5h1.622c.175 0 .276.218.14.354l-3.24 3.24a.203.203 0 01-.287 0l-3.24-3.24a.208.208 0 01.147-.354h1.623V6.705c0-.111.093-.205.205-.205h2.824c.112 0 .206.094.206.206v3.53zM7.706 17.5a.208.208 0 01-.206-.206c0-.112.094-.206.206-.206h8.47c.113 0 .206.094.206.206a.208.208 0 01-.206.206h-8.47z"
    ></Path>
  </Svg>
  )
}

export default SaveIcon
