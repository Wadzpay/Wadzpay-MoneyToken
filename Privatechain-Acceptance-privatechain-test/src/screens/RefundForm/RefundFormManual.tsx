import React from "react"
import "./RefundFormStyle.scss"
import Button from "react-bootstrap/Button"
import Form from "react-bootstrap/Form"
import Row from "react-bootstrap/Row"
import Col from "react-bootstrap/Col"
import { RouteType } from "src/constants/routeTypes"
import { useNavigate } from "react-router-dom"
import { useState } from "react"
import TermsAndConditionsPopupModal from "src/components/ui/PopUps/TermsAndConditionsPopupModal"
import { WALLET_MAX_LENGTH } from "src/constants/Defaults"

export default function RefundFormManual(): JSX.Element {
  const navigate = useNavigate()
  const [showTermsAndConditionsForm, setShowTermsAndConditionsForm] =
    useState(false)

  return (
    <Col
      className="refund-main col-md-8 mb-4"
      md={{ offset: 2 }}
      style={{ marginBottom: "20px" }}
    >
      <h2 className="refund-header">
        Customer&apos;s Verification form for Refund
      </h2>
      <Form autoComplete="off">
        <Form.Group className="mb-3">
          <Form.Label>Transaction reference</Form.Label>
          <Form.Control type="text" aria-autocomplete='both' aria-haspopup="false" /* autoComplete="true" */ placeholder="Transaction reference" />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Customer Name</Form.Label>
          <Form.Control type="text"  aria-autocomplete='both' aria-haspopup="false" /* autoComplete="true" */  placeholder="Customer Name" />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Mobile number (optional)</Form.Label>
          <Form.Control type="number" aria-autocomplete='both' aria-haspopup="false"/* autoComplete="true" */  placeholder="Mobile number (optional)" />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Email Address (optional)</Form.Label>
          <Form.Control type="email" aria-autocomplete='both' aria-haspopup="false"/* autoComplete="true" */ placeholder="Email Address (optional)" />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Digital currency amount</Form.Label>
          <Form.Control type="number" aria-autocomplete='both' aria-haspopup="false"/* autoComplete="true" */ placeholder="Digital currency amount" />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Digital currency name</Form.Label>
          <Form.Control type="text" aria-autocomplete='both' aria-haspopup="false"/* autoComplete="true" */ placeholder="Digital currency name" />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Refund Amount in AED</Form.Label>
          <Form.Control type="number" aria-autocomplete='both' aria-haspopup="false" /* autoComplete="true" */ placeholder="Refund Amount in AED" />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Refund Amount in USDC</Form.Label>
          <Form.Control type="number" aria-autocomplete='both' aria-haspopup="false"/* autoComplete="true" */ placeholder="Refund Amount in USDC" />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Reason for Refund</Form.Label>
          <Form.Control type="text" aria-autocomplete='both' aria-haspopup="false"/* autoComplete="true" */ placeholder="Reason for Refund" />
          <Form.Text className="text-muted">to be filled by customer</Form.Text>
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>USDC wallet address</Form.Label>
          <Form.Control
            type="text"
            aria-autocomplete='both' aria-haspopup="false"
            /* autoComplete="true" */ maxLength={WALLET_MAX_LENGTH}
            placeholder="USDC wallet address"
          />
          <Form.Text className="text-muted">to be filled by customer</Form.Text>
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>Reconfirm USDC wallet address</Form.Label>
          <Form.Control
            type="text"
            aria-autocomplete='both' aria-haspopup="false"
/*             autoComplete="true" 
 */            maxLength={WALLET_MAX_LENGTH}
            placeholder="Reconfirm USDC wallet address"
          />
          <Form.Text className="text-muted">to be filled by customer</Form.Text>
        </Form.Group>
        <h4>
          Declaration signed by the customer to the effect that (Customer to
          accept and sign).
        </h4>
        <ul className="list-details">
          <li>Refund will be in USDT, credited into a USDT wallet.</li>
          <li>Gas fee will be borne by the customer.</li>
          <li>
            Upon verification, refund value will be processed and will be
            credited in customer’s account within 7days.
          </li>
          <li>
            Refund amount would be credited based on conversion rate of the
            original crypto currency at the time of execution of refund
            transaction with the deduction of gas fee.
          </li>
        </ul>
        {/* <Form.Group as={Row} className="mb-3">
          <Form.Label column sm={6}>
            Customer&#39;s Signature
          </Form.Label>
          <Col sm={6}>
            <Form.Control type="text" className="signatureInput" />
          </Col>
        </Form.Group> */}
        <Form.Group className="mb-3 mt-4" controlId="formBasicCheckbox">
          <div style={{ display: "flex" }}>
            <Form.Check
              type="checkbox"
              label={`I accept all of the above points and the `}
            />
            <button
              type="button"
              className="buttonToLink"
              onClick={() =>
                setShowTermsAndConditionsForm(!showTermsAndConditionsForm)
              }
            >
              Terms and Conditions
            </button>
          </div>
        </Form.Group>
        <div style={{ textAlign: "center" }}>
          <button
            type="submit"
            className="wdz-btn-primary wdz-btn-md"
            style={{ marginRight: "20px" }}
          >
            Submit
          </button>
          <button
            className="wdz-btn-grey wdz-btn-md ml-4"
            onClick={() => navigate(RouteType.REFUND_DISPUTE)}
          >
            Cancel
          </button>
        </div>
        {/* <div>
          <Button
            className="btn wdz-btn-primary wdz-btn-md"
            onClick={() => navigate(RouteType.REFUND_DISPUTE)}
          >
            Back
          </Button>
        </div> */}
        {showTermsAndConditionsForm ? (
          <TermsAndConditionsPopupModal
            showhide={() => setShowTermsAndConditionsForm(false)}
          />
        ) : null}
      </Form>
    </Col>
  )
}
