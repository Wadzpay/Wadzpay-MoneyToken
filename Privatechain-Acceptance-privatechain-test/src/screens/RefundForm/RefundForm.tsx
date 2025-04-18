import React, { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import "./RefundFormStyle.scss"
import Button from "react-bootstrap/Button"
import Form from "react-bootstrap/Form"
import { useParams } from "react-router-dom"
import { useSubmitRefundFormData } from "src/api/user"
import Modal from "react-bootstrap/Modal"
import { useGetExchangeRateForRefund } from "src/api/onRamp"
import { useRefundFormFields } from "src/api/user"
import TermsAndConditionsPopupModal from "src/components/ui/PopUps/TermsAndConditionsPopupModal"
import {
  DIGITAL_ASSET,
  WALLET_MAX_LENGTH,
  WALLET_MIN_LENGTH,
  WALLET_REGEX
} from "src/constants/Defaults"
import { RouteType } from "src/constants/routeTypes"

import SavedGIF from "./success.gif"
import env, { ENV } from "../../env"
import DOMPurify from "dompurify"

export default function RefundForm({ data, x }: any): JSX.Element {
  const navigate = useNavigate()
  const [checkVal, setCheckVal] = useState(false)
  const [formErrorMessages, setFormErrorMessages] = useState({
    refundUserNameError: "",
    sourceWalletAddress: "",
    refundWalletAddress: "",
    refundReasonError: "",
    checkBoxError: "",
    confirmWalletAddress: "",
    serverError: ""
  })
  const digitalAsset =
    env.TYPE === ENV.DEV || env.TYPE === ENV.TESTING ? "BTC" : "USDT"
  const { data: refundExchangeRates } = useGetExchangeRateForRefund(
    data?.fiatAsset
  )

  const { id } = useParams() as { id: string }
  const [isVerificationFrom, setIsVerificationFrom] = useState<boolean>(true)
  const { data: refundFields } = useRefundFormFields("CVF")
  const [show, setShow] = useState(false)
  const [errorModelTitle, serErrorModelTitle] = useState("Required Fields")
  const {
    mutate: submitRefundForm,
    error,
    data: successRespData,
    isSuccess
  } = useSubmitRefundFormData()
  const [refundVal, setRefundVal] = useState({
    refundUserName: data.refundUserName ? data.refundUserName : "",
    refundUserMobile: data.refundUserMobile,
    refundUserEmail: data.refundUserEmail,
    sourceWalletAddress: data.isRefundReinitiate
      ? data.refundWalletAddress
        ? data.sourceWalletAddress
        : ""
      : "",
    refundWalletAddress: data.isRefundReinitiate
      ? data.refundWalletAddress
      : "",
    reasonForRefund: data.isRefundReinitiate ? data.refundReason : "",
    reconfirmWalletAddress: data.isRefundReinitiate
      ? data.refundWalletAddress
      : ""
  })
  const [storeRefundFiatAmount, setStoreRefundFiatAmount] = useState(0)
  const [successMsg, setSuccessMsg] = useState("")
  const handleChange = (e: any) => {
    const { name, value } = e.target
    const sanitizedInputName = DOMPurify.sanitize(name)
    const sanitizedInputValue = DOMPurify.sanitize(value)
    setRefundVal({ ...refundVal, [sanitizedInputName]: sanitizedInputValue })
  }
  const [showTermsAndConditionsForm, setShowTermsAndConditionsForm] =
    useState(false)

  useEffect(() => {
    if (refundExchangeRates && data) {
      renderRefundAmount()
    }
  }, [refundExchangeRates, data])

  const handleClose = () => {
    setShow(false)
  }
  const handleShow = () => setShow(true)
  const submitData = (values: any) => {
    values.preventDefault()
    const isReasonForRefundValid =
      refundVal.reasonForRefund && refundVal.reasonForRefund.length > 0
    const isRefundUserNameValid =
      refundVal.refundUserName &&
      refundVal.refundUserName.length > 0 &&
      refundVal.refundUserName.match(/^[A-Za-z ]+$/)
    const isSourceWalletAddressValid =
      refundVal.sourceWalletAddress && refundVal.sourceWalletAddress.length > 0
    const isRefundWalletAddressValid =
      refundVal.reconfirmWalletAddress &&
      refundVal.reconfirmWalletAddress.length > 0 &&
      refundVal.refundWalletAddress.length >= WALLET_MIN_LENGTH &&
      refundVal.refundWalletAddress.length <= WALLET_MAX_LENGTH &&
      WALLET_REGEX.test(refundVal.refundWalletAddress)
    const isConfirmWalletAddressValid =
      refundVal.refundWalletAddress === refundVal.reconfirmWalletAddress
    const isCheckError = checkVal ? true : false

    // const obj = {
    //   transactionId: data?.uuid,
    //   refundUserName: refundVal.refundUserName,
    //   refundUserMobile: refundVal.refundUserMobile
    //     ? refundVal.refundUserMobile
    //     : null,
    //   refundUserEmail: refundVal.refundUserEmail
    //     ? refundVal.refundUserEmail
    //     : null,
    //   refundWalletAddress: refundVal.refundWalletAddress,
    //   refundDigitalType: data?.refundDigitalCurrencyType,
    //   refundFiatType: data?.refundFiatType,
    //   refundAmountDigital: storeRefundFiatAmount,
    //   reasonForRefund: refundVal.reasonForRefund,
    //   refundAmountFiat: data?.refundFiatAmount,
    //   refundMode: data?.refundMode
    // }
    // console.log(obj)
    if (
      isReasonForRefundValid &&
      isRefundUserNameValid &&
      isSourceWalletAddressValid &&
      isRefundWalletAddressValid &&
      isCheckError &&
      isConfirmWalletAddressValid
    ) {
      submitRefundForm({
        transactionId: data?.uuid,
        refundUserName: refundVal.refundUserName,
        refundUserMobile: refundVal.refundUserMobile
          ? refundVal.refundUserMobile
          : null,
        refundUserEmail: refundVal.refundUserEmail
          ? refundVal.refundUserEmail
          : null,
        sourceWalletAddress: refundVal.sourceWalletAddress,
        refundWalletAddress: refundVal.refundWalletAddress,
        refundDigitalType: data?.refundDigitalCurrencyType,
        refundFiatType: data?.refundFiatType,
        refundAmountDigital: storeRefundFiatAmount,
        reasonForRefund: refundVal.reasonForRefund,
        refundAmountFiat: data?.refundFiatAmount,
        refundMode: data?.refundMode,
        refundToken: id
      })
    } else {
      setShow(true)
      serErrorModelTitle("Required Fields")
      setFormErrorMessages((prevState) => ({
        ...prevState,
        refundReasonError: !isReasonForRefundValid
          ? "Reason For Refund Field is empty"
          : "",
        refundUserNameError: !isRefundUserNameValid
          ? "Username Field is Invalid"
          : "",
        sourceWalletAddress: !isSourceWalletAddressValid
          ? "Source Wallet Address Field is Invalid"
          : "",
        refundWalletAddress: !isRefundWalletAddressValid
          ? "Wallet Address Field is Invalid"
          : "",
        checkBoxError:
          isCheckError === false
            ? "Accept the Terms and Conditions Checkbox"
            : "",
        confirmWalletAddress: !isConfirmWalletAddressValid
          ? "Wallet address and confirm wallet address must be same"
          : ""
      }))
      setSuccessMsg("")
      if (!isConfirmWalletAddressValid) {
        refundVal.refundWalletAddress = ""
        refundVal.reconfirmWalletAddress = ""
      }
    }
  }

  useEffect(() => {
    if (successRespData) {
      setSuccessMsg("Data Submitted Successfully!!")
      setFormErrorMessages({
        refundUserNameError: "",
        sourceWalletAddress: "",
        refundWalletAddress: "",
        refundReasonError: "",
        checkBoxError: "",
        confirmWalletAddress: "",
        serverError: ""
      })
      setShow(false)
    }
  }, [isSuccess])

  useEffect(() => {
    if (error?.message) {
      serErrorModelTitle("Error:")
      setShow(true)
      setFormErrorMessages((prevState) => ({
        refundUserNameError: "",
        sourceWalletAddress: "",
        refundWalletAddress: "",
        refundReasonError: "",
        checkBoxError: "",
        confirmWalletAddress: "",
        serverError: error?.message
      }))
    }
  }, [error])

  const onCancelClick = () => {
    setRefundVal({
      refundUserName: data.refundUserName ? data.refundUserName : "",
      refundUserMobile: data.refundUserMobile,
      refundUserEmail: data.refundUserEmail,
      sourceWalletAddress: "",
      refundWalletAddress: "",
      reasonForRefund: data.refundReason,
      reconfirmWalletAddress: ""
    })
    setFormErrorMessages({
      refundUserNameError: "",
      sourceWalletAddress: "",
      refundWalletAddress: "",
      refundReasonError: "",
      checkBoxError: "",
      confirmWalletAddress: "",
      serverError: ""
    })
    setCheckVal(false)
    setSuccessMsg("")
  }

  function renderRefundAmount() {
    if (refundExchangeRates) {
      const value = refundExchangeRates[digitalAsset] * data.refundFiatAmount
      const finalVal = value.toFixed(8)
      setStoreRefundFiatAmount(parseFloat(finalVal))
    }
  }

  const closeModal = () => {
    setIsVerificationFrom(false)
    navigate(RouteType.HOME)
  }

  return (
    <>
      {isVerificationFrom ? (
        <div style={{ overflow: "hidden" }} className="refund-main">
          <span onClick={() => closeModal()} className="closeButtonCustom">
            X
          </span>
          <h2 className="refund-header">
            Customer&apos;s Verification form for Refund
          </h2>
          <Modal
            show={show}
            onHide={handleClose}
            onShow={handleShow}
            backdrop="static"
          >
            <Modal.Header>
              <Modal.Title>{"Required Fields"}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <ul>
                {formErrorMessages.checkBoxError && (
                  <li>{formErrorMessages.checkBoxError}</li>
                )}
                {formErrorMessages.refundReasonError && (
                  <li>{formErrorMessages.refundReasonError}</li>
                )}
                {formErrorMessages.refundUserNameError && (
                  <li>{formErrorMessages.refundUserNameError}</li>
                )}
                {formErrorMessages.sourceWalletAddress && (
                  <li>{formErrorMessages.sourceWalletAddress}</li>
                )}
                {formErrorMessages.refundWalletAddress && (
                  <li>{formErrorMessages.refundWalletAddress}</li>
                )}
                {formErrorMessages.confirmWalletAddress && (
                  <li>{formErrorMessages.confirmWalletAddress}</li>
                )}
                {formErrorMessages.serverError && (
                  <li>{formErrorMessages.serverError}</li>
                )}
              </ul>
            </Modal.Body>
            <Modal.Footer>
              <Button variant="secondary" onClick={handleClose}>
                Close
              </Button>
            </Modal.Footer>
          </Modal>
          {successMsg ? (
            <div style={{ textAlign: "center" }}>
              <div className="save-bg-img">
                <img className="save-img" src={SavedGIF} alt="Save Image" />
              </div>
              <div
                style={{
                  fontSize: "30px",
                  fontWeight: 600,
                  paddingTop: "20px"
                }}
              >
                {successMsg}
              </div>
            </div>
          ) : (
            <Form autoComplete="off" onSubmit={submitData}   >
              {refundFields?.txnReference && (
                <Form.Group className="mb-3" controlId="formBasicEmail">
                  <Form.Label>Transaction reference</Form.Label>
                  <Form.Control
                    type="text"
                    aria-autocomplete='both' aria-haspopup="false"
                    //autoComplete="true" 
                    placeholder="Transaction reference"
                    value={data?.uuid}
                    name="transactionId"
                    disabled
                  />
                </Form.Group>
              )}
              {refundFields?.customerName && (
                <Form.Group className="mb-3" controlId="formBasicPassword">
                  <Form.Label>Customer Name</Form.Label>
                  <Form.Control
                    type="text"
                    placeholder="Customer Name"
                    aria-autocomplete='both' aria-haspopup="false"
                    onChange={handleChange}
                    value={refundVal.refundUserName}
                    name="refundUserName"
                    //autoComplete="true" 
                     maxLength={30}
                    disabled={data?.refundUserName ? true : false}
                  />
                  {/* {refundVal.refundUserName &&
                !refundVal.refundUserName.match(/^[A-Za-z]+$/) && (
                  <p className="text-danger">Enter Valid Name!</p>
                )} */}
                </Form.Group>
              )}

              {refundFields?.mobile && (
                <Form.Group className="mb-3" controlId="formBasicPassword">
                  <Form.Label>Mobile number</Form.Label>
                  <Form.Control
                    type="text"
                    placeholder="Mobile number"
                    aria-autocomplete='both' aria-haspopup="false"
                    onChange={handleChange}
                    value={refundVal.refundUserMobile}
                    name="refundUserMobile"
                    //autoComplete="true" 
                    disabled={
                      data?.refundUserMobile
                        ? true
                        : false || data?.refundUserEmail.length > 0
                        ? true
                        : false
                    }
                  />
                </Form.Group>
              )}

              {refundFields?.email && (
                <Form.Group className="mb-3" controlId="formBasicPassword">
                  <Form.Label>Email Address</Form.Label>
                  <Form.Control
                    type="email"
                    placeholder="Email Address"
                    onChange={handleChange}
                    value={refundVal.refundUserEmail}
                    name="refundUserEmail"
                    disabled={
                      data?.refundUserEmail
                        ? true
                        : false || data?.refundUserMobile.length > 0
                        ? true
                        : false
                    }
                  />
                </Form.Group>
              )}

              {refundFields?.digitalAmt && (
                <Form.Group className="mb-3" controlId="formBasicPassword">
                  <Form.Label>Digital currency amount</Form.Label>
                  <Form.Control
                    type="number"
                    aria-autocomplete='both' aria-haspopup="false"
                    //autoComplete="true" 
                    placeholder="Digital currency amount"
                    value={data?.totalDigitalCurrencyReceived}
                    name="refundAmountDigital"
                    disabled
                  />
                </Form.Group>
              )}

              {refundFields?.digitalName && (
                <Form.Group className="mb-3" controlId="formBasicPassword">
                  <Form.Label>Digital currency name</Form.Label>
                  <Form.Control
                    type="text"
                    //autoComplete="true" 
                    placeholder="Digital currency name"
                    aria-autocomplete='both' aria-haspopup="false"
                    value={data?.refundDigitalCurrencyType}
                    name="refundDigitalType"
                    disabled
                  />
                </Form.Group>
              )}

              {refundFields?.refundAmountInFiat && (
                <Form.Group className="mb-3" controlId="formBasicPassword">
                  <Form.Label>Refund Amount in {data?.fiatAsset}</Form.Label>
                  <Form.Control
                    type="number"
                    aria-autocomplete='both' aria-haspopup="false"
                    //autoComplete="true" 
                    placeholder={`Refund Amount in ${data?.fiatAsset}`}
                    value={data?.refundFiatAmount}
                    name="refundAmountFiat"
                    disabled
                  />
                </Form.Group>
              )}

              {refundFields?.refundAmountInCrypto && (
                <Form.Group className="mb-3">
                  <Form.Label>Refund Amount in {digitalAsset}</Form.Label>
                  <Form.Control
                    type="number"
                    aria-autocomplete='both' aria-haspopup="false"
                    //autoComplete="true" 
                    placeholder={`Refund Amount in ${digitalAsset}`}
                    value={storeRefundFiatAmount}
                    name="refundFiatAmount"
                    disabled
                  />
                </Form.Group>
              )}

              {refundFields?.reason && (
                <Form.Group className="mb-3" controlId="formBasicPassword">
                  <Form.Label>Reason for Refund</Form.Label>
                  <Form.Control
                    type="text"
                    //autoComplete="true" 
                    placeholder="Reason for Refund"
                    aria-autocomplete='both' aria-haspopup="false"
                    onChange={handleChange}
                    value={refundVal.reasonForRefund || ""}
                    name="reasonForRefund"
                    disabled={data?.refundReason ? true : false}
                  />
                  <Form.Text className="text-muted">
                    to be filled by customer
                  </Form.Text>
                </Form.Group>
              )}

              {refundFields?.srcWalletAddr && (
                <Form.Group className="mb-3">
                  <Form.Label>Source wallet address</Form.Label>
                  <Form.Control
                    type="text"
                    aria-autocomplete='both' aria-haspopup="false"
                    //autoComplete="true" 
                    placeholder={`Source wallet address`}
                    value={refundVal.sourceWalletAddress}
                    name="sourceWalletAddress"
                    onChange={handleChange}
                    maxLength={WALLET_MAX_LENGTH}
                  />
                </Form.Group>
              )}

              {refundFields?.walletAddr && (
                <Form.Group className="mb-3" controlId="formBasicPassword">
                  <Form.Label>{digitalAsset} wallet address</Form.Label>
                  <Form.Control
                    type="text"
                    aria-autocomplete='both' aria-haspopup="false"
                    //autoComplete="true" 
                    maxLength={WALLET_MAX_LENGTH}
                    placeholder={`${digitalAsset} wallet address`}
                    onChange={handleChange}
                    value={refundVal.refundWalletAddress}
                    name="refundWalletAddress"
                  />
                  <Form.Text className="text-muted">
                    to be filled by customer
                  </Form.Text>
                  {/* {refundVal.refundWalletAddress === "" &&
                (refundVal.refundWalletAddress.length < 24 ||
                  refundVal.refundWalletAddress.length > 32) && (
                  <p className="text-danger">Enter Valid Wallet Address!</p>
                )} */}
                </Form.Group>
              )}

              {refundFields?.confirmWalletAddr && (
                <Form.Group className="mb-3" controlId="formBasicPassword">
                  <Form.Label>
                    Reconfirm {digitalAsset} wallet address
                  </Form.Label>
                  <Form.Control
                    type="text"
                                        // autoComplete="true" 
                    maxLength={WALLET_MAX_LENGTH}
                    aria-autocomplete='both' aria-haspopup="false"
                    placeholder={`Reconfirm ${digitalAsset} wallet address`}
                    onChange={handleChange}
                    value={refundVal.reconfirmWalletAddress}
                    name="reconfirmWalletAddress"
                  />
                  <Form.Text className="text-muted">
                    to be filled by customer
                  </Form.Text>
                </Form.Group>
              )}

              <h4>
                Declaration signed by the customer to the effect that (Customer
                to accept and sign).
              </h4>
              <ul className="list-details">
                <li>
                  Refund will be in {digitalAsset}, credited into a{" "}
                  {digitalAsset} wallet.
                </li>
                <li>Gas fee will be borne by the customer.</li>
                <li>
                  Upon verification, refund value will be processed and will be
                  credited in customer&apos;s account within 7 days.
                </li>
                <li>
                  Refund amount would be credited based on conversion rate of
                  the original crypto currency at the time of execution of
                  refund transaction with the deduction of gas fee.
                </li>
              </ul>
              <Form.Group className="mb-3 mt-4" controlId="formBasicCheckbox">
                <div style={{ display: "flex" }}>
                  <Form.Check
                    type="checkbox"
                    checked={checkVal}
                    onChange={() => setCheckVal(!checkVal)}
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
              <Button
                variant="primary"
                type="submit"
                disabled={!checkVal}
                // disabled={
                //   (refundVal.refundUserName &&
                //     !refundVal.refundUserName.match(/^[A-Za-z]+$/)) ||
                //   (refundVal.refundWalletAddress &&
                //     (refundVal.refundWalletAddress.length < 24 ||
                //       refundVal.refundWalletAddress.length > 32))
                //     ? true
                //     : false
                // }
                onClick={submitData}
              >
                Submit
              </Button>
              <Button
                variant="secondary"
                className="ml-4"
                onClick={onCancelClick}
              >
                Clear
              </Button>
            </Form>
          )}
          {showTermsAndConditionsForm ? (
            <TermsAndConditionsPopupModal
              showhide={() => setShowTermsAndConditionsForm(false)}
            />
          ) : null}
        </div>
      ) : (
        <></>
      )}
    </>
  )
}
