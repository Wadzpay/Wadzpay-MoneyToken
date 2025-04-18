import React, { useState } from "react"
import "../RefundForm/RefundFormStyle.scss"
import Form from "react-bootstrap/Form"
import DatePicker from "react-datepicker"
import "react-datepicker/dist/react-datepicker.css"

export default function AcknowledgmentReciept(): JSX.Element {
  const [startDate, setStartDate] = useState(new Date())
  return (
    <div className="refund-main">
      <h2 className="refund-header">Acknowledgment Slip</h2>
      <Form>
        <Form.Group className="mb-3" controlId="formBasicEmail">
          <Form.Label>Acknowledgment Number</Form.Label>
          <Form.Control type="number" placeholder="Acknowledgment Number" />
        </Form.Group>
        <Form.Group className="mb-3" controlId="formBasicEmail">
          <Form.Label>Date</Form.Label>
          <DatePicker
            selected={startDate}
            onChange={(date: Date) => setStartDate(date)}
          />
        </Form.Group>
        <Form.Group className="mb-3" controlId="formBasicEmail">
          We acknowledge your refund request for transaction number.
          <input className="acknowledgment-inputs" />
          for amount
          <input className="acknowledgment-inputs" />
        </Form.Group>
        <div>
          You will receive your refund to your mentioned wallet in X days if
          approved.In case of any queries please call on our call centre by
          mentioning your Ack No.
        </div>
      </Form>
    </div>
  )
}
