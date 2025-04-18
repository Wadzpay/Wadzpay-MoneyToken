import * as React from "react"
import Svg, { SvgProps, Path } from "react-native-svg"

function BellActive(props: SvgProps) {
  return (
    <Svg width={24} height={24} viewBox="0 0 24 24" fill="none" {...props}>
      <Path
        fillRule="evenodd"
        clipRule="evenodd"
        d="M21.962 16.166c-.12-.768-.608-1.442-1.325-1.83-.4-.223-.648-.636-.78-1.3-.14-.703-.215-1.32-.302-2.034v-.005a54.118 54.118 0 00-.462-3.163C18.46 4.236 15.932 2 12.5 2S6.541 4.236 5.906 7.835a54.054 54.054 0 00-.462 3.163v.005c-.087.714-.162 1.331-.302 2.034-.132.664-.38 1.077-.78 1.299-.716.388-1.204 1.063-1.324 1.83-.114.741.12 1.493.645 2.06a2.847 2.847 0 002.085.892H8.69c.25 1.096 1.027 2.023 2.104 2.514a4.14 4.14 0 003.412 0c1.077-.492 1.854-1.419 2.104-2.514h2.922c.8 0 1.561-.326 2.085-.892a2.472 2.472 0 00.645-2.06zM12.5 20.68c-1.061.005-2.008-.625-2.354-1.563h4.708c-.346.938-1.293 1.568-2.354 1.563zm6.732-2.882c.39 0 .762-.159 1.018-.434a1.2 1.2 0 00.316-1.015c-.06-.371-.3-.696-.648-.881-.764-.425-1.238-1.142-1.446-2.192-.15-.75-.228-1.39-.318-2.13v-.007c-.1-.814-.212-1.737-.45-3.09-.524-2.963-2.47-4.732-5.204-4.732-2.735 0-4.68 1.769-5.204 4.731-.238 1.353-.35 2.276-.45 3.09v.008l-.008.056c-.088.717-.164 1.343-.31 2.074-.209 1.05-.682 1.767-1.446 2.192-.348.185-.587.51-.648.88-.058.363.056.733.312 1.011.257.279.63.439 1.022.44h13.464z"
        fill={props.color}
      />
      <Path
        d="M20.637 14.335l-.121.219h.002l.12-.219zm1.325 1.83l.044.247.24-.043-.037-.242-.247.039zm-2.105-3.129l.245-.048-.245.048zm-.302-2.034l.248-.03v-.002l-.248.031zm0 0l-.248.03.248-.03zm0-.005l-.249.03.248-.03zm-.462-3.163l.247-.043-.247.043zm-13.187.001l-.247-.043.247.043zm-.462 3.163l.249.03-.249-.03zm0 .005l.248.03-.248-.03zm-.302 2.034l-.245-.048.245.048zm-.78 1.299l-.171-.183.292.402-.121-.22zm0 0l.172.182-.29-.403.119.22zm-1.324 1.83l-.247-.039.247.039zm.645 2.06l-.184.17.184-.17zm2.085.892v.25-.25zm2.922 0l.243-.056-.044-.194h-.2v.25zm2.104 2.514l-.104.227.104-.227zm3.412 0l.104.227-.104-.227zm2.104-2.514v-.25h-.2l-.044.194.244.056zm2.922 0v.25-.25zm2.085-.892l.183.17-.183-.17zm.645-2.06l-.044-.246-.24.043.037.24.247-.037zm-11.816 2.952v-.25h-.358l.124.337.234-.087zm2.354 1.563l.001-.25h-.002v.25zm2.354-1.563l.234.087.124-.337h-.358v.25zm5.396-1.753l.184.17.345-.373-.506-.046-.023.248zm-1.018.434v.25-.25zm1.018-.435l-.183-.17-.347.372.507.047.023-.249zm.316-1.014l.246-.04-.247.04zm-.648-.881l-.121.218.003.002.118-.22zm-1.446-2.192l.245-.049-.245.049zm-.318-2.13l-.248.03.248-.03zm0-.007l.248-.03-.248.03zm-.45-3.09l.246-.044-.247.043zm-10.408 0l-.246-.044.246.043zm-.45 3.09l-.248-.03.248.03zm0 .007l.248.03-.249-.03zm-.008.056l-.248-.03.248.03zm-.31 2.074l-.245-.05v.001l.245.049zm-1.446 2.192l.118.22.003-.002-.121-.218zm-.648.88l-.247-.04.247.04zm.312 1.011l-.184.17.184-.17zm1.022.44v.25-.25zm14.75-3.245c.653.354 1.09.963 1.197 1.65l.494-.078c-.132-.848-.67-1.588-1.453-2.012l-.238.44zm-.906-1.47c.139.701.414 1.196.904 1.469l.243-.438c-.31-.172-.532-.501-.657-1.128l-.49.097zm-.305-2.053c.087.713.163 1.34.305 2.053l.49-.097a25.936 25.936 0 01-.299-2.017l-.496.06zm0 0l.496-.062-.496.063zm0-.004v.004l.496-.06v-.005l-.497.06zm-.46-3.15c.244 1.383.358 2.32.46 3.15l.496-.06a54.384 54.384 0 00-.463-3.177l-.493.087zM12.5 2.25c1.66 0 3.089.54 4.18 1.505 1.092.966 1.858 2.371 2.167 4.123l.493-.087c-.326-1.847-1.14-3.359-2.329-4.41C15.822 2.328 14.272 1.75 12.5 1.75v.5zM6.152 7.879c.31-1.752 1.076-3.158 2.168-4.124C9.41 2.79 10.84 2.25 12.5 2.25v-.5c-1.772 0-3.322.578-4.512 1.63C6.8 4.433 5.986 5.946 5.66 7.793l.493.087zm-.46 3.15c.102-.83.216-1.768.46-3.15l-.493-.087a54.297 54.297 0 00-.463 3.176l.497.06zm0 .004v-.005l-.496-.06v.005l.496.06zm-.305 2.053c.142-.714.218-1.34.305-2.053l-.496-.06a25.976 25.976 0 01-.3 2.016l.491.097zm-.904 1.469c.491-.273.765-.768.904-1.469l-.49-.097c-.125.627-.346.956-.656 1.128l.242.438zm-.291-.402H4.19l.342.365-.341-.366zm-.907 2.051c.107-.686.544-1.295 1.197-1.65l-.238-.439c-.782.424-1.32 1.163-1.453 2.012l.494.077zm.581 1.852a2.223 2.223 0 01-.581-1.852l-.494-.076a2.722 2.722 0 00.708 2.267l.367-.339zm1.903.812c-.735 0-1.428-.3-1.903-.812l-.367.34c.574.619 1.402.971 2.269.972v-.5zm2.92 0H5.77v.5h2.92v-.5zm2.209 2.536c-1.011-.46-1.733-1.327-1.965-2.342l-.487.112c.269 1.176 1.1 2.164 2.244 2.685l.208-.455zm3.204 0a3.89 3.89 0 01-3.205 0l-.207.455a4.39 4.39 0 003.62 0l-.208-.455zm1.964-2.342c-.232 1.015-.953 1.881-1.964 2.342l.208.455c1.143-.521 1.975-1.51 2.244-2.685l-.488-.112zm3.166-.194H16.31v.5h2.922v-.5zm1.902-.812a2.597 2.597 0 01-1.903.812v.5c.868 0 1.696-.353 2.27-.972l-.367-.34zm.58-1.852a2.223 2.223 0 01-.58 1.852l.367.34a2.722 2.722 0 00.708-2.268l-.494.076zm.204-.284l.088.492-.088-.492zM9.912 19.204c.385 1.045 1.43 1.732 2.589 1.727l-.002-.5c-.965.004-1.811-.567-2.118-1.4l-.47.174zm4.942-.336h-4.708v.5h4.708v-.5zm-2.355 2.063c1.158.005 2.204-.682 2.59-1.727l-.47-.172c-.307.832-1.153 1.403-2.118 1.4l-.002.5zm7.568-3.737a1.144 1.144 0 01-.836.355l.001.5a1.644 1.644 0 001.202-.514l-.367-.34zm.16.42l.046-.498-.046.497zm.092-1.224a.951.951 0 01-.252.804l.366.34a1.45 1.45 0 00.38-1.224l-.494.08zm-.519-.7a.982.982 0 01.519.7l.493-.08a1.482 1.482 0 00-.776-1.062l-.236.441zm-1.574-2.364c.22 1.104.728 1.893 1.57 2.361l.243-.437c-.686-.38-1.124-1.026-1.322-2.022l-.49.098zm-.32-2.15c.09.74.169 1.39.32 2.15l.49-.098c-.146-.74-.223-1.37-.314-2.112l-.496.06zm0-.005v.006l.497-.06-.001-.007-.497.06zm-.449-3.078c.238 1.346.35 2.264.448 3.078l.497-.06a53.04 53.04 0 00-.452-3.105l-.493.087zM12.5 3.568c1.31 0 2.42.423 3.263 1.19.845.768 1.44 1.897 1.694 3.335l.493-.087c-.27-1.525-.909-2.762-1.85-3.618-.945-.858-2.176-1.32-3.6-1.32v.5zM7.542 8.093c.254-1.438.85-2.567 1.695-3.335.844-.767 1.952-1.19 3.263-1.19v-.5c-1.424 0-2.656.462-3.6 1.32-.942.856-1.58 2.093-1.85 3.618l.492.087zm-.448 3.077c.1-.813.211-1.731.448-3.077l-.492-.087c-.24 1.36-.353 2.288-.452 3.103l.496.06zm0 .007v-.007l-.496-.06v.006l.496.06zm-.007.057l.007-.057-.497-.06-.007.056.497.061zm-.314 2.092c.148-.742.226-1.376.314-2.092l-.497-.06a26.469 26.469 0 01-.307 2.054l.49.098zm-1.57 2.361c.843-.468 1.35-1.257 1.57-2.361l-.49-.098c-.198.996-.637 1.641-1.323 2.022l.243.437zm-.522.703a.982.982 0 01.519-.7l-.236-.442c-.412.22-.703.61-.777 1.062l.494.08zm.249.8a.956.956 0 01-.249-.8l-.494-.08c-.07.44.069.887.375 1.22l.368-.34zm.839.36c-.326-.001-.632-.134-.84-.36l-.367.34c.306.332.747.518 1.206.52v-.5zm13.463 0H5.768v.5h13.464v-.5z"
        fill={props.color}
      />
      <Path
        d="M18.072 9.25c2.297 0 4.197-1.825 4.197-4.125S20.37 1 18.072 1c-2.296 0-4.197 1.825-4.197 4.125s1.9 4.125 4.197 4.125z"
        fill="#FFBC04"
        stroke="#fff"
        strokeWidth={2}
      />
    </Svg>
  )
}

export default BellActive
