import React, { useState, useEffect } from "react"
import { useTranslation } from "react-i18next"
import Button from "react-bootstrap/Button"
import Modal from "react-bootstrap/Modal"
import Form from "react-bootstrap/Form"
import { useRefundAcceptRefundTrx } from "src/api/user"
import BlockUI from "src/helpers/BlockUI"
import DOMPurify from "dompurify"

function ApprovalPopupModal(props: any) {
  const {
    btntype,
    txt,
    reason,
    inputdisabled,
    id,
    color,
    apicallstatus,
    disabled,
    rejectreason,
    trx
  } = props
  const { t } = useTranslation()
  const [show, setShow] = useState(false)
  const [showAccept, setShowAccept] = useState(false)
  const [reqReason, setReqReason] = useState(reason)
  const [loaderShow, setLoaderShow] = useState(false)
  const [inputError, setInputError] = useState("")

  const {
    mutate: submitAcceptTransaction,
    error,
    isSuccess
  } = useRefundAcceptRefundTrx()

  useEffect(() => {
    if (isSuccess) {
      setTimeout(() => {
        setLoaderShow(false)
        setShowAccept(false)
        setShow(false)
        apicallstatus()
      }, 1000)
    }
  }, [isSuccess])

  useEffect(() => {
    if (error?.message) {
      setLoaderShow(false)
    }
  }, [error])

  const handleClose = () => setShow(false)
  const handleAcceptClose = () => setShowAccept(false)
  const handleShow = () => setShow(true)

  function submitAcceptDataNon(id: any, btntype: any) {
    console.log("submitAcceptDataNon")
  }
  function submitAcceptData(id: any, btntype: any) {
    const obj = {
      txn_uuid: id,
      status: "ACCEPT",
      type: "APPROVAL",
      rejectReason: ""
    }
    setShowAccept(true)
    setLoaderShow(true)
    submitAcceptTransaction({
      txn_uuid: obj.txn_uuid,
      status: obj.status,
      type: obj.type,
      rejectReason: obj.rejectReason
    })
  }

  function submitRejectData(id: any, btntype: any) {
    if (reqReason === "") {
      setInputError("Reject reason is required")
      setTimeout(() => {
        setInputError("")
      }, 2000)
      return
    }
    const obj = {
      txn_uuid: id,
      status: "REJECT",
      type: "APPROVAL",
      rejectReason: reqReason
    }
    setLoaderShow(true)
    submitAcceptTransaction({
      txn_uuid: obj.txn_uuid,
      status: obj.status,
      type: obj.type,
      rejectReason: obj.rejectReason
    })
  }
  const checkRefundBtnType = (type: string) => {
    if (type == null) {
      return <Button variant="btn btn-success btn-sm">{t("Approve")}</Button>
    }
    console.log("hi")
    console.log(type)
    if (type == "REFUNDED") {
      return (
        <Button
          color={"#ffffff"}
          variant="btn btn-primary btn-sm"
          style={{ background: "rgb(22, 114, 71)" }}
          onClick={() => submitAcceptDataNon(id, btntype)}
        >
          {t("Approved")}
        </Button>
      )
    }
    if (
      type == "REFUND_INITIATED" ||
      type == "REFUND_ACCEPTED" ||
      type == "REFUND_APPROVED"
    ) {
      return (
        <Button
          variant="btn btn-primary btn-sm"
          style={{ background: "rgb(117, 177, 145)" }}
          onClick={() => submitAcceptData(id, btntype)}
        >
          {t("Approve")}
        </Button>
      )
    }
    if (
      type == "REFUND_FAILED" ||
      type == "REFUND_CANCELED" ||
      type == "REFUND_EXPIRED"
    ) {
      return (
        <Button
          variant="btn"
          style={{ background: "rgb(240, 240, 240)" }}
          disabled
        >
          {t("Approve")}
        </Button>
      )
    }
  }
  return (
    <>
      {txt == "Approve" ? (
        <>
          {checkRefundBtnType(trx.refundStatus)}
          <Modal show={showAccept} onHide={handleAcceptClose} backdrop="static">
            <Modal.Header closeButton>
              <BlockUI blocking={loaderShow} title="submitting" />
            </Modal.Header>
            <Modal.Body>
              {error && (
                <div className="alert alert-danger" role="alert">
                  {error.message}
                </div>
              )}
              <textarea
                className="form-control"
                rows={3}
                value={reqReason}
                autoComplete="off"
                disabled={inputdisabled}
                onChange={(e) =>
                  {
                const sanitizedInput = DOMPurify.sanitize(e.target.value)
                  setReqReason(sanitizedInput)
                  }}
                style={{ visibility: "hidden" }}
              ></textarea>
            </Modal.Body>
          </Modal>
        </>
      ) : null}
      {txt == "Reject" ? (
        <>
          <button
            className={`${color} ${
              disabled && rejectreason ? "tooltip" : null
            }`}
            onClick={() => setShow(true)}
            disabled={disabled}
            style={{
              pointerEvents: "auto",
              background:
                trx.refundStatus == "REFUNDED" ? "rgb(240, 240, 240)" : ""
            }}
            data-title={disabled && rejectreason}
          >
            {trx.refundStatus === "REFUND_CANCELED" ? t("Rejected") : t(txt)}
          </button>
          <Modal show={show} onHide={handleClose} backdrop="static">
            <Modal.Header closeButton>
              <BlockUI blocking={loaderShow} title="submitting" />
              <Modal.Title>{t(txt)} Reason</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              {inputError && (
                <div className="alert alert-danger" role="alert">
                  {inputError}
                </div>
              )}
              {error && (
                <div className="alert alert-danger" role="alert">
                  {error.message}
                </div>
              )}
              <textarea
                className="form-control"
                rows={3}
                value={reqReason}
                autoComplete="off"
                disabled={inputdisabled}                
                onChange={(e) =>{                         
                   const sanitizedInput = DOMPurify.sanitize(e.target.value)
                  setReqReason(sanitizedInput)
                }}
              />
            </Modal.Body>
            <Modal.Footer>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleClose}
              >
                Close
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => submitRejectData(id, btntype)}
              >
                Submit
              </button>
            </Modal.Footer>
          </Modal>
        </>
      ) : null}
    </>
  )
}

export default ApprovalPopupModal
