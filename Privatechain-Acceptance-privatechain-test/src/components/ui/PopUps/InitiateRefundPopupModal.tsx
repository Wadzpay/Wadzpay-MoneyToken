import React, { useState, useEffect, useContext, useRef } from "react";
import { useTranslation } from "react-i18next";
import { Button, Dropdown } from "react-bootstrap";
import Modal from "react-bootstrap/Modal";
import Form from "react-bootstrap/Form";
import { useNavigate } from "react-router-dom";
import { MerchantContext } from "src/context/Merchant";
import { RouteType } from "src/constants/routeTypes";
import { useInitiateWebLinkRefund } from "src/api/user";
import { useSubmitRefundformWithAuth } from "src/api/user";
import env, { ENV } from "src/env";
import BlockUI from "src/helpers/BlockUI";
import { useGetExchangeRate } from "src/api/onRamp";
import { REFUND_AMOUNT_LIMIT } from "src/constants/Defaults";
import {
  WALLET_MAX_LENGTH,
  WALLET_MIN_LENGTH,
  WALLET_REGEX,
} from "src/constants/Defaults";

import CustomerVeriRefundPopupModal from "./CustomerVeriRefundPopupModal";
import DOMPurify from "dompurify";

function InitiateRefundPopupModal(props: any) {
  const { merchantDetails } = useContext(MerchantContext);
  const { txt, id, color, disabled, rowtrx, apicallstatus } = props;
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [show, setShow] = useState(false);
  const [showCustomerForm, setShowCustomerForm] = useState(false);
  const [loaderShow, setLoaderShow] = useState(false);
  const [formErrorMessage, setFormErrorMessage] = useState("");
  const [refundAmountLimit, setRefundAmountLimit] =
    useState(REFUND_AMOUNT_LIMIT);
  const [initiateData, setInitiateData] = useState({
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
          : rowtrx.refundUserMobile.includes("+966")
          ? rowtrx.refundUserMobile.split("+966")[1]
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
          : rowtrx.refundUserMobile.includes("+966")
          ? "+966"
          : ""
        : "",
    refundUserEmail:
      rowtrx.refundStatus === "REFUND_CANCELED" ||
      rowtrx.refundStatus === "REFUND_FAILED" ||
      rowtrx.refundStatus === "REFUND_EXPIRED"
        ? rowtrx.refundUserEmail == null
          ? ""
          : rowtrx.refundUserEmail
        : "",
    totalFiatReceived: rowtrx.totalFiatReceived,
    refundAmountFiat:
      rowtrx.refundStatus == "REFUND_EXPIRED" ||
      rowtrx.refundStatus == "REFUND_CANCELED" ||
      rowtrx.refundStatus == "REFUND_FAILED"
        ? rowtrx.refundFiatAmount
        : rowtrx.balanceAmountFiat > 0
        ? rowtrx.balanceAmountFiat
        : rowtrx.totalFiatReceived,
    balanceAmountFiat: "",
    refundMode: "WALLET",
    sourceWalletAddress: rowtrx.sourceWalletAddress,
    refundWalletAddress: rowtrx.refundWalletAddress,
    refundUserName: rowtrx.refundUserName,
  });
  const [serverErrorMessage, setServerErrorMessage] = useState<string>();

  const {
    mutate: submitInitiateRefundTransaction,
    error,
    isSuccess,
  } = useSubmitRefundformWithAuth();

  const digitalAsset =
    env.TYPE == ENV.DEV || env.TYPE == ENV.TESTING ? "SART" : "SART";
  const { data: exchangeRatesData, isFetching: isFetchingExchangeRates } =
    useGetExchangeRate(rowtrx.fiatAsset);

  const inputPhoneCodeRef = useRef<HTMLSelectElement>(null);
  const inputPhoneRef = useRef<HTMLInputElement>(null);
  const inputEmailRef = useRef<HTMLInputElement>(null);

  /*  useEffect(() => {
    if (merchantDetails?.merchant) {
      setRefundAmountLimit(merchantDetails.merchant.defaultRefundableFiatValue)
    }
  }, [merchantDetails]) */

  useEffect(() => {
    if (isSuccess) {
      setLoaderShow(true);
      setTimeout(() => {
        setShow(false);
        apicallstatus();
      }, 2000);
    }
  }, [isSuccess]);

  useEffect(() => {
    if (error?.message) {
      console.log(error)
      setLoaderShow(false);
      setServerErrorMessage(error?.message);
    }
  }, [error]);

  useEffect(() => {
    if (rowtrx) {
      if (
        rowtrx.totalFiatReceived &&
        rowtrx.totalFiatReceived > refundAmountLimit
      ) {
        setInitiateData({ ...initiateData, refundMode: "WALLET" });
      }
    }
  }, [rowtrx]);

  const handleClose = () => {
    setInitiateData({
      ...initiateData,
      refundAmountFiat:
        rowtrx.refundStatus == "REFUND_CANCELED" ||
        rowtrx.refundStatus == "REFUND_FAILED" ||
        rowtrx.refundStatus === "REFUND_EXPIRED"
          ? rowtrx.refundFiatAmount
          : rowtrx.totalFiatReceived,
      refundUserEmail:
        rowtrx.refundStatus === "REFUND_CANCELED" ||
        rowtrx.refundStatus === "REFUND_FAILED" ||
        rowtrx.refundStatus === "REFUND_EXPIRED"
          ? rowtrx.refundUserEmail == null
            ? ""
            : rowtrx.refundUserEmail
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
            : rowtrx.refundUserMobile.includes("+966")
            ? rowtrx.refundUserMobile.split("+966")[1]
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
            : rowtrx.refundUserMobile.includes("+966")
            ? "+966"
            : ""
          : "",
    });
    setShow(false);
    setServerErrorMessage("");
  };
  const handleShow = () => setShow(true);

  function renderRefundAmount() {
    if (exchangeRatesData && initiateData) {
      const value =
        exchangeRatesData[digitalAsset] * initiateData.refundAmountFiat;
      const finalVal = value.toFixed(8);
      return parseFloat(finalVal);
    }
  }

  function submitData() {

    const pattern = /^[A-Za-z ]+$/;
    if (initiateData.refundUserName !="") {
      if(!pattern.test(initiateData.refundUserName)) {
      setFormErrorMessage("Enter Valid Customer Name");
      return;
      }
    }
    // console.log(id)
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
    if (initiateData.refundAmountFiat == 0) {
      setFormErrorMessage("Refund amount should be greater than 0");
      setTimeout(() => {
        setFormErrorMessage("");
      }, 2000);
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
    
    if (initiateData.sourceWalletAddress != initiateData.refundWalletAddress) {
      setFormErrorMessage("Non Genguine Transaction, Please enter valid wallet address");
      return;
    }

    // const obj = {
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
    //   refundDigitalType: rowtrx.asset,
    //   refundFiatType: rowtrx.fiatAsset,
    //   refundAmountFiat: initiateData.refundAmountFiat,
    //   transactionId: rowtrx.uuid,
    //   refundMode: initiateData.refundMode,
    //   refundCustomerFormUrl:
    //     window.location.protocol + "//" + window.location.hostname + "/",
    //   refundAmountDigital: renderRefundAmount() || 0
    // }
    // console.log(obj)
    if (rowtrx.totalFiatReceived <= refundAmountLimit) {
      setLoaderShow(true);
      submitInitiateRefundTransaction({
        refundDigitalType: "SART",
        refundFiatType: rowtrx.fiatAsset,
        refundAmountFiat: initiateData.refundAmountFiat,
        balanceAmountFiat:
          initiateData.totalFiatReceived - initiateData.refundAmountFiat,
        transactionId: rowtrx.uuid,
        refundMode: initiateData.refundMode,
        refundAmountDigital: initiateData.refundAmountFiat,
        isReInitiateRefund:
          rowtrx.refundStatus == "REFUND_CANCELED" ||
          rowtrx.refundStatus == "REFUND_FAILED"
            ? true
            : false,
        refundTransactionID:
          rowtrx.refundStatus == "REFUND_CANCELED" ||
          rowtrx.refundStatus == "REFUND_FAILED"
            ? rowtrx.refundTransactionID
            : null,
        sourceWalletAddress: initiateData.sourceWalletAddress,
        refundWalletAddress: initiateData.refundWalletAddress,
        refundUserName: initiateData.refundUserName,
        refundUserEmail: initiateData.refundUserEmail,
        refundUserMobile:
          initiateData.refundUserMobileCountryCode +
            initiateData.refundUserMobile ===
          ""
            ? null
            : initiateData.refundUserMobileCountryCode +
              initiateData.refundUserMobile,
        reasonForRefund: "",
      });
    } else {
      setLoaderShow(true);
      submitInitiateRefundTransaction({
        /*refundUserMobile:
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
        refundDigitalType: rowtrx.asset,
        refundFiatType: rowtrx.fiatAsset,
        refundAmountFiat: initiateData.refundAmountFiat,
        balanceAmountFiat:
          initiateData.totalFiatReceived - initiateData.refundAmountFiat,
        transactionId: rowtrx.uuid,
        refundMode:
          initiateData.refundAmountFiat <= refundAmountLimit
            ? "CASH"
            : "WALLET",
        refundCustomerFormUrl:
          window.location.protocol + "//" + window.location.hostname + "/",
        refundAmountDigital: renderRefundAmount() || 0,
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
            ? rowtrx.refundTransactionID.toString()
            : null,
        sourceWalletAddress: initiateData.sourceWalletAddress,
        refundWalletAddress: initiateData.refundWalletAddress*/

        refundDigitalType: "SART",
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
        refundWalletAddress: initiateData.refundWalletAddress,
        refundAmountDigital: initiateData.refundAmountFiat,
        reasonForRefund: "refund",
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
  }

  function storeData(val: any, input: any) {
    switch (input) {
      case "name":
        setInitiateData({
          ...initiateData,
          refundUserName: val,
        });
        break;
      case "refundAmountFiat":
        console.log(val);
        const amount = val.indexOf(".") >= 0
           ? val.substr(0, val.indexOf(".")) +
           val.substr(val.indexOf("."), 3)
        : val
        if (!isNaN(amount)) {
          setInitiateData({
            ...initiateData,
            refundAmountFiat: amount,
          });
        }
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
      case "sourceWalletAddress":
        setInitiateData({
          ...initiateData,
          sourceWalletAddress: val,
        });
        break;
      case "walletaddress":
        setInitiateData({
          ...initiateData,
          refundWalletAddress: val,
        });
        break;
      default:
        break;
    }
  }

  const clearData = () => {
    if (
      rowtrx.refundStatus == "REFUND_CANCELED" ||
      rowtrx.refundStatus == "REFUND_FAILED" ||
      rowtrx.refundStatus === "REFUND_EXPIRED"
    ) {
      return "";
    } else {
      setInitiateData({
        ...initiateData,
        refundAmountFiat: "",
        refundUserMobileCountryCode: "",
        refundUserMobile: "",
        refundUserEmail: "",
        refundWalletAddress:"",
        refundUserName:""
      });
    }

    if (inputPhoneCodeRef?.current) {
      if (
        rowtrx.refundStatus == "REFUND_CANCELED" ||
        rowtrx.refundStatus == "REFUND_FAILED" ||
        rowtrx.refundStatus === "REFUND_EXPIRED"
      ) {
        return "";
      } else {
        inputPhoneCodeRef.current.value = "";
      }
    }
    if (inputPhoneRef?.current) {
      if (
        rowtrx.refundStatus == "REFUND_CANCELED" ||
        rowtrx.refundStatus == "REFUND_FAILED" ||
        rowtrx.refundStatus === "REFUND_EXPIRED"
      ) {
        return "";
      } else {
        inputPhoneRef.current.value = "";
      }
    }
    if (inputEmailRef?.current) {
      if (
        rowtrx.refundStatus == "REFUND_CANCELED" ||
        rowtrx.refundStatus == "REFUND_FAILED" ||
        rowtrx.refundStatus === "REFUND_EXPIRED"
      ) {
        return "";
      } else {
        inputEmailRef.current.value = "";
      }
    }
  };

  return (
    <>
      <button
        className={`${color}`}
        onClick={handleShow}
        disabled={disabled}
        style={{ pointerEvents: "auto", width: "120px" }}
        data-title={
          disabled &&
          (rowtrx.refundStatus === "REFUND_CANCELED"
            ? rowtrx.refundAcceptanceComment
            : null)
        }
      >
        {t(txt)}
      </button>

      <Modal show={show} onHide={handleClose} centered backdrop="static">
        <BlockUI blocking={loaderShow} title="submitting" />
        <Modal.Header closeButton>
          <Modal.Title>Refund Form</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {serverErrorMessage && (
            <div className="alert alert-danger" role="alert">
              {serverErrorMessage}
            </div>
          )}
          {initiateData.refundAmountFiat > rowtrx.balanceAmountFiat && (
            <div className="alert alert-danger" role="alert">
              Refund Amount should not be more than received amount
            </div>
          )}
          <Form  autoComplete="off" >
            <Form.Group className="mb-3">
              <Form.Label>Transaction Reference</Form.Label>
              <Form.Control type="text" autoComplete="true"  value={rowtrx.uuid} disabled={true} />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Customer Name</Form.Label>
              <Form.Control
                type="text"
                aria-autocomplete='both' aria-haspopup="false"
                placeholder="Customer Name"
                /* autoComplete="true"  */
                onChange={(e) =>{
                  const sanitizedInput = DOMPurify.sanitize(e.target.value)
                 storeData(sanitizedInput, "name")}}
                maxLength={30}
                value={initiateData.refundUserName}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Source Wallet Address</Form.Label>
              <Form.Control
                type="text"
                aria-autocomplete='both' aria-haspopup="false"
/*                 autoComplete="true"
 */                maxLength={220}
                placeholder={`Source wallet address`}
                value={rowtrx.sourceWalletAddress} 
                disabled={true}
                onChange={(e) => {
                  const sanitizedInput = DOMPurify.sanitize(e.target.value)
                  storeData(sanitizedInput, "sourceWalletAddress")}}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Refund Wallet Address</Form.Label>
              <Form.Control
                type="text"
                aria-autocomplete='both' aria-haspopup="false"
/*                 autoComplete="true"
 */                maxLength={200}
                placeholder={`refund wallet address`}
                onChange={(e) => {
                  const sanitizedInput = DOMPurify.sanitize(e.target.value)
                  storeData(sanitizedInput, "walletaddress")}}
                value={initiateData.refundWalletAddress}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              {formErrorMessage && (
                <div
                  className="alert alert-danger"
                  role="alert"
                  style={{ textAlign: "center" }}
                >
                  {formErrorMessage}
                </div>
              )}
              {/* <Form.Label>Refund Amount in {rowtrx.fiatAsset}</Form.Label>
              <Form.Control
                type="text"
                placeholder={`Refund Amount in ${rowtrx.fiatAsset}`}
                value={initiateData.refundAmountFiat}
                onChange={(e) => storeData(e, "refundAmount")}
              /> */}

              <Form.Label>
                Maximum Refund Eligibility in{" "}
                {rowtrx.fiatAsset && rowtrx.fiatAsset}
              </Form.Label>
              <Form.Control
                type="text"
                aria-autocomplete='both' aria-haspopup="false"
                placeholder={`Received Fiat Amount in ${rowtrx?.fiatAsset}`}
                value={
                  rowtrx?.balanceAmountFiat > 0
                    ? rowtrx?.balanceAmountFiat
                    : rowtrx?.totalFiatReceived
                }
/*                 autoComplete="true"
 */                onChange={(e) => {
                  const sanitizedInput = DOMPurify.sanitize(e.target.value)
                  storeData(sanitizedInput, "refundAmount")}}
                disabled={true}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>
                Refund Amount in {rowtrx.fiatAsset && rowtrx.fiatAsset}
              </Form.Label>
              <Form.Control
                type="text"
                aria-autocomplete='both' aria-haspopup="false"
                placeholder={`Refund Amount in ${rowtrx?.fiatAsset}`}
                value={initiateData.refundAmountFiat}
                disabled={
                  rowtrx.refundStatus == "REFUND_EXPIRED" ||
                  rowtrx.refundStatus === "REFUND_CANCELED" ||
                  rowtrx.refundStatus === "REFUND_FALIED"
                    ? true
                    : false
                }
/*                 autoComplete="true" 
 */                // value={rowtrx.refundAmount}
                onChange={(e) =>{
                  const sanitizedInput = DOMPurify.sanitize(e.target.value)
                 storeData(sanitizedInput, "refundAmountFiat")}}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Refund Mode</Form.Label>
              <Form.Control
                type="text"
                aria-autocomplete='both' aria-haspopup="false"
                placeholder="Refund Mode"
                value={"wallet"}
                disabled
/*                 autoComplete="true" 
 */              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>
                Balance Fiat Amount in {rowtrx.fiatAsset && rowtrx.fiatAsset}
              </Form.Label>
              <Form.Control
                type="text"
                aria-autocomplete='both' aria-haspopup="false"
/*                 autoComplete="true"
 */                value={
                  rowtrx?.balanceAmountFiat - initiateData.refundAmountFiat >= 0
                    ? (
                        rowtrx.balanceAmountFiat - initiateData.refundAmountFiat
                      ).toFixed(2)
                    : ""
                }
                disabled
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>No. of Times Refund Done</Form.Label>
              <Form.Control
                type="text"
                aria-autocomplete='both' aria-haspopup="false"
/*                 autoComplete="true" 
 */                placeholder={`No. of Times Refund Done`}
                value={rowtrx.numberOfRefunds && rowtrx.numberOfRefunds}
                disabled
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Refund Type</Form.Label>
              <Form.Control
                type="text"
                aria-autocomplete='both' aria-haspopup="false"
/*                 autoComplete="true" 
 */                placeholder="Refund Type"
                value={
                  initiateData.refundAmountFiat == rowtrx.totalFiatReceived
                    ? "Full"
                    : "Partial"
                }
                disabled
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Mobile number</Form.Label>
              <div className="mobileNumberInputs">
                <Form.Select
/*                 autoComplete="true"
 */                  defaultValue={initiateData.refundUserMobileCountryCode}
                  onChange={(e) => {
                    const sanitizedInput = DOMPurify.sanitize(e.target.value)
                    storeData(sanitizedInput, "mobileCode")}}
                  ref={inputPhoneCodeRef}
                >
                  <option value="">Code</option>
                  <option value="+91">+91</option>
                  <option value="+971">+971</option>
                  <option value="+61">+61</option>
                  <option value="+966">+966</option>
                </Form.Select>
                <Form.Control
                  ref={inputPhoneRef}
                  type="text"
                  aria-autocomplete='both' aria-haspopup="false"
/*                   autoComplete="true"
 */                  value={initiateData.refundUserMobile}
                  placeholder="Mobile number"
                  onChange={(e) => {
                    const sanitizedInput = DOMPurify.sanitize(e.target.value)
                    storeData(sanitizedInput, "mobileNumber")}}
                />
              </div>
            </Form.Group>
            {/* <div>OR</div> */}
            <Form.Group className="mb-3">
              <Form.Label>Email Address</Form.Label>
              <Form.Control
                ref={inputEmailRef}
                type="email"
                value={initiateData.refundUserEmail}
                placeholder="Email Address"
                onChange={(e) => {
                  const sanitizedInput = DOMPurify.sanitize(e.target.value)
                  storeData(sanitizedInput, "email")}}
              />
            </Form.Group>
            {/* <div>OR</div> */}
            <Form.Group
              className="mb-3"
              style={{ justifyContent: "space-between", display: "flex" }}
            >
              <div>
                <button
                  type="button"
                  className="btn btn-primary"
                  disabled={
                    initiateData.refundAmountFiat > rowtrx.totalFiatReceived
                  }
                  onClick={() => submitData()}
                >
                  Submit
                </button>
                <button
                  type="button"
                  className="btn btn-secondary ms-3"
                  onClick={() => clearData()}
                >
                  Clear
                </button>
              </div>
            </Form.Group>
          </Form>
        </Modal.Body>
      </Modal>
      {showCustomerForm ? (
        <CustomerVeriRefundPopupModal
          rowtrx={props.rowtrx}
          showhide={() => setShowCustomerForm(false)}
          refundableFiatAmount={initiateData.refundAmountFiat}
        />
      ) : null}
    </>
  );
}

export default InitiateRefundPopupModal;
