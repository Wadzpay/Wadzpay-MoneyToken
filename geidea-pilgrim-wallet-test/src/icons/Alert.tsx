import * as React from "react"
import Svg, { SvgProps, G, Path, Defs, ClipPath } from "react-native-svg"

function Alert(props: SvgProps) {
  return (
    <Svg width={20} height={20} {...props}
      fill="none"
      viewBox="0 0 16 16"
    >
      <Path
        fill="#F6F6F6"
        d="M6.753 2.576l-5.599 9.696A1.152 1.152 0 002.152 14h11.196a1.152 1.152 0 00.998-1.728L8.748 2.576a1.152 1.152 0 00-1.995 0z"
      ></Path>
      <Path
        fill="#E33434"
        d="M7.823 5.567h-.146a.651.651 0 00-.65.65V9.33c0 .36.29.651.65.651h.146c.36 0 .651-.291.651-.65V6.217a.651.651 0 00-.65-.651zM7.75 12.449a.724.724 0 100-1.449.724.724 0 000 1.449z"
      ></Path>
    </Svg>
  )
}

export default Alert
