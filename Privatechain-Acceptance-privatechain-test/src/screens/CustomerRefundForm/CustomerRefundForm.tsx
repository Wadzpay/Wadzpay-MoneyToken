import React from "react"
import "../RefundForm/RefundFormStyle.scss"
import { Button, Dropdown } from "react-bootstrap"
import Form from "react-bootstrap/Form"

export default function CustomerRefundForm(): JSX.Element {
  return (
    <div className="refund-main">
      <h2 className="refund-header">Send Web Form to Customer for Refund</h2>
      <Form  autoComplete="off" >
        <Form.Group className="mb-3" controlId="formBasicEmail">
          <Form.Label>Refund Amount in AED</Form.Label>
          <Form.Control type="text" autoComplete="true"  placeholder="Refund Amount in AED" />
        </Form.Group>
        <Form.Group className="mb-3" controlId="formBasicEmail">
          <Form.Label> Refund Mode</Form.Label>
          <Dropdown>
            <Dropdown.Toggle variant="success" id="dropdown-basic">
              Refund Mode
            </Dropdown.Toggle>

            <Dropdown.Menu>
              <Dropdown.Item href="#/action-1">Action</Dropdown.Item>
              <Dropdown.Item href="#/action-2">Another action</Dropdown.Item>
              <Dropdown.Item href="#/action-3">Something else</Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>
        </Form.Group>

        <Form.Group className="mb-3" controlId="formBasicPassword">
          <Form.Label>Mobile number (optional)</Form.Label>
          <Form.Control type="number" autoComplete="true"  placeholder="Mobile number (optional)" />
        </Form.Group>

        <Form.Group className="mb-3" controlId="formBasicPassword">
          <Form.Label>Email Address (optional)</Form.Label>
          <Form.Control type="email" autoComplete="true"  placeholder="Email Address (optional)" />
        </Form.Group>
        <Button variant="primary" type="submit">
          Submit
        </Button>
        <Button variant="secondary" className="ml-4" type="submit">
          Cancel
        </Button>
      </Form>
    </div>
  )
}
