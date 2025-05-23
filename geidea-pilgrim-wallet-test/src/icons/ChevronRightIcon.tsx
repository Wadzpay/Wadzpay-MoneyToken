import React from "react";
import Svg, { SvgProps, Path, G } from "react-native-svg"

function ChevronRightIcon(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <G data-name="Layer 2">
        <Path
        fill={props.color}
        stroke={props.color}
          d="M10.5 17a1 1 0 01-.71-.29 1 1 0 010-1.42L13.1 12 9.92 8.69a1 1 0 010-1.41 1 1 0 011.42 0l3.86 4a1 1 0 010 1.4l-4 4a1 1 0 01-.7.32z"
          data-name="chevron-right"
        ></Path>
      </G>
    </Svg>
  );
}

export default ChevronRightIcon;