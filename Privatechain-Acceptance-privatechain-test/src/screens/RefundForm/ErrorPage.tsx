import React from "react"

import ErrorImg from "./error.gif"

import "./errorPage.scss"

export default function ErrorPage(props: any) {
  return (
    <div className="error-handle-main">
      <div className="error-text">{props?.errorMssg}</div>
      <div className="error-img-section">
        <img src={ErrorImg} alt="error-img" className="error-img" />
      </div>
    </div>
  )
}
