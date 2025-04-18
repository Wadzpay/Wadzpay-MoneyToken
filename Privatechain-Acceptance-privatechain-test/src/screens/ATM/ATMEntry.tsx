import React, { useEffect, useState } from "react"
import { Link, Navigate, useNavigate } from "react-router-dom"
import { t } from "i18next"
import { RouteType } from "src/constants/routeTypes"
import Form from "react-bootstrap/esm/Form"
import Button from "react-bootstrap/esm/Button"
import { Modal } from "react-bootstrap"
import { useGetExchangeRate } from "src/api/onRamp"
import DOMPurify from "dompurify"
export default function ATMEntry(): JSX.Element {
  const [showModal, setShowModal] = useState(false)
  const navigate = useNavigate()
  const [amountVal, setAmountVal] = useState("")
  const { data: exchangeRatesData } = useGetExchangeRate("AED")
  const [digitalAsset, setDigitalAsset] = useState("")

  const sendToQr = () => {
    if (amountVal) {
      navigate(RouteType.ATM_ENTRY_QR_CODE, {
        state: { amount: amountVal }
      })
    }
  }

  useEffect(() => {
    if (amountVal && exchangeRatesData) {
      const value = exchangeRatesData["USDT"] * Number(amountVal)
      setDigitalAsset(value.toFixed(8))
    }
  }, [amountVal])

  return (
    <div className="container" style={{ width: "530px" }}>
      <div className="atm">
        <h3>Enter Withdrawl Amount from Wallet</h3>
      </div>
      <div>Please enter the amount you want to withdrawl</div>
      <div
        className="row justify-content-center"
        style={{ paddingTop: "20px" }}
      >
        <Form autoComplete="off" >
          <Form.Label style={{ alignItems: "left" }}>Enter Amount</Form.Label>
          <div className="inputbox-wrap">
            <Form.Control
              type="text"
              placeholder="Enter Amount"
              aria-autocomplete='both' aria-haspopup="false"
/*               autoComplete="true" 
 *//*               autoComplete="off"
 */              onChange={(e) => 
                { const sanitizedInput = DOMPurify.sanitize(e.target.value)
                setAmountVal(sanitizedInput)}}
              value={amountVal}
              name="amount"
            />
            <Button name="amount" disabled>
              USD
            </Button>
          </div>
        </Form>
      </div>
      <div
        className="row justify-content-center"
        style={{ fontSize: "30px", fontWeight: 600, paddingTop: "20px" }}
      >
        <Button
          className="btn wdz-btn-primary"
          onClick={() => (amountVal ? setShowModal(true) : "")}
        >
          {t("Submit")}
        </Button>
        <Modal show={showModal}>
          <Modal.Body>
            {t(
              `Entered Digital Amount is: ${digitalAsset},Are you sure you want to make this transaction?`
            )}
          </Modal.Body>
          <Modal.Footer>
            <button
              type="submit"
              className="btn btn-primary"
              onClick={() => sendToQr()}
              data-testid="confirmButton"
            >
              {t("Confirm")}
            </button>
            <button
              className="btn btn-secondary"
              style={{ marginLeft: "2%" }}
              onClick={() => setShowModal(false)}
              data-testid="cancelConfirmButton"
            >
              {t("Cancel")}
            </button>
          </Modal.Footer>
        </Modal>
      </div>
    </div>
  )
}
