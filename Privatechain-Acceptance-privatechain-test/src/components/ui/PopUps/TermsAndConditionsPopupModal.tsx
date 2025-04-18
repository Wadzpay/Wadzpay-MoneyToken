import React, { useState, useEffect, useContext } from "react"
import Modal from "react-bootstrap/Modal"
import Container from "react-bootstrap/Container"
import { useMerchantDetails } from "src/api/user"
import { MerchantContext } from "src/context/Merchant"

function TermsAndConditionsPopupModal(props: any) {
  const [showTermsAndConditions, setShowTermsAndConditions] = useState(true)
  const handleClose = () => setShowTermsAndConditions(false)
  const { merchantDetails } = useContext(MerchantContext)
  const [termsAndCondtitonPara1, setTermsAndConditionPara1] = useState()
  const [termsAndCondtitonPara2, setTermsAndConditionPara2] = useState()
  const [termsAndCondtitonPara3, setTermsAndConditionPara3] = useState()
  const [termsAndCondtitonPara4, setTermsAndConditionPara4] = useState()
  const [tnc, setTnc] = useState({
    heading: "Terms and Conditions",
    para1:
      "Customer must ensure that this purchase meets any rules and regulations, including but not limited to customs, tax and security, both during the journey and at the country of destination, and Dubai Duty Free does not bear any responsibility in this regard.",
    para2:
      "Return/exchange within 6 months from the date of purchase with original packaging, purchase receipt, and in unused condition.",
    para3:
      "Refund claims for payments made using digital currency will be credited in USDT through a USDT wallet. Upon completion of verification form sent to the customer via Email or SMS.",
    para4: "",
    footer: "Terms and Conditions"
  })

  useEffect(() => {
    if (tnc) {
      const termsJson = JSON.parse(JSON.stringify(tnc))
      setTermsAndConditionPara1(termsJson["para1"])
      setTermsAndConditionPara2(termsJson["para2"])
      setTermsAndConditionPara3(termsJson["para3"])
      setTermsAndConditionPara4(termsJson["para4"])
    }
  }, [tnc])

  return (
    <>
      <Modal
        show={showTermsAndConditions}
        onHide={handleClose}
        size="lg"
        centered
        backdrop="static"
      >
        <Modal.Header style={{ justifyContent: "center" }}>
          <h2 className="refund-header" style={{ justifyContent: "center" }}>
            Terms and Conditions
          </h2>
        </Modal.Header>
        <Modal.Body
          style={{ height: "450px" }}
          className="show-grid trxbalances"
        >
          <Container className="textareaTnc">
            {tnc && (
              <ol>
                <li key={1}>{termsAndCondtitonPara1}</li>
                <li key={2}>{termsAndCondtitonPara2}</li>
                <li key={3}>{termsAndCondtitonPara3}</li>
                {termsAndCondtitonPara4 && (
                  <li key={4}>{termsAndCondtitonPara4}</li>
                )}
              </ol>
            )}
          </Container>
        </Modal.Body>
        <Modal.Footer style={{ justifyContent: "center" }}>
          <div style={{ textAlign: "center" }}>
            <button
              className="wdz-btn-grey wdz-btn-md ml-4"
              onClick={() => props.showhide()}
            >
              Close
            </button>
          </div>
        </Modal.Footer>
      </Modal>
    </>
  )
}

export default TermsAndConditionsPopupModal
