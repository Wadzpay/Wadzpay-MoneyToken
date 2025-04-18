import React, { useEffect, useState, useContext } from "react"
import { Form } from "react-bootstrap"
import { UserContext } from "src/context/User"
import { IssuanceContext } from "src/context/Merchant"

interface Props {
  updateIssuanceDetails: (data?: object) => void
}

const DefaultCurrency: React.FC<Props> = ({ updateIssuanceDetails }: Props) => {
  const { issuanceDetails } = useContext(IssuanceContext)
  const { setFiatAsset } = useContext(UserContext)
  const defaultCurrency = () => {
    if (localStorage.getItem("default-currency") !== null) {
      const currency = JSON.parse(
        localStorage.getItem("default-currency") || ""
      )
      return currency
    }
    return "SART"
  }
  const [currenctCurrency, setCurrentCurrency] = useState(defaultCurrency)

  const selectCurrency = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const currency: string = event.currentTarget.value
    localStorage.setItem("default-currency", JSON.stringify(currency))
    setFiatAsset(JSON.parse(localStorage.getItem("default-currency") || ""))
    setCurrentCurrency(currency)
    updateIssuanceDetails({
      defaultCurrency: currency,
      defaultTimeZone: issuanceDetails?.timeZone
    })
  }

  return (
    <>
      <div className="table-responsive" style={{ overflowX: "visible" }}>
        <div className="col-lg-3 col-sm-12 mt-4 ml-2">
          <Form.Group controlId="custom-select">
            <Form.Label>Default Digital Currency</Form.Label>
            <Form.Select
              as="select"
              value={currenctCurrency}
              onChange={(evt) => selectCurrency(evt as any)}
            >
              {[
                issuanceDetails?.defaultCurrency === "SART"
                  ? /*issuanceDetails?.defaultCurrency.replace("T", "*")*/ "xQAR"
                  : issuanceDetails?.defaultCurrency
              ].map((option) => (
                <option key={option}>{option}</option>
              ))}
            </Form.Select>
          </Form.Group>
        </div>
      </div>
    </>
  )
}

export default DefaultCurrency
