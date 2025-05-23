import * as React from "react"
import Svg, { SvgProps, G, Path, Defs, ClipPath, Circle } from "react-native-svg"

function ShowMyQr(props: SvgProps) {
  return (
    <Svg
      width="21"
      height="18"
      fill="none"
      viewBox="0 0 21 18"
      {...props}
    >
      <Path
        fill={props.color}
        d="M4.484 0H2.316A2.319 2.319 0 000 2.316v2.168a.55.55 0 101.1 0V2.316A1.216 1.216 0 012.316 1.1h2.168a.55.55 0 100-1.1zM14.188 0h-2.167a.55.55 0 000 1.1h2.167a1.216 1.216 0 011.216 1.216v2.168a.55.55 0 001.1 0V2.316A2.318 2.318 0 0014.189 0zM4.484 12.653H2.316A1.216 1.216 0 011.1 11.438V9.27a.55.55 0 10-1.1 0v2.167a2.319 2.319 0 002.316 2.317h2.168a.55.55 0 000-1.1zM15.404 9.27v2.167a1.216 1.216 0 01-1.216 1.216h-2.167a.55.55 0 000 1.1h2.167a2.319 2.319 0 002.316-2.316V9.27a.55.55 0 10-1.1 0z"
      ></Path>
      <Path
        fill={props.color}
        d="M4.047 2.236h-1.73a.55.55 0 00-.55.55v8.184a.55.55 0 00.55.55h1.73a.55.55 0 00.55-.55V2.786a.55.55 0 00-.55-.55zM9.119 2.236H7.386a.55.55 0 00-.55.55v8.184a.55.55 0 00.55.55h1.733a.55.55 0 00.55-.55V2.786a.55.55 0 00-.55-.55zM5.793 2.236a.55.55 0 00-.55.55v8.18a.55.55 0 001.1 0v-8.18a.55.55 0 00-.55-.55zM14.28 2.17h-1.734a.55.55 0 00-.55.55v8.184a.55.55 0 00.55.55h1.733a.55.55 0 00.55-.55V2.72a.55.55 0 00-.55-.55zM10.803 2.17a.55.55 0 00-.55.55v8.184a.55.55 0 101.1 0V2.72a.55.55 0 00-.55-.55z"
      ></Path>
      <Circle
        cx="14.842"
        cy="11.175"
        r="5.658"
        fill="#fff"
        stroke="#494949"
      ></Circle>
      <G clipPath="url(#clip0_2270_23732)">
        <Path
          fill={props.color}
          d="M16.561 11.175c-.515 0-.976.228-1.291.587l-1.091-.672c.056-.153.091-.316.091-.489 0-.112-.016-.221-.04-.326l1.45-.67c.21.257.525.424.883.424a1.147 1.147 0 000-2.29c-.631 0-1.145.513-1.145 1.145 0 .07.01.137.02.204l-1.449.67a1.425 1.425 0 00-1.15-.587 1.432 1.432 0 100 2.864c.412 0 .779-.176 1.041-.455l1.09.67a1.718 1.718 0 001.594 2.364 1.72 1.72 0 100-3.438l-.003-.001z"
        ></Path>
      </G>
      <Defs>
        <ClipPath id="clip0_2270_23732">
          <Path
            fill="#fff"
            d="M0 0H6.877V6.877H0z"
            transform="translate(11.404 7.736)"
          ></Path>
        </ClipPath>
      </Defs>
    </Svg>
  )
}

export default ShowMyQr
