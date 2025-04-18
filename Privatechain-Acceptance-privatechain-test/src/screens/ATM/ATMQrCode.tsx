import React, { useEffect } from "react"
import { Link, useNavigate } from "react-router-dom"
import { t } from "i18next"
import { RouteType } from "src/constants/routeTypes"
import { useLocation } from "react-router-dom"
import QRCode from "react-qr-code"
import { useWalletWithdraw } from "src/api/user"
import Button from "react-bootstrap/esm/Button"

export default function ATMQrCode(): JSX.Element {
  const location = useLocation()
  const navigate = useNavigate()
  const amount = location.state.amount
  const { mutate: walletWithDrawApi, error, isSuccess } = useWalletWithdraw()

  const formSubmit = () => {
    walletWithDrawApi({
      walletAddress: "0x07C1F3849be8594717DcB5e2078BfAf27fa933f6",
      amount: amount,
      asset: "USDT"
    })
  }

  useEffect(() => {
    if (isSuccess) {
      navigate(RouteType.ATM_DISBURSEMENT)
    }
  }, [isSuccess])

  return (
    <div className="container" style={{ width: "520px" }}>
      <div className="atm">
        <h3>Please Scan QR Code</h3>
      </div>
      <div>Use USDT wallet app to authorize QR Code for withdrawl</div>
      {error && (
        <div className="alert alert-danger" role="alert">
          {error.message}
        </div>
      )}
      <div className="col-md-12 mt-4">
        <div className="qrCodeContainer">
          <QRCode
            value={amount}
            // value="bitcoin:18G8pwXnXEn4YPDxBp7v5xNEkMvZwjjyCr?amount=0.00800519&message=Transaction%2520ID%253A%2520a1630887-59ca-4dc6-9eea-f881aa6ecd8f"
            size={200}
          />
        </div>
      </div>

      <div
        className="row justify-content-center"
        style={{ fontSize: "30px", fontWeight: 600, paddingTop: "20px" }}
      >
        <Button className="btn wdz-btn-primary" onClick={() => formSubmit()}>
          {t("Scan")}
        </Button>
      </div>
    </div>
  )
}
