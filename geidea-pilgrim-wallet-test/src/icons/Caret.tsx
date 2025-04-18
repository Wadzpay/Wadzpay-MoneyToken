import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function Caret(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M11.168 14.752a1 1 0 001.664 0l2.131-3.197A1 1 0 0014.133 10H9.869a1 1 0 00-.833 1.555l2.132 3.197z"
        fill="#BDC2CA"
      />
    </Svg>
  )
}

export default Caret
