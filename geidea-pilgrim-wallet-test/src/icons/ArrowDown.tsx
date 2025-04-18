import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function ArrowDown(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M19.671 14.835c.22.215.329.484.329.78 0 .295-.11.564-.329.78l-7.403 7.256a.721.721 0 01-.165.134s-.027.027-.055.027a.596.596 0 00-.137.08c-.027 0-.054 0-.054.027-.055.027-.083.027-.138.054-.082.027-.137.027-.219.027-.082 0-.137 0-.22-.027a.212.212 0 01-.136-.054c-.028 0-.055 0-.055-.027l-.165-.08s-.027 0-.027-.027a.721.721 0 01-.165-.134L3.33 16.394a1.088 1.088 0 010-1.559 1.142 1.142 0 011.59 0l5.484 5.375V1.102c0-.618.494-1.102 1.124-1.102.631 0 1.125.484 1.125 1.102V20.21l5.484-5.375c.383-.43 1.096-.43 1.535 0z"
        fill={props.color}
      />
    </Svg>
  )
}

export default ArrowDown
