import React, { ReactNode } from "react"

const Card = ({ children }: { children: ReactNode }) => {
  return <div className="wdz-card">{children}</div>
}

export default Card
