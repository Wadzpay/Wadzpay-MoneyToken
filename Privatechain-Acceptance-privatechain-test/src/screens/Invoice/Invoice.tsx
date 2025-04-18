import React, { useContext, useState } from "react"
import { Link, useLocation } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import Modal from "react-bootstrap/Modal"
import { useTranslation } from "react-i18next"
import { MerchantContext } from "src/context/Merchant"
import Card from "src/helpers/Card"
import QRCode from "react-qr-code"

import InvoiceTransactions from "./InvoiceTransactions"

function Invoice(): JSX.Element {
  const location = useLocation()
  const { t } = useTranslation()
  const [showModal, setShowModal] = useState(false)
  const { merchantDetails } = useContext(MerchantContext)

  return (
    // <Card>
    //   <div className="d-flex justify-content-between align-items-center">
    //     <h3>{t("Invoice")}</h3>
    //     <button
    //       className="wdz-btn-primary wdz-btn-xl"
    //       onClick={() => setShowModal(true)}
    //     >
    //       <img src="/images/Invoice.svg" style={{ marginRight: "10px" }} />
    //       Generate Invoice
    //     </button>
    //   </div>
    //   {/* <div>
    //     <InvoiceTransactions />
    //   </div> */}
    //   <Modal show={showModal} centered>
    //     <Modal.Body>
    //       <div className="Invoice-modal-head d-flex justify-content-between align-items-center">
    //         <h4 className="wdz-font-color">{t("Generate Invoice")}</h4>
    //         <img src="/images/Cross.svg" onClick={() => setShowModal(false)} />
    //       </div>
    //       <div style={{ background: "white", padding: "16px" }}>
    //         <QRCode value="0x0d4d3979ce026628317a55fa44187ba0d8ea4ee9" />
    //       </div>
    //     </Modal.Body>
    //     <Modal.Footer>
    //       <button
    //         type="submit"
    //         className="btn btn-primary wdz-btn-primary"
    //         data-testid="confirmButton"
    //         style={{ width: "100%", height: "50px", fontSize: "20px" }}
    //       >
    //         {t("Continue")}
    //       </button>
    //     </Modal.Footer>
    //   </Modal>
    // </Card>
    <div
      style={{
        height: "600px",
        display: "flex",
        justifyContent: "center",
        alignItems: "center"
      }}
    >
      <h4>Coming soon...</h4>
    </div>
  )
}

export default Invoice
