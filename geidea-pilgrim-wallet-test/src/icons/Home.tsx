import * as React from "react"
import Svg, { SvgProps, G, Path, Defs } from "react-native-svg"
/* SVGR has dropped some elements not supported by react-native-svg: filter */

function Home(props: SvgProps) {
  return (
    <Svg width={63} height={64} viewBox="0 0 63 64" fill="none" {...props}>
      <G filter="url(#prefix__filter0_d_1803_19828)">
        <Path
          d="M27.962 42.528v-3.67a1.703 1.703 0 011.708-1.695h3.449a1.715 1.715 0 011.578 1.046c.086.206.13.427.13.65v3.67a1.457 1.457 0 00.428 1.04c.277.276.653.431 1.045.431h2.353a4.155 4.155 0 002.932-1.2 4.093 4.093 0 001.215-2.908V29.44a2.968 2.968 0 00-1.074-2.283l-8.005-6.346a3.717 3.717 0 00-4.738.085l-7.823 6.261A2.967 2.967 0 0020 29.44v10.443A4.133 4.133 0 0024.147 44h2.3a1.477 1.477 0 001.482-1.461l.033-.011z"
          fill={props.color}
        />
      </G>
      <Defs></Defs>
    </Svg>
  )
}

export default Home
