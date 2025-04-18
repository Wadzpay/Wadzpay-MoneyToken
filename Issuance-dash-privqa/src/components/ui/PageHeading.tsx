import React from "react"
import { useNavigate, Link } from "react-router-dom"
import { Button } from "antd"
import { useSelector } from "react-redux"
import { ThreeDots } from "react-loader-spinner"
import { ROOTSTATE } from "src/utils/modules"

type Props = {
  title: string
  buttonTitle?: undefined | string
  buttonDisabled?: boolean
  linkData?: undefined | any
  backIcon?: boolean
  setMappedLanguage?: () => void
  loading?: any
}

const PageHeading: React.FC<Props> = ({
  title,
  buttonTitle,
  buttonDisabled,
  linkData,
  backIcon,
  setMappedLanguage,
  loading
}: Props) => {
  const navigate = useNavigate()

  // Selector
  const buttonColor = useSelector(
    (store: ROOTSTATE) => store.appConfig.buttonColor
  )

  return (
    <div className="d-sm-flex align-items-center justify-content-between">
      <h5
        className="h5 mt-3"
        style={{ letterSpacing: "1px", fontSize: "21px" }}
      >
        {backIcon !== undefined ? (
          <img
            style={{ cursor: "pointer" }}
            onClick={() => navigate(-1)}
            src="/images/back-icon.svg"
          />
        ) : null}
        &nbsp;<b>{title}</b>
      </h5>
      {buttonTitle !== undefined ? (
        <Button
          style={{
            background: buttonDisabled ? "rgb(255 217 128)" : buttonColor,
            color: "#131313",
            border: "none",
            fontWeight: 500
          }}
          onClick={setMappedLanguage}
          disabled={buttonDisabled}
        >
          <span style={{ display: "flex" }}>
            {buttonTitle}
            &nbsp;
            {loading}
          </span>
        </Button>
      ) : null}
      {linkData !== undefined ? (
        <Link to={`${linkData.url}`}>
          <Button
            style={{
              background: buttonColor,
              color: "#131313",
              border: "none",
              fontWeight: 500
            }}
          >
            {linkData.label}
          </Button>
        </Link>
      ) : null}
    </div>
  )
}

export default PageHeading
