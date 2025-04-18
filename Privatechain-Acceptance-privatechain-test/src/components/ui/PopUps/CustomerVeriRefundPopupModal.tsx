import React, { useState, useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import { Button, Dropdown } from "react-bootstrap";
import Modal from "react-bootstrap/Modal";
import Form from "react-bootstrap/Form";
import Col from "react-bootstrap/Col";
import Row from "react-bootstrap/Row";
import Container from "react-bootstrap/Container";
import { useNavigate } from "react-router-dom";
import { RouteType } from "src/constants/routeTypes";
import { useRefundFormFields } from "src/api/user";
import { useSubmitRefundformWithAuth } from "src/api/user";
import BlockUI from "src/helpers/BlockUI";
import { useGetExchangeRate } from "src/api/onRamp";
import ReactToPrint from "react-to-print";
import {
  Asset,
  CryptoFullName,
  FiatAsset,
  TokenToAmount,
} from "src/constants/types";
import {
  WALLET_MAX_LENGTH,
  WALLET_MIN_LENGTH,
  WALLET_REGEX,
} from "src/constants/Defaults";
import env from "src/env.template";

import { ENV } from "../../../env";
import TermsAndConditionsPopupModal from "./TermsAndConditionsPopupModal";
import DOMPurify from "dompurify";

function CustomerVeriRefundPopupModal(props: any) {
  const { rowtrx, refundableFiatAmount } = props;

  const { t } = useTranslation();
  const navigate = useNavigate();
  const [showCustomerPop, setShowCustomerPop] = useState(true);
  const [loaderShow, setLoaderShow] = useState(false);
  const [formErrorMessage, setFormErrorMessage] = useState("");
  const [customerVerificationSubmitBtn, setCustomerVerificationSubmitBtn] =
    useState(false);
  const [initiateData, setInitiateData] = useState({
    refundDigitalType: "SAR",
    refundFiatType: rowtrx.fiatAsset,
    refundAmountFiat: refundableFiatAmount,
    refundUserName:
      rowtrx.refundStatus === "REFUND_CANCELED" ||
      rowtrx.refundStatus === "REFUND_FAILED" ||
      rowtrx.refundStatus === "REFUND_EXPIRED"
        ? rowtrx.refundUserName
        : "",
    refundUserMobile:
      rowtrx.refundStatus === "REFUND_CANCELED" ||
      rowtrx.refundStatus === "REFUND_FAILED" ||
      rowtrx.refundStatus === "REFUND_EXPIRED"
        ? rowtrx.refundUserMobile == null
          ? ""
          : rowtrx.refundUserMobile.includes("+91")
          ? rowtrx.refundUserMobile.split("+91")[1]
          : rowtrx.refundUserMobile.includes("+61")
          ? rowtrx.refundUserMobile.split("+61")[1]
          : rowtrx.refundUserMobile.includes("+971")
          ? rowtrx.refundUserMobile.split("+971")[1]
          : ""
        : "",
    refundUserMobileCountryCode:
      rowtrx.refundStatus === "REFUND_CANCELED" ||
      rowtrx.refundStatus === "REFUND_FAILED" ||
      rowtrx.refundStatus === "REFUND_EXPIRED"
        ? rowtrx.refundUserMobile === null
          ? ""
          : rowtrx.refundUserMobile.includes("+91")
          ? "+91"
          : rowtrx.refundUserMobile.includes("+61")
          ? "+61"
          : rowtrx.refundUserMobile.includes("+971")
          ? "+971"
          : ""
        : "",
    refundUserEmail:
      rowtrx.refundStatus === "REFUND_CANCELED" ||
      rowtrx.refundStatus === "REFUND_FAILED" ||
      rowtrx.refundStatus === "REFUND_EXPIRED"
        ? rowtrx.refundUserEmail
        : "",
    sourceWalletAddress:
      rowtrx.refundStatus === "REFUND_CANCELED" ||
      rowtrx.refundStatus === "REFUND_FAILED" ||
      rowtrx.refundStatus === "REFUND_EXPIRED"
        ? rowtrx.refundWalletAddress
          ? rowtrx.sourceWalletAddress
          : ""
        : "",
    refundWalletAddress:
      rowtrx.refundStatus === "REFUND_CANCELED" ||
      rowtrx.refundStatus === "REFUND_FAILED" ||
      rowtrx.refundStatus === "REFUND_EXPIRED"
        ? rowtrx.refundWalletAddress
        : "",
    refundWalletAddressConfirm:
      rowtrx.refundStatus === "REFUND_CANCELED" ||
      rowtrx.refundStatus === "REFUND_FAILED" ||
      rowtrx.refundStatus === "REFUND_EXPIRED"
        ? rowtrx.refundWalletAddress
        : "",
    refundAmountDigital: 0,
    reasonForRefund:
      rowtrx.refundStatus === "REFUND_CANCELED" ||
      rowtrx.refundStatus === "REFUND_FAILED" ||
      rowtrx.refundStatus === "REFUND_EXPIRED"
        ? rowtrx.refundReason
        : "",
    refundTermsConditionChecked: false,
  });

  const resetData = {
    refundDigitalType: rowtrx.asset,
    refundFiatType: rowtrx.fiatAsset,
    refundAmountFiat: refundableFiatAmount,
    refundUserName: "",
    refundUserMobile: "",
    refundUserMobileCountryCode: "",
    refundUserEmail: "",
    sourceWalletAddress: "",
    refundWalletAddress: "",
    refundWalletAddressConfirm: "",
    refundAmountDigital: 0,
    reasonForRefund: "",
    refundTermsConditionChecked: false,
  };

  const { data: exchangeRatesData, isFetching: isFetchingExchangeRates } =
    useGetExchangeRate(rowtrx.fiatAsset);

  const digitalAsset =
    env.TYPE === ENV.DEV || env.TYPE === ENV.TESTING ? "BTC" : "USDT";

  const { data: refundFields } = useRefundFormFields("PCVF");

  const {
    mutate: submitRefundform,
    error,
    isSuccess,
  } = useSubmitRefundformWithAuth();

  const printButtonRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isSuccess) {
      setLoaderShow(true);
      setTimeout(() => {
        window.location.reload();
      }, 2000);
    }
  }, [isSuccess]);

  useEffect(() => {
    if (error?.message) {
      setLoaderShow(false);
    }
  }, [error]);

  useEffect(() => {
    if (rowtrx) {
      // console.log(rowtrx)
    }
  }, [rowtrx]);

  useEffect(() => {
    if (
      initiateData &&
      initiateData.refundUserName &&
      initiateData.reasonForRefund &&
      initiateData.sourceWalletAddress &&
      initiateData.refundWalletAddress &&
      initiateData.refundWalletAddressConfirm &&
      initiateData.refundTermsConditionChecked
    ) {
      // console.log(rowtrx)
      setCustomerVerificationSubmitBtn(true);
    } else {
      setCustomerVerificationSubmitBtn(false);
    }
  }, [
    initiateData,
    initiateData.refundUserName,
    initiateData.reasonForRefund,
    initiateData.sourceWalletAddress,
    initiateData.refundWalletAddress,
    initiateData.refundWalletAddressConfirm,
    initiateData.refundTermsConditionChecked,
  ]);

  const handleClose = () => setShowCustomerPop(false);
  const handleShow = () => setShowCustomerPop(true);

  function storeData(val: any, inputType: any) {
    switch (inputType) {
      case "name":
        setInitiateData({
          ...initiateData,
          refundUserName: val,
        });
        break;
      case "mobileCode":
        setInitiateData({
          ...initiateData,
          refundUserMobileCountryCode: val,
        });
        break;
      case "mobileNumber":
        setInitiateData({ ...initiateData, refundUserMobile: val });
        break;
      case "email":
        setInitiateData({ ...initiateData, refundUserEmail: val });
        break;
      case "refundreason":
        setInitiateData({ ...initiateData, reasonForRefund: val });
        break;
      case "sourceWalletAddress":
        setInitiateData({
          ...initiateData,
          sourceWalletAddress: val.replace(/[^\w\s]/gi, ``),
        });
        break;
      case "walletaddress":
        setInitiateData({
          ...initiateData,
          refundWalletAddress: val.replace(/[^\w\s]/gi, ``),
        });
        break;
      case "walletaddressconfirm":
        setInitiateData({
          ...initiateData,
          refundWalletAddressConfirm: val.replace(/[^\w\s]/gi, ``),
        });
        break;
      case "termsConditionChecked":
        setInitiateData({
          ...initiateData,
          refundTermsConditionChecked: val,
        });
        break;
      default:
        break;
    }
  }

  const fiatRecievedAmount = rowtrx.totalFiatRecieved
    ? rowtrx.totalFiatRecieved.toFixed(8)
    : 0;

  function renderRefundAmount() {
    if (exchangeRatesData && initiateData) {
      const value =
        exchangeRatesData[digitalAsset] * initiateData.refundAmountFiat;
      const finalVal = value.toFixed(8);
      return parseFloat(finalVal);
    }
  }

  function submitData() {
    setFormErrorMessage("");
    // const obj = {
    //   refundDigitalType: rowtrx.asset,
    //   refundFiatType: rowtrx.fiatAsset,
    //   refundAmountFiat: initiateData.refundAmountFiat,
    //   transactionId: rowtrx.uuid,
    //   refundUserName: initiateData.refundUserName,
    //   refundUserMobile:
    //     initiateData.refundUserMobileCountryCode +
    //       initiateData.refundUserMobile ===
    //     ""
    //       ? null
    //       : initiateData.refundUserMobileCountryCode +
    //         initiateData.refundUserMobile,
    //   refundUserEmail:
    //     initiateData.refundUserEmail === ""
    //       ? null
    //       : initiateData.refundUserEmail,
    //   refundWalletAddress: initiateData.refundWalletAddressConfirm,
    //   refundAmountDigital: renderRefundAmount() || 0,
    //   reasonForRefund: initiateData.reasonForRefund,
    //   refundMode: "WALLET"
    // }
    // console.log(obj)
    const pattern = /^[A-Za-z ]+$/;
    if (!pattern.test(initiateData.refundUserName)) {
      setFormErrorMessage("Enter Valid Customer Name");
      return;
    }
    // if (initiateData.refundUserMobileCountryCode == "") {
    //   setFormErrorMessage("Select Mobile Number Country Code")
    //   return
    // }
    // if (initiateData.refundUserMobile == "") {
    //   setFormErrorMessage("Fill Mobile Number")
    //   return
    // }
    // if (initiateData.refundUserEmail == "") {
    //   setFormErrorMessage("Fill Email Address")
    //   return
    // }
    if (initiateData.reasonForRefund == "") {
      setFormErrorMessage("Fill Reason for Refund");
      return;
    }
    if (initiateData.sourceWalletAddress == "") {
      setFormErrorMessage("Fill source Wallet Address");
      return;
    }
    if (initiateData.refundWalletAddress == "") {
      setFormErrorMessage("Fill Wallet Address");
      return;
    }
    if (initiateData.refundWalletAddressConfirm == "") {
      setFormErrorMessage("Fill Confirm Wallet Address");
      return;
    }
    if (
      initiateData.refundWalletAddressConfirm !=
      initiateData.refundWalletAddress
    ) {
      setFormErrorMessage(
        "Wallet Address And Confirm Wallet Address Not Matching"
      );
      initiateData.refundWalletAddressConfirm = "";
      initiateData.refundWalletAddress = "";
      return;
    }
    if (
      !(
        initiateData.refundWalletAddressConfirm &&
        WALLET_REGEX.test(initiateData.refundWalletAddress) &&
        initiateData.refundWalletAddress.length >= WALLET_MIN_LENGTH &&
        initiateData.refundWalletAddress.length <= WALLET_MAX_LENGTH
      )
    ) {
      setFormErrorMessage("Wallet Address not Valid");
      return;
    }

    if (!initiateData.refundTermsConditionChecked) {
      setFormErrorMessage("Read Terms and Conditions and Accept");
      return;
    }
    if (
      initiateData.refundUserMobile !== "" &&
      initiateData.refundUserMobileCountryCode === ""
    ) {
      setFormErrorMessage("Select Mobile Code");
      setTimeout(() => {
        setFormErrorMessage("");
      }, 2000);
      return;
    }
    setLoaderShow(true);
    submitRefundform({
      refundDigitalType: rowtrx.asset,
      refundFiatType: rowtrx.fiatAsset,
      refundAmountFiat: initiateData.refundAmountFiat,
      transactionId: rowtrx.uuid,
      refundUserName: initiateData.refundUserName,
      refundUserMobile:
        initiateData.refundUserMobileCountryCode +
          initiateData.refundUserMobile ===
        ""
          ? null
          : initiateData.refundUserMobileCountryCode +
            initiateData.refundUserMobile,
      refundUserEmail:
        initiateData.refundUserEmail === ""
          ? null
          : initiateData.refundUserEmail,
      sourceWalletAddress: initiateData.sourceWalletAddress,
      refundWalletAddress: initiateData.refundWalletAddressConfirm,
      refundAmountDigital: renderRefundAmount() || 0,
      reasonForRefund: initiateData.reasonForRefund,
      refundMode: "WALLET",
      isReInitiateRefund:
        rowtrx.refundStatus == "REFUND_CANCELED" ||
        rowtrx.refundStatus == "REFUND_FAILED" ||
        rowtrx.refundStatus == "REFUND_EXPIRED"
          ? true
          : false,
      refundTransactionID:
        rowtrx.refundStatus == "REFUND_CANCELED" ||
        rowtrx.refundStatus == "REFUND_FAILED" ||
        rowtrx.refundStatus == "REFUND_EXPIRED"
          ? rowtrx.refundTransactionID
          : null,
      balanceAmountFiat: 0,
    });
  }

  return (
    <>
      <Modal
        show={showCustomerPop}
        onHide={handleClose}
        size="lg"
        centered
        backdrop="static"
      >
        <Modal.Header closeButton>
          <BlockUI blocking={loaderShow} title="submitting" />
          <Modal.Title></Modal.Title>
          {formErrorMessage && (
            <div
              className="alert alert-danger"
              role="alert"
              style={{ textAlign: "center" }}
            >
              {formErrorMessage}
            </div>
          )}
        </Modal.Header>
        <Modal.Body
          style={{ height: "550px", overflowY: "scroll" }}
          className="show-grid"
        >
          {error && (
            <div className="alert alert-danger" role="alert">
              {error.message}
            </div>
          )}
          <Container ref={printButtonRef}>
            <Row>
              <Col
                className="col-md-10 mb-4"
                md={{ offset: 1 }}
                style={{ marginBottom: "10px" }}
              >
                <h2 className="refund-header">
                  Customer&apos;s Verification form for Refund
                </h2>
                <Form  autoComplete="off"  >
                  {refundFields?.txnReference && (
                    <Form.Group className="mb-3">
                      <Form.Label>Transaction reference</Form.Label>
                      <Form.Control
                        type="text"
                        aria-autocomplete='both' aria-haspopup="false"
/*                         autoComplete="true" 
 */                        placeholder="Transaction reference"
                        value={rowtrx.uuid}
                        disabled
                        inputMode="text"
                      />
                    </Form.Group>
                  )}

                  {refundFields?.customerName && (
                    <Form.Group className="mb-3">
                      <Form.Label>Customer Name</Form.Label>
                      <Form.Control
                        type="text"
                        placeholder="Customer Name"
                        aria-autocomplete='both' aria-haspopup="false"
/*                         autoComplete="true"
 */                        onChange={(e) =>{ 
                          const sanitizedInput = DOMPurify.sanitize(e.target.value)
                          storeData(sanitizedInput, "name")}}
                        maxLength={30}
                        value={initiateData.refundUserName}
                      />
                      {/* {initiateData.refundUserName &&
                      !initiateData.refundUserName.match(/^[A-Za-z]+$/) && (
                        <p className="text-danger">Enter Valid Name!</p>
                      )} */}
                    </Form.Group>
                  )}

                  {refundFields?.mobile && (
                    <Form.Group className="mb-3">
                      <Form.Label>Mobile number</Form.Label>
                      <div className="mobileNumberInputs">
                        <Form.Select
                          onChange={(e) =>{ 
                            const sanitizedInput = DOMPurify.sanitize(e.target.value)
                            storeData(sanitizedInput, "mobileCode")                        
                          }}
                          value={initiateData.refundUserMobileCountryCode}
                        >
                          <option value="">Code</option>
                          <option value="+91">+91</option>
                          <option value="+971">+971</option>
                          <option value="+61">+61</option>
                        </Form.Select>
                        <Form.Control
                          type="text"
                          placeholder="Mobile number"
                          aria-autocomplete='both' aria-haspopup="false"
/*                           autoComplete="true" 
 */                          value={initiateData.refundUserMobile}
                          onChange={(e) =>{ 
                            const sanitizedInput = DOMPurify.sanitize(e.target.value)
                            storeData(sanitizedInput, "mobileNumber")}}
                        />
                      </div>
                    </Form.Group>
                  )}

                  {refundFields?.email && (
                    <Form.Group className="mb-3">
                      <Form.Label>Email Address</Form.Label>
                      <Form.Control
                        type="email"
/*                         autoComplete="true"
 */                        placeholder="Email Address"
                        aria-autocomplete='both' 
                        aria-haspopup="false"
                        value={initiateData.refundUserEmail}
                        onChange={(e) => {
                          const sanitizedInput = DOMPurify.sanitize(e.target.value)
                          storeData(sanitizedInput, "email")
                        }}
                      />
                    </Form.Group>
                  )}

                  {refundFields?.digitalAmt && (
                    <Form.Group className="mb-3">
                      <Form.Label>Digital currency amount</Form.Label>
                      <Form.Control
                        type="text"
                        aria-autocomplete='both' aria-haspopup="false"
/*                         autoComplete="true"
 */                        placeholder="Digital currency amount"
                        value={rowtrx.totalDigitalCurrencyReceived}
                        disabled
                      />
                    </Form.Group>
                  )}

                  {refundFields?.digitalName && (
                    <Form.Group className="mb-3">
                      <Form.Label>Digital currency name</Form.Label>
                      <Form.Control
                        type="text"
                        aria-autocomplete='both' aria-haspopup="false"
/*                         autoComplete="true"
 */                        placeholder="Digital currency name"
                        value={rowtrx.asset}
                        disabled
                      />
                    </Form.Group>
                  )}

                  {refundFields?.refundAmountInFiat && (
                    <Form.Group className="mb-3">
                      <Form.Label>
                        Refund Amount in {rowtrx.fiatAsset}
                      </Form.Label>
                      <Form.Control
                        type="text"
                        aria-autocomplete='both' aria-haspopup="false"
/*                         autoComplete="true"
 */                        placeholder={`Refund Amount in ${rowtrx.fiatAsset}`}
                        value={initiateData.refundAmountFiat}
                        disabled
                      />
                    </Form.Group>
                  )}

                  {refundFields?.refundAmountInCrypto && (
                    <Form.Group className="mb-3">
                      <Form.Label>Refund Amount in {digitalAsset}</Form.Label>
                      <Form.Control
                        type="text"
                        aria-autocomplete='both' aria-haspopup="false"
/*                         autoComplete="true"
 */                        placeholder={`Refund Amount in ${digitalAsset}`}
                        value={renderRefundAmount()?.toFixed(8)}
                        disabled
                      />
                    </Form.Group>
                  )}

                  {refundFields?.reason && (
                    <Form.Group className="mb-3">
                      <Form.Label>Reason for Refund</Form.Label>
                      <Form.Control
                        type="text"
                        aria-autocomplete='both' aria-haspopup="false"
                        placeholder="Reason for Refund"
/*                         autoComplete="true"
 */                        value={initiateData.reasonForRefund}
                        onChange={(e) =>{ 
                          const sanitizedInput = DOMPurify.sanitize(e.target.value)
                          storeData(sanitizedInput, "refundreason")
                        }}
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
/*                         autoComplete="true" 
 */                        maxLength={WALLET_MAX_LENGTH}
                        placeholder={`Source wallet address`}
                        value={initiateData.sourceWalletAddress}
                        onChange={(e) => {
                          const sanitizedInput = DOMPurify.sanitize(e.target.value)
                          storeData(sanitizedInput, "sourceWalletAddress")
                        }}
                      />
                    </Form.Group>
                  )}

                  {refundFields?.walletAddr && (
                    <Form.Group className="mb-3">
                      <Form.Label>{digitalAsset} wallet address</Form.Label>
                      <Form.Control
                        type="text"
                        aria-autocomplete='both' aria-haspopup="false"
                        maxLength={WALLET_MAX_LENGTH}
/*                         autoComplete="true"
 */                        placeholder={`${digitalAsset} wallet address`}
                        value={initiateData.refundWalletAddress}
                        onChange={(e) => {
                          const sanitizedInput = DOMPurify.sanitize(e.target.value)
                          storeData(sanitizedInput, "walletaddress")}}
                      />
                      <Form.Text className="text-muted">
                        to be filled by customer
                      </Form.Text>
                      {/* {initiateData.refundWalletAddress === "" &&
                      (initiateData.refundWalletAddress.length < 24 ||
                        initiateData.refundWalletAddress.length > 32) && (
                        <p className="text-danger">Enter Valid Wallet Address!</p>
                      )} */}
                    </Form.Group>
                  )}

                  {refundFields?.confirmWalletAddr && (
                    <Form.Group className="mb-3">
                      <Form.Label>
                        Reconfirm {digitalAsset} wallet address
                      </Form.Label>
                      <Form.Control
                        type="text"
                        aria-autocomplete='both' aria-haspopup="false"
/*                         autoComplete="true"
 */                        maxLength={WALLET_MAX_LENGTH}
                        placeholder={`Reconfirm ${digitalAsset} wallet address`}
                        value={initiateData.refundWalletAddressConfirm}
                        onChange={(e) => {
                          const sanitizedInput = DOMPurify.sanitize(e.target.value)
                          storeData(sanitizedInput, "walletaddressconfirm")}}
                      />
                      <Form.Text className="text-muted">
                        to be filled by customer
                      </Form.Text>
                    </Form.Group>
                  )}
                  {refundFields && initiateData && (
                    <>
                      <h6
                        style={{
                          border: "2px solid black",
                          padding: "3px",
                          fontSize: "15px",
                        }}
                      >
                        <b>
                          Declaration signed by the customer to the effect that
                          (Customer to accept and sign).
                        </b>
                      </h6>
                      <ul className="list-details">
                        <li>
                          Refund will be in {digitalAsset}, credited into a{" "}
                          {digitalAsset} wallet.
                        </li>
                        <li>Gas fee will be borne by the customer.</li>
                        <li>
                          Upon verification, refund value will be processed and
                          will be credited in customer&apos;s account within 7
                          days.
                        </li>
                        <li>
                          Refund amount would be credited based on conversion
                          rate of the original crypto currency at the time of
                          execution of refund transaction with the deduction of
                          gas fee.
                        </li>
                      </ul>
                      <Form.Group
                        className="mb-3 mt-4"
                        controlId="formBasicCheckbox"
                      >
                        <div style={{ display: "flex" }}>
                          <Form.Check
                            type="checkbox"
                            label={`I accept all of the above points and the Terms and Conditions `}
                            checked={initiateData.refundTermsConditionChecked}
                            onChange={(e) =>
                              storeData(e.target.checked, "termsConditionChecked")
                            }
                          />
                        </div>
                        <div
                          style={{
                            border: "2px solid black",
                            padding: "20px",
                            fontSize: "15px",
                            textAlign: "center",
                          }}
                          className="pt-3"
                        >
                          <h6>
                            <u>
                              <b>Terms and Conditions</b>
                            </u>
                          </h6>
                          <ol style={{ textAlign: "left" }}>
                            <li key={1}>
                              Customer must ensure that this purchase meets any
                              rules and regulations, including but not limited
                              to customs, tax and security, both during the
                              journey and at the country of destination, and
                              Dubai Duty Free does not bear any responsibility
                              in this regard.
                            </li>
                            <li key={2}>
                              Return/exchange within 6 months from the date of
                              purchase with original packaging, purchase
                              receipt, and in unused condition.
                            </li>
                            <li key={3}>
                              Refund claims for payments made using digital
                              currency will be credited in USDT through a USDT
                              wallet. Upon completion of verification form sent
                              to the customer via Email or SMS.
                            </li>
                          </ol>
                        </div>
                      </Form.Group>
                      <Form.Group
                        className="mb-2"
                        style={{ display: "flex", flexDirection: "row" }}
                      >
                        <Col lg={6} md={6} sm={12} xs={12}>
                          <Form.Label>Customer&apos;s Signature:</Form.Label>
                        </Col>
                        <Form.Control /* autoComplete="true" */  type="text" disabled />
                      </Form.Group>
                      <Form.Group
                        className="mb-2"
                        style={{ display: "flex", flexDirection: "row" }}
                      >
                        <Col lg={6} md={6} sm={12} xs={12}>
                          <Form.Label>Date:</Form.Label>
                        </Col>
                        <Form.Control /* autoComplete="true" */  type="text" disabled />
                      </Form.Group>
                    </>
                  )}
                </Form>
              </Col>
            </Row>
          </Container>
        </Modal.Body>
        <Modal.Footer style={{ justifyContent: "center" }}>
          {/* <div>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleClose}
              style={{ marginRight: "10px" }}
            >
              Close
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => submitData(id)}
            >
              Submit
            </button>
          </div> */}
          <div style={{ textAlign: "center" }}>
            <button
              type="submit"
              // className={
              //   (initiateData.refundUserName &&
              //     !initiateData.refundUserName.match(/^[A-Za-z]+$/)) ||
              //   (initiateData.refundWalletAddress === "" &&
              //     (initiateData.refundWalletAddress.length < 32 ||
              //       initiateData.refundWalletAddress.length > 48))
              //     ? "wdz-btn-grey wdz-btn-md"
              //     : "wdz-btn-primary wdz-btn-md"
              // }
              className={
                customerVerificationSubmitBtn
                  ? "wdz-btn-primary wdz-btn-md"
                  : "wdz-btn-grey wdz-btn-md"
              }
              style={{ marginRight: "20px" }}
              onClick={() => submitData()}
              // disabled={
              //   (initiateData.refundUserName &&
              //     !initiateData.refundUserName.match(/^[A-Za-z]+$/)) ||
              //   (initiateData.refundWalletAddress === "" &&
              //     (initiateData.refundWalletAddress.length < 24 ||
              //       initiateData.refundWalletAddress.length > 32))
              //     ? true
              //     : false
              // }
              disabled={!customerVerificationSubmitBtn}
            >
              Submit
            </button>
            <button
              className="wdz-btn-grey wdz-btn-md"
              onClick={() => setInitiateData(resetData)}
              style={{ marginRight: "20px" }}
            >
              Clear
            </button>
            <ReactToPrint
              trigger={() => (
                <button className={"btn btn-secondary wdz-btn-md"}>
                  Print
                </button>
              )}
              content={() => printButtonRef.current}
            />
          </div>
        </Modal.Footer>
      </Modal>
    </>
  );
}

export default CustomerVeriRefundPopupModal;
