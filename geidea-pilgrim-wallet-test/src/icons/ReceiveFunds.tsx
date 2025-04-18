import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function ReceiveFunds(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        d="M8.364 20.182H4.727a.91.91 0 01-.909-.91v-3.636a.91.91 0 00-1.818 0v3.637A2.727 2.727 0 004.727 22h3.637a.91.91 0 000-1.818zm12.727-5.455a.91.91 0 00-.91.91v3.636a.91.91 0 01-.908.909h-3.637a.91.91 0 000 1.818h3.637A2.727 2.727 0 0022 19.273v-3.637a.91.91 0 00-.91-.909zM19.273 2h-3.637a.91.91 0 000 1.818h3.637a.91.91 0 01.909.91v3.636a.91.91 0 001.818 0V4.727A2.727 2.727 0 0019.273 2zM2.909 9.273a.91.91 0 00.91-.91V4.728a.91.91 0 01.908-.909h3.637a.91.91 0 000-1.818H4.727A2.727 2.727 0 002 4.727v3.637a.91.91 0 00.91.909zm7.273-3.637H6.545a.91.91 0 00-.909.91v3.636a.909.909 0 00.91.909h3.636a.909.909 0 00.909-.91V6.546a.91.91 0 00-.91-.909zm-.91 3.637H7.456V7.455h1.818v1.818zm4.546 1.818h3.636a.91.91 0 00.91-.91V6.546a.909.909 0 00-.91-.909h-3.636a.909.909 0 00-.909.91v3.636a.909.909 0 00.91.909zm.91-3.636h1.818v1.818h-1.819V7.455zm-4.546 5.454H6.545a.91.91 0 00-.909.91v3.635a.91.91 0 00.91.91h3.636a.909.909 0 00.909-.91v-3.636a.909.909 0 00-.91-.909zm-.91 3.636H7.456v-1.818h1.818v1.819zm4.546-.909a.909.909 0 00.91-.909.909.909 0 000-1.818h-.91a.909.909 0 00-.909.91v.908a.909.909 0 00.91.91zm3.636-2.727a.909.909 0 00-.909.91v2.726a.909.909 0 100 1.819h.91a.909.909 0 00.909-.91v-3.636a.909.909 0 00-.91-.909zm-3.636 3.636a.91.91 0 100 1.819.91.91 0 000-1.819z"
        fill={props.color}
      />
    </Svg>
  )
}

export default ReceiveFunds
