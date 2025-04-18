import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function Transactions(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M12 24a12 12 0 110-24 12 12 0 010 24zm0-15.6H7.2v2.404H18l-6-6V8.4zm-6 4.8l6 6v-3.6h4.8v-2.404L6 13.199z"
        fill={props.color}
      />
    </Svg>
  )
}

export default Transactions
