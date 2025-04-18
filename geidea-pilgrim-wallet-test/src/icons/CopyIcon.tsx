import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function CopyIcon(props: SvgProps) {
  return (
    <Svg width={20} height={20} viewBox="0 0 20 20" fill="none" {...props}>
      <Path
        d="M3.333 1.667c-.92 0-1.666.745-1.666 1.667V15h1.666V3.334H15V1.667H3.333zM6.667 5C5.746 5 5 5.746 5 6.667v10c0 .921.746 1.667 1.667 1.667h10c.921 0 1.666-.746 1.666-1.667v-10c0-.921-.745-1.667-1.666-1.667h-10zm0 1.667h10v10h-10v-10z"
        fill="#000"
      />
    </Svg>
  )
}

export default CopyIcon
