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
  const [loading, setLoading] = useState(true)
  const [captchaToken, setCaptchaToken] = useState<string | null>(null)
  const handleCaptchaChange = (token: string | null) => {
    setCaptchaToken(token)
  }

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
  useEffect(() => {
    if (loading) setLoading(false)
  }, [loading])

  const generateCaptcha = () => {
    const characters =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    let captcha = ""
    for (let i = 0; i < 6; i++) {
      captcha += characters.charAt(
          Math.floor(Math.random() * characters.length)
      )
    }
    return captcha
  }
  const [captcha, setCaptcha] = useState<string>(generateCaptcha())
  const [userInput, setUserInput] = useState<string>("")
  const [captchaValid, setCaptchaValid] = useState<boolean>(false)

  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setUserInput(event.target.value)
  }
  const [isCaptchaAvailable, setIsCaptchaAvailable] = useState<boolean>(true)
  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    console.log("isCaptchaAvailable = ", isCaptchaAvailable)
    if (isCaptchaAvailable) {
      if (userInput === captcha) {
        setCaptchaValid(true)
        onGenerateKey()
        console.log("CAPTCHA validation passed")
        // Proceed with form submission
        // setMessage("CAPTCHA validation passed")
        setIsCaptchaAvailable(false)
      } else {
        setCaptchaValid(false)
        console.log("CAPTCHA validation failed")
        // Display error message to the user
        setMessage("CAPTCHA validation failed")
        setIsCaptchaAvailable(true)
      }
      // Generate a new CAPTCHA challenge
      setCaptcha(generateCaptcha())
      setUserInput("")
    } else {
      setMessage("Please Try After Sometime...!")
    }
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
              <img
                  src={`https://dummyimage.com/120x40/000/fff&text=${captcha}`}
                  alt="CAPTCHA"
              />
              {captchaValid && !loading ? null : (
                  <p style={{ color: "black" }}>Please Validate Captcha.</p>
              )}
              <form autoComplete="off" onSubmit={handleSubmit}>
                <div>
                  {" "}
                  <input
                      type="text" aria-autocomplete='both' aria-haspopup="false"
                      value={userInput}
                     // autoComplete="true" 
                      onChange={handleInputChange}
                  />
                </div>

                <div className="nav-link ps-0">
                  <button
                      data-testid="generateAPI"
                      className="btn btn-secondary wdz-btn-primary mx-1"
                      type="submit"
                      disabled={isLoading}
                  >
                    {t(`Generate${responseData ? t(" another") : ""} API key`)}
                  </button>

                  <Link
                      to={RouteType.SETTINGS}
                      data-testid="backButton"
                      className="btn btn-secondary wdz-btn-grey wdz-btn-md mx-1"
                      role="button"
                  >
                    {t("Back")}
                  </Link>
                </div>
              </form>
            </div>
          </li>
          <li className="nav-item"></li>
        </ul>
      </Card>
  )
}

export default AdminCreateAPIKey
