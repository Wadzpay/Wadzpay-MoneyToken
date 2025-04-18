import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function Search(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M20.5 20.5l-3.986-3.994M19 10.5a8.5 8.5 0 11-17 0 8.5 8.5 0 0117 0z"
        stroke={props.color}
        strokeWidth={2}
        strokeLinecap="round"
      />
    </Svg>
  )
}

export default Search
