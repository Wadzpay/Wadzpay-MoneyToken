import React, { useContext } from "react"
import { Form } from "react-bootstrap"

const walletsCountPerPage: React.FC = () => {
  return (
    <>
      <div className="col-lg-2 col-lg-3 mt-4 ml-2">
        <Form.Group controlId="custom-select">
          <Form.Label>Wallet count per page</Form.Label>
          <Form.Select as="select" value={""} style={{ width: "50%" }}>
            {["10", "25", "50", "100"].map((option) => (
              <option key={option}>{option}</option>
            ))}
          </Form.Select>
        </Form.Group>
      </div>
    </>
  )
}

export default walletsCountPerPage
