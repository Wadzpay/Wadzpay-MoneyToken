import * as React from "react"
import Svg, { SvgProps, Rect, G, Path, Defs, ClipPath } from "react-native-svg"

function Wtk(props: SvgProps) {
  return (
    <Svg width={40} height={40} viewBox="0 0 40 40" fill="none" {...props}>
      <Rect width={40} height={40} rx={8} fill="#FFC235" />
      <G clipPath="url(#prefix__clip0_57_18314)" fill="#fff">
        <Path d="M20.985 24.298h-3.088a1.18 1.18 0 01-1.092-.816l-1.075-3.596a.884.884 0 01.856-1.146h3.822a.602.602 0 01.588.468.58.58 0 01-.324.644.588.588 0 01-.25.058h-3.46l.955 3.207h3.031l1.837-7.168c.116-.399.371-.742.717-.97l1.248-.76a.61.61 0 01.822.149.573.573 0 01.084.45.601.601 0 01-.266.376l-1.281.779a.618.618 0 00-.193.272l-1.846 7.218a1.12 1.12 0 01-1.087.839l.002-.004zM17.977 25.297a.584.584 0 101.168 0 .584.584 0 00-1.168 0zM20.217 25.297a.584.584 0 101.168 0 .584.584 0 00-1.168 0z" />
        <Path d="M27.86 27.13c.325.255.797.2 1.055-.127a11.286 11.286 0 002.37-8.082 11.29 11.29 0 00-3.857-7.486 11.29 11.29 0 00-7.958-2.757 11.285 11.285 0 00-7.664 3.493 11.28 11.28 0 00-3.137 7.816 11.295 11.295 0 003.12 7.823 11.284 11.284 0 008.222 3.523c2.718 0 5.327-.968 7.398-2.755a.751.751 0 10-.983-1.137 9.79 9.79 0 01-6.909 2.377 9.787 9.787 0 01-6.64-3.044 9.793 9.793 0 01-2.708-6.787 9.784 9.784 0 012.723-6.78 9.797 9.797 0 016.647-3.032 9.777 9.777 0 016.902 2.393 9.787 9.787 0 013.347 6.494 9.789 9.789 0 01-2.058 7.01.753.753 0 00.127 1.056l.002.002z" />
        <Path d="M30.016 10.216a13.95 13.95 0 00-9.565-4.21 13.946 13.946 0 00-9.814 3.588.75.75 0 101.004 1.116 12.429 12.429 0 018.762-3.202c3.24.106 6.272 1.44 8.538 3.757a12.437 12.437 0 013.557 8.623 12.44 12.44 0 01-3.405 8.686 12.451 12.451 0 01-8.47 3.91c-3.239.164-6.371-.921-8.817-3.046a12.441 12.441 0 01-4.253-8.303 12.44 12.44 0 012.682-8.934.75.75 0 10-1.173-.938A13.943 13.943 0 006.058 21.27a13.94 13.94 0 004.763 9.301 13.935 13.935 0 009.876 3.412 13.93 13.93 0 009.488-4.38 13.936 13.936 0 003.813-9.727 13.946 13.946 0 00-3.984-9.66h.002z" />
        <Path d="M23.682 17.168l3.248-.382h-3.152l-.096.382zM23.418 18.202l5.184-.663h-5.017l-.167.663z" />
      </G>
      <Defs>
        <ClipPath id="prefix__clip0_57_18314">
          <Path fill="#fff" transform="translate(6 6)" d="M0 0h28v28H0z" />
        </ClipPath>
      </Defs>
    </Svg>
  )
}

export default Wtk
