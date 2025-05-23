import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function EyeOpen(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M12 12.438a3.52 3.52 0 00-3.515 3.515A3.52 3.52 0 0012 19.47a3.52 3.52 0 003.516-3.516A3.52 3.52 0 0012 12.438zm0 5.468a1.955 1.955 0 01-1.953-1.953c0-1.077.876-1.953 1.953-1.953s1.953.876 1.953 1.953A1.955 1.955 0 0112 17.906z"
        fill={props.color}
        stroke={props.color}
      />
      <Path
        d="M21.916 14.103c-.862-1.707-2.227-3.153-3.95-4.183l2.115-2.115A.781.781 0 1018.976 6.7l-2.48 2.481a11.693 11.693 0 00-3.715-.859v-3.54a.781.781 0 10-1.562 0v3.54c-1.29.084-2.545.375-3.714.86L5.024 6.7a.781.781 0 10-1.105 1.105L6.034 9.92c-1.723 1.03-3.088 2.476-3.95 4.182a.781.781 0 001.395.705C4.928 11.937 8.157 9.859 12 9.859c3.845 0 7.073 2.08 8.521 4.948a.781.781 0 101.395-.704z"
        fill={props.color}
        stroke={props.color}
      />
    </Svg>
  )
}

export default EyeOpen
