import React, { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { useGenerateAPIKey } from "src/api/admin"
import { useTranslation } from "react-i18next"
import Card from "src/helpers/Card"

type Data = { username: string; password: string; basicKey: string }

function AdminCreateAPIKey(): JSX.Element {
  const [message, setMessage] = useState<string>()
  const [type, setType] = useState<string>()
  const { t } = useTranslation()
  const {
    data,
    mutate: generateKey,
    error,
    isSuccess,
    isLoading
  } = useGenerateAPIKey()
  const [responseData, setResponseData] = useState<Data>()

  useEffect(() => {
    if (isSuccess) {
      setResponseData(data as Data)
      setMessage("API key generated")
      setType("success")
    }
  }, [isSuccess])

  useEffect(() => {
    if (error) {
      setMessage(t(error.message))
      setType("danger")
    }
  }, [error])

  const onGenerateKey = () => {
    setMessage("Generating API key...")
    setType("primary")
    generateKey({})
  }

  return (
    <Card>
      <h2>{t("Generate API Key")}</h2>
      <div className={`alert alert-${type}`}>{message}</div>
      {responseData && (
        <>
          <dl>
            <dt>{t("Username")}</dt>
            <dd>{responseData.username}</dd>
            <dt>{t("Password")}</dt>
            <dd>{responseData.password}</dd>
            <dt>{t("BasicKey")}</dt>
            <dd>{responseData.basicKey}</dd>
          </dl>
        </>
      )}
      <ul className="nav">
        <li className="nav-item">
          <div className="nav-link ps-0">
            <button
              data-testid="generateAPI"
              className="btn btn-secondary wdz-btn-primary"
              onClick={() => onGenerateKey()}
              disabled={isLoading}
            >
              {t(`Generate${responseData ? t(" another") : ""} API key`)}
            </button>
          </div>
        </li>
        <li className="nav-item">
          <div className="nav-link ps-0">
            <Link
              to={RouteType.SETTINGS}
              data-testid="backButton"
              className="btn btn-secondary wdz-btn-grey wdz-btn-md"
              role="button"
            >
              {t("Back")}
            </Link>
          </div>
        </li>
      </ul>
    </Card>
  )
}

export default AdminCreateAPIKey
