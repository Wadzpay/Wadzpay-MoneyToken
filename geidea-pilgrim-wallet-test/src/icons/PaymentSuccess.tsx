import * as React from "react"
import Svg, { SvgProps, Path, Rect } from "react-native-svg"

function PaymentSuccess(props: SvgProps) {
  return (
    <Svg width={21} height={21} viewBox="0 0 21 21" fill="none" {...props}>
      <Path
        d="M5.578 5.031a.548.548 0 00-.547.547v5.469c0 .301.246.547.547.547h9.844a.548.548 0 00.547-.547V5.578a.548.548 0 00-.547-.547H5.578zm1.047 1.094h7.752a.819.819 0 00.498.5v3.377a.81.81 0 00-.498.498H6.625a.819.819 0 00-.5-.498V6.625a.827.827 0 00.5-.5zm3.875.547a1.641 1.641 0 100 3.282 1.641 1.641 0 000-3.282zM7.766 7.766a.547.547 0 10-.001 1.093.547.547 0 000-1.093zm5.468 0a.547.547 0 100 1.093.547.547 0 000-1.093zm-3.28 4.921v1.094H8.286l2.187 2.188 2.188-2.188h-1.615v-1.094H9.953z"
        fill="#1EDA25"
      />
      <Rect opacity={0.2} width={21} height={21} rx={5} fill="#1EDA25" />
    </Svg>
  )
}

export default PaymentSuccess
