import * as React from "react"
import Svg, { SvgProps, G, Path } from "react-native-svg"

function ShareNew(props: SvgProps) {
  return (
    <Svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" {...props}>
      <Path d="M18 14a4 4 0 00-3.08 1.48l-5.1-2.35a3.64 3.64 0 000-2.26l5.1-2.35A4 4 0 1014 6a4.17 4.17 0 00.07.71L8.79 9.14a4 4 0 100 5.72l5.28 2.43A4.17 4.17 0 0014 18a4 4 0 104-4zm0-10a2 2 0 11-2 2 2 2 0 012-2zM6 14a2 2 0 112-2 2 2 0 01-2 2zm12 6a2 2 0 112-2 2 2 0 01-2 2z"
       fill={props.color}
      />
    </Svg>
  );
}

export default ShareNew;