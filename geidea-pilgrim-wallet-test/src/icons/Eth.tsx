import * as React from "react"
import Svg, { SvgProps, Rect, Path } from "react-native-svg"

function Eth(props: SvgProps) {
  return (
    <Svg width={40} height={40} viewBox="0 0 40 40" fill="none" {...props}>
      <Rect width={40} height={40} rx={8} fill="#7F89C8" />
      <Path
        fillRule="evenodd"
        clipRule="evenodd"
        d="M19.787 8.232c-.051.128-1.586 2.834-3.412 6.015-1.826 3.18-3.334 5.824-3.353 5.875-.024.066.967.714 3.478 2.271l3.511 2.178 3.51-2.18c2.523-1.565 3.502-2.205 3.477-2.272C26.871 19.772 20.045 8 19.97 8c-.05 0-.133.104-.183.232zM13.03 21.69C13.166 21.93 19.982 32 20.01 32c.048-.002 7.017-10.341 6.99-10.37-.026-.026-6.54 4.002-6.811 4.212l-.167.13-3.373-2.091a636.72 636.72 0 01-3.525-2.194c-.139-.094-.147-.094-.094.002z"
        fill="#fff"
      />
    </Svg>
  )
}

export default Eth
