import * as React from "react"
import Svg, { SvgProps, Path, Mask } from "react-native-svg"

function Backspace(props: SvgProps) {
  return (
    <Svg width={32} height={32} viewBox="0 0 32 32" fill="none" {...props}>
      <Path
        d="M14.293 20.707a1 1 0 001.414 0L19 17.414l3.293 3.293a1 1 0 001.414-1.414L20.414 16l3.293-3.293a1 1 0 00-1.414-1.414L19 14.586l-3.293-3.293a1 1 0 00-1.414 1.414L17.586 16l-3.293 3.293a1 1 0 000 1.414z"
        fill={props.color}
      />
      <Mask id="prefix__a" fill="#fff">
        <Path
          fillRule="evenodd"
          clipRule="evenodd"
          d="M11.331 25.825a5.047 5.047 0 01-.454-.324L3.88 19.904c-2.502-2.001-2.502-5.807 0-7.808l6.996-5.597c.147-.118.3-.226.455-.324a3.983 3.983 0 012.455-.842h11.428a4 4 0 014 4v13.334a4 4 0 01-4 4H13.786a3.983 3.983 0 01-2.455-.842z"
        />
      </Mask>
      <Path
        d="M11.331 25.825l1.228-1.578-.077-.06-.082-.053-1.069 1.691zm-.454-.324l1.249-1.562-1.25 1.562zM3.88 19.904l-1.249 1.562 1.25-1.562zm0-7.808l1.25 1.561-1.25-1.561zm6.996-5.597l1.25 1.561-1.25-1.561zm.455-.324l1.069 1.69.082-.052.077-.06-1.228-1.578zm1.069 17.96a3.052 3.052 0 01-.274-.196l-2.499 3.124c.205.164.417.315.635.453l2.137-3.381zm-.274-.196L5.13 18.343 2.63 21.466l6.996 5.597 2.499-3.123zM5.13 18.343a3 3 0 010-4.686L2.63 10.534c-3.503 2.802-3.503 8.13 0 10.932l2.499-3.123zm0-4.686l6.996-5.597-2.499-3.123-6.996 5.597 2.499 3.123zm6.996-5.597a3.04 3.04 0 01.274-.195l-2.137-3.381c-.219.138-.431.29-.636.453l2.499 3.123zm.433-.307c.34-.264.762-.42 1.227-.42v-4a5.983 5.983 0 00-3.683 1.263l2.456 3.157zm1.227-.42h11.428v-4H13.786v4zm11.428 0a2 2 0 012 2h4a6 6 0 00-6-6v4zm2 2v13.334h4V9.333h-4zm0 13.334a2 2 0 01-2 2v4a6 6 0 006-6h-4zm-2 2H13.786v4h11.428v-4zm-11.428 0c-.465 0-.888-.156-1.227-.42l-2.456 3.157a5.983 5.983 0 003.683 1.263v-4z"
        fill={props.color}
        mask="url(#prefix__a)"
      />
    </Svg>
  )
}

export default Backspace
