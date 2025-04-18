import React from "react"
import { Button, Result } from "antd"
import { Link, useNavigate } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"

const ComingSoon: React.FC = () => {
  const navigate = useNavigate()

  return (
    <Result
      title={
        <h1 style={{ color: "#004c91", fontWeight: 400 }}>Coming Soon!</h1>
      }
      status="info"
      icon={false}
      extra={
        <Button
          onClick={() => navigate(-1)}
          style={{ border: "none", color: "#ffffff" }}
          className="wdz-grey-bg-color"
        >
          Back
        </Button>
      }
    />
  )
}

export default ComingSoon
