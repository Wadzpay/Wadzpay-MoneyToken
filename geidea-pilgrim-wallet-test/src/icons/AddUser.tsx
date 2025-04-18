import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function AddUser(props: SvgProps) {
  return (
    <Svg
    width="22"
    height="26"
    fill="none"
    viewBox="0 0 22 26"
  >
    <Path
      fill="#494949"
      d="M11.604 12.624H5.26a5.262 5.262 0 00-5.255 5.492c.071 1.651 1.499 2.925 3.151 2.925h10.552c1.652 0 3.08-1.274 3.151-2.925a5.262 5.262 0 00-5.255-5.492z"
    ></Path>
    <Path
      fill="#fff"
      d="M14.87 25.466a6.02 6.02 0 100-12.04 6.02 6.02 0 000 12.04z"
    ></Path>
    <Path
      fill="#727272"
      d="M14.87 13.96a5.487 5.487 0 110 10.972 5.487 5.487 0 010-10.973zm0-1.068a6.533 6.533 0 00-4.635 1.92 6.525 6.525 0 00-1.92 4.634 6.536 6.536 0 001.92 4.635A6.526 6.526 0 0014.87 26a6.536 6.536 0 004.635-1.92 6.528 6.528 0 001.919-4.634 6.534 6.534 0 00-1.92-4.635 6.527 6.527 0 00-4.634-1.92z"
    ></Path>
    <Path
      fill="#494949"
      d="M8.433 10.52a5.26 5.26 0 100-10.52 5.26 5.26 0 000 10.52z"
    ></Path>
    <Path
      fill="#727272"
      d="M19.078 19.446c0 .578-.473 1.052-1.052 1.052h-2.104v2.104c0 .579-.473 1.052-1.052 1.052a1.055 1.055 0 01-1.052-1.052v-2.104h-2.104a1.055 1.055 0 01-1.052-1.052c0-.579.473-1.052 1.052-1.052h2.104V16.29c0-.579.474-1.052 1.053-1.052.578 0 1.051.473 1.051 1.052v2.104h2.104c.58 0 1.052.473 1.052 1.052z"
    ></Path>
  </Svg>
  )
}

export default AddUser
