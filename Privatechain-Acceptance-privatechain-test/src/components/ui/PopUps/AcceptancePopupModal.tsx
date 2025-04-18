import React, { useState, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { Button, OverlayTrigger, Tooltip } from "react-bootstrap"
import Modal from "react-bootstrap/Modal"
import Form from "react-bootstrap/Form"
import { useRefundAcceptRefundTrx } from "src/api/user"
import BlockUI from "src/helpers/BlockUI"
import DOMPurify from "dompurify"

const btnMinWidth = "70px"

function AcceptancePopupModal(props: any) {
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
  const [holdButtonDisabled, setHoldButtonDisabled] = useState(false)
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

  const checkRefundBtnType = (type: string) => {
    if (type == null) {
      return <Button variant="btn btn-success btn-sm">{t("Approve")}</Button>
    }
    if (type == "REFUNDED") {
      return (
        <Button
          color={"#ffffff"}
          variant="btn btn-primary btn-sm"
          style={{ background: "rgb(22, 114, 71)" }}
          onClick={() => submitAcceptDataNon(id, btntype)}
        >
          {t("Accepted")}
        </Button>
      )
    }
    if (type == "REFUND_ACCEPTED" || type == "REFUND_HOLD") {
      return (
        <Button
          color={"#ffffff"}
          variant="btn btn-success btn-sm"
          style={{ background: "rgb(117, 177, 145)" }}
          onClick={() => submitAcceptData(id, btntype)}
        >
          {t("Accept")}
        </Button>
      )
    }
    if (type == "REFUND_HOLD") {
      return (
        <Button
          color={"#ffffff"}
          variant="btn btn-success btn-sm"
          style={{ background: "rgb(117, 177, 145)" }}
          disabled={true}
        >
          {t("Accept")}
        </Button>
      )
    }
    if (type == "REFUND_INITIATED" || type == "REFUND_APPROVED") {
      return (
        <Button variant="btn btn-primary btn-sm" disabled>
          {t("Accepted")}
        </Button>
      )
    }
    if (type == "REFUND_CANCELED" || type == "REFUND_EXPIRED") {
      return (
        <Button
          variant="btn"
          style={{ background: "rgb(240, 240, 240)" }}
          disabled
        >
          {t("Accept")}
        </Button>
      )
    }
    if (type == "REFUND_FAILED") {
      return (
        <Button
          variant="btn btn-danger btn-sm"
          style={{ background: "rgb(203 29 54)" }}
        >
          {t("Accepted(Failed)")}
        </Button>
      )
    }
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

  function submitHoldData(id: any, status: any) {
    const obj = {
      txn_uuid: id,
      status: status === ("REFUND_ACCEPTED" || "REFUND_HOLD") ? "HOLD" : "HOLD",
      type: "HOLD",
      rejectReason: ""
    }

    //setHoldButtonDisabled(true)

    submitAcceptTransaction({
      txn_uuid: obj.txn_uuid,
      status: obj.status,
      type: obj.type,
      rejectReason: obj.rejectReason
    })
  }

  function handleClear() {
    setReqReason("")
  }
  return (
    <>
      {txt == "Accept" ? (
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
                autoComplete="off"
                rows={3}
                value={reqReason}
                disabled={inputdisabled}
                onChange={(e) =>{ 
                  const sanitizedInput = DOMPurify.sanitize(e.target.value)
                  setReqReason(sanitizedInput)}}
                style={{ visibility: "hidden" }}
              ></textarea>
            </Modal.Body>
          </Modal>
        </>
      ) : null}
      {txt == "Reject" ? (
        <>
          <OverlayTrigger
            placement="left"
            overlay={
              trx.refundStatus === "REFUND_CANCELED" ? (
                <Tooltip id="tooltip-disabled">
                  {trx.refundAcceptanceComment}
                </Tooltip>
              ) : (
                <span></span>
              )
            }
          >
            <button
              className={`${color}`}
              onClick={() =>
                setShow(
                  trx.refundStatus == "REFUNDED" ||
                    trx.refundStatus === "REFUND_EXPIRED" ||
                    trx.refundStatus === "REFUND_CANCELED" ||
                    trx.refundStatus === "REFUND_FAILED"
                    ? false
                    : true
                )
              }
              style={{
                background:
                  trx.refundStatus == "REFUNDED" ||
                  trx.refundStatus === "REFUND_FAILED"
                    ? "rgb(240, 240, 240)"
                    : trx.refundStatus === "REFUND_EXPIRED" ||
                      trx.refundStatus === "REFUND_CANCELED"
                    ? ""
                    : "#e87c87",
                cursor:
                  trx.refundStatus === "REFUND_ACCEPTED" ? "pointer" : "default"
              }}
              //data-title={disabled && rejectreason}
            >
              {trx.refundStatus === "REFUND_CANCELED" ? t("Rejected") : t(txt)}
            </button>
          </OverlayTrigger>
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
                disabled={inputdisabled}
                value={reqReason}
                autoComplete="off"
                onChange={(e) => {
                  const sanitizedInput = DOMPurify.sanitize(e.target.value)
                  setReqReason(sanitizedInput)
                }}
              ></textarea>
            </Modal.Body>
            <Modal.Footer>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => handleClear()}
              >
                Clear
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
      {txt == "Hold" ? (
        <Button
          color={"#ffffff"}
          variant="btn btn-danger btn-sm"
          style={
            trx.refundStatus === "REFUND_ACCEPTED" ||
            trx.refundStatus === "REFUNDED"
              ? { background: "rgb(232 124 135)", width: btnMinWidth }
              : { width: btnMinWidth }
          }
          onClick={() => submitHoldData(id, trx.refundStatus)}
          disabled={
            holdButtonDisabled || trx.refundStatus === "REFUNDED" ? true : false
          }
        >
          {t(
            trx.refundStatus === "REFUND_ACCEPTED" ||
              trx.refundStatus === "REFUNDED"
              ? "Hold"
              : "On Hold"
          )}
        </Button>
      ) : null}
    </>
  )
}

export default AcceptancePopupModal
