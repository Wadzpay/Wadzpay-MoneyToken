import React from "react";
import { Modal } from "react-bootstrap";
const btnMinWidth = { width: "120px" };

type Props = {
  show?: boolean;
  setShow: (value?: boolean) => void;
};

const RefundButtonStatus: React.FC<Props> = ({ show, setShow }: Props) => {
  const handleClose = () => setShow(false);
  return (
    <>
      <Modal show={show} onHide={handleClose}>
        <Modal.Header closeButton>
          <Modal.Title>Refund button status info</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <table className="table transactions-table">
            <thead>
              <tr>
                <th>Button</th>
                <th>Description</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>
                  <button
                    className="btn-sm btn btn-success maXwidth-200"
                    style={btnMinWidth}
                  >
                    {"Initiate"}
                  </button>
                </td>
                <td>{"Yet to Start/Not Started"}</td>
              </tr>

              <tr>
                <td>
                  <button
                    className="btn-sm btn btn-secondary refundbtn btn-sm"
                    disabled
                    style={btnMinWidth}
                  >
                    {"Refunded"}
                  </button>
                </td>
                <td>{"Refund Completed"}</td>
              </tr>
              <tr>
                <td>
                  <button
                    className="btn-sm btn btn-success btn-sm"
                    style={btnMinWidth}
                  >
                    {"Reinitiate"}
                    <br />
                    {"Failed"}
                  </button>
                </td>
                <td>{"Reinitiating Refund failed from Blockchain"}</td>
              </tr>
            </tbody>
          </table>
        </Modal.Body>
      </Modal>
    </>
  );
};

export default RefundButtonStatus;
