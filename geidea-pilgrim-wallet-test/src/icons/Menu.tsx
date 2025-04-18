import * as React from "react"
import Svg, { SvgProps, Path, Circle } from "react-native-svg"

function Menu(props: SvgProps) {
  return (
    <Svg width={32} height={32} viewBox="0 0 32 32" fill="none" {...props}>
      <Path
        d="M8.8 22.175c0-2.72 3.5-3.4 7.6-3.4 4.122 0 7.6.704 7.6 3.424 0 2.72-3.5 3.401-7.6 3.401-4.12 0-7.6-.705-7.6-3.425zm2.571-11.284A5.148 5.148 0 0116.4 5.6a5.146 5.146 0 015.029 5.291 5.146 5.146 0 01-5.029 5.292 5.15 5.15 0 01-5.029-5.292z"
        fill={props.color}
      />
      <Circle cx={16} cy={16} r={15.5} stroke="#D2D2D2" />
    </Svg>
  )
}

export default Menu
