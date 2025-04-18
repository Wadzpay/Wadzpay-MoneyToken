import * as React from "react"
import Svg, { SvgProps, Path, Rect, Circle } from "react-native-svg"

function Link(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path fill="#fff" d="M24 0v24H0V0z" />
      <Rect
        x={3}
        y={3}
        width={18}
        height={18}
        rx={2}
        stroke={props.color}
        strokeWidth={2}
      />
      <Path fill="#fff" d="M9 2h13v13H9z" />
      <Circle cx={9} cy={3} r={1} fill={props.color} />
      <Circle cx={21} cy={15} r={1} fill={props.color} />
      <Path
        d="M13.72 1.922a.631.631 0 01.18-.51.631.631 0 01.51-.18l6.902.489a.505.505 0 01.18.038c.04.016.079.031.104.033.014.014.028.027.04.015.039.016.053.03.092.045.053.03.08.057.121.098.041.04.069.068.098.121a.16.16 0 01.044.092c.014.014.028.028.016.04l.046.117s.014.014.002.026c.016.04.02.09.023.141l.488 6.902a.627.627 0 01-.689.69.882.882 0 01-.793-.794l-.362-5.113-8.445 8.445c-.273.273-.733.24-1.048-.074-.315-.315-.347-.775-.074-1.048L19.6 3.05l-5.113-.362c-.382-.002-.737-.357-.766-.766z"
        fill={props.color}
        stroke={props.color}
        strokeWidth={0.5}
      />
    </Svg>
  )
}

export default Link
