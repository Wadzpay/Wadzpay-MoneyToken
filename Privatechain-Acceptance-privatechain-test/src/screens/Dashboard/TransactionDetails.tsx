import React, {useContext, useState} from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import dayjs from "dayjs";
import { useMerchantTransaction } from "src/api/user";
import { RouteType } from "src/constants/routeTypes";
import Modal from "react-bootstrap/Modal";
import useIsEmailValid from "src/helpers/useIsEmailValid";
import useFormatCurrencyAmount from "src/helpers/formatCurrencyAmount";
import QRCode from "react-qr-code";
import Form from "react-bootstrap/Form";
import {MerchantContext} from "../../context/Merchant";

function TransactionDetail(): JSX.Element {
  const { t } = useTranslation();
  const { transactionId } = useParams() as { transactionId: string };
  const location = useLocation();
  const [showModal, setShowModal] = useState(false);
  const { from }: { from: string } = location.state || {
    from: RouteType.HOME,
  };
  const [showCopyText, setShowCopyText] = useState(false);

  const formatter = useFormatCurrencyAmount();

  const { data: transaction } = useMerchantTransaction(transactionId);
  const isEmailValid = useIsEmailValid(transaction?.senderName);
  const { merchantDetails, institutionDetails } = useContext(MerchantContext)

  const copyAddress = (address: string) => {
    navigator.clipboard.writeText(address);
    setShowCopyText(true);
    setTimeout(() => {
      setShowCopyText(false);
    }, 2000);
  };

  const dateFormat = (time: any) => {
    if (time) {
      const localStorageTime = localStorage.getItem("TimeZone");
      if (localStorageTime) {
        const timezone = JSON.parse(localStorageTime).value || "Asia/Dubai";
        return dayjs(time).tz(timezone).format("DD-MM-YYYY");
      } else {
        return dayjs(time).format("DD-MM-YYYY");
      }
    } else return "-";
  };

  const dateTimeFormat = (time: any) => {
    if (time) {
      const localStorageTime = localStorage.getItem("TimeZone");
      if (localStorageTime) {
        const timezone = JSON.parse(localStorageTime).value || "Asia/Dubai";
        return dayjs(time).tz(timezone).format("DD-MM-YYYY HH:mm:ss");
      } else {
        const timezone = "Asia/Dubai";
        return dayjs(time).tz(timezone).format("DD-MM-YYYY HH:mm:ss");
      }
    } else return "-";
  };

  const trxIdReturn = (obj: any) => {
    if (obj.description && obj.uuid == "") {
      return null;
    }

    if (obj.description && obj.uuid == null) {
      return null;
    }

    if (
      obj.description &&
      obj.description.includes("-") &&
      obj.description.length == 36
    ) {
      return obj.description;
    }

    return obj.uuid;
  };

  const digitalCurrencyReturn = (obj: any) => {
    if (obj.totalDigitalCurrencyReceived && obj.amount) {
      const sum = obj.totalDigitalCurrencyReceived - obj.amount;
      if (sum > 0) {
        return "+ " + formatter(sum, obj.asset);
      } else if (sum < 0) {
        return formatter(sum, obj.asset);
      } else if (sum == 0) {
        return formatter(sum, obj.asset);
      }
    } else {
      return 0;
    }
  };

  const digitalFiatReturn = (obj: any) => {
    if (obj.fiatAmount && obj.totalFiatReceived) {
      const sum = obj.totalFiatReceived - obj.fiatAmount;

      if (sum > 0) {
        return "+ " + formatter(sum, obj.asset);
      } else if (sum < 0) {
        return formatter(sum, obj.asset);
      } else if (sum == 0) {
        return formatter(sum, obj.asset);
      }
    } else {
      return 0;
    }
  };

  const timeFormat = (time: any) => {
    if (!time) {
      return null;
    }
    return time.replaceAll(":", "-");
  };

  function renderHashURL(tx: any) {
    if (tx?.blockchainTxId == "null" || tx?.blockchainTxId == null) {
      return "Not Available";
    }

    if (tx?.blockchainTxId) {
      // console.log(tx.asset)
      if (tx.asset == "BTC") {
        return tx.blockchainTxId;
      } else {
        return tx.blockchainTxId;
      }
    }
  }

  return (
    <>
      <div className="d-flex justify-content-between align-items-center">
        <h2>
          <strong data-testid="title">{t("Transaction Details")}</strong>
        </h2>
        {/* {transaction?.blockchainTxId == "null" ||
        transaction?.blockchainTxId == null ? null : (
          <button
            className="wdz-btn-primary wdz-btn-xl"
            onClick={() => setShowModal(true)}
          >
            <img src="/images/Invoice.svg" style={{ marginRight: "10px" }} />
            QR Code
          </button>
        )} */}
      </div>
      {transaction && (
        <>
          <div className="row mt-2">
            <div className="col-3">{t("Actual Date")}</div>
            <div className="col-9" data-testid="extPosActualDate">
              {dateFormat(transaction.extPosActualDate)}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Actual Time")}</div>
            <div className="col-9" data-testid="extPosActualTime">
              {transaction.extPosActualTime}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Transaction ID")}</div>
            <div className="col-9" data-testid="transactionId">
              {trxIdReturn(transaction)}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Merchant ID / PoS Terminal ID")}</div>
            <div className="col-9" data-testid="posTransactionId">
              {merchantDetails?.merchant?.merchantId} / {transaction.posId}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Transaction Type")}</div>
            <div className="col-9" data-testid="direction">
              {t(transaction.direction)}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Transaction Hash")}</div>
            <div className="col-9" data-testid="blockchainAddress">
              {renderHashURL(transaction)}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Order Fiat Amount")}</div>
            <div className="col-9" data-testid="fiatAmount">
              {transaction.fiatAsset} {transaction.fiatAmount.toFixed(2)}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Requested Digital Amount")}</div>
            <div className="col-9" data-testid="requestedDigitalAmount">
              {institutionDetails?.merchantCurrency ? institutionDetails?.merchantCurrency+" *" : ""}{" "}
              {formatter(transaction.requestedDigitalAmount, {
                asset: transaction.asset,
              })}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Received Digital Amount")}</div>
            <div className="col-9" data-testid="rcvdDigitalCurrency">
              {institutionDetails?.merchantCurrency ? institutionDetails?.merchantCurrency+" *" : ""}{" "}
              {formatter(transaction.totalDigitalCurrencyReceived, {
                asset: transaction.asset,
              })}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Received Fiat Amount")}</div>
            <div className="col-9" data-testid="fiatAmount">
              {transaction?.fiatAsset} {transaction?.totalFiatReceived?.toFixed(2)}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Payment Received Date")}</div>
            <div className="col-9" data-testid="paymentRecievedDate">
              {dateTimeFormat(transaction.paymentReceivedDate)}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Status")}</div>
            <div
              className={`col-9 ${
                transaction.status === "UNDERPAID"
                  ? "text-danger"
                  : "text-success"
              }`}
              data-testid="status"
            >
              {t(transaction.status)}
            </div>
          </div>
          {/* <div className="row mt-2">
            <div className="col-3">{t("Date & Time")}</div>
            <div className="col-9" data-testid="date">
              { {dayjs(transaction.createdAt).format("MMMM D, YYYY h:mm A")} }
              {dateTimeFormat(transaction.createdAt)}
            </div>
          </div> */}
          {/* <div className="row mt-2">
            <div className="col-3">{t("Sender Name")}</div>
            <div className="col-9" data-testid="senderName">
              {transaction.senderName}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Receiver Name")}</div>
            <div className="col-9" data-testid="receiverName">
              {transaction.receiverName}
            </div>
          </div> */}
          {/* <div className="row mt-2">
            <div className="col-3">{t("Token")}</div>
            <div className="col-9" data-testid="token">
              <img
                src={"/images/" + transaction.asset + ".svg"}
                alt={transaction.asset}
                width="20px"
                height="20px"
              />
              <span className="align-text-top ps-1">{transaction.asset}</span>
            </div>
          </div> */}
          {/* <div className="row mt-2">
            <div className="col-3">{t("Fiat Value")}</div>
            <div className="col-9" data-testid="fiatAmount">
              {transaction.fiatAmount} {transaction.fiatAsset}
            </div>
          </div> */}

          {/* <div className="row mt-2">
            <div className="col-3">{t("Fee Amount")}</div>
            <div className="col-9" data-testid="feeAmount">
              {formatter(transaction.feeAmount, {
                asset: transaction.asset
              })}
            </div>
          </div> */}
          {/* <div className="row mt-2">
            <div className="col-3">{t("Transaction Type")}</div>
            <div className="col-9" data-testid="transactionType">
              {t(transaction.transactionType)}
            </div>
          </div> */}
          {/* <div className="row mt-2">
            <div className="col-3">{t("Description")}</div>
            <div className="col-9" data-testid="description">
              {t(transaction.description)}
            </div>
          </div> */}
          <div className="row mt-2">
            <div className="col-3">{t("Difference in Digital Amount")}</div>
            <div className="col-9" data-testid="diffDigiAmount">
              {digitalCurrencyReturn(transaction)}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Difference In Fiat Amount")}</div>
            <div className="col-9" data-testid="diffFiatAmount">
              {digitalFiatReturn(transaction)}
            </div>
          </div>
          <div className="row mt-2">
            <div className="col-3">{t("Transaction Mode")}</div>
            <div className="col-9" data-testid="diffFiatAmount">
              {transaction.txnMode == "MERCHANT_OFFLINE"
                ? "Merchant Offline"
                : transaction.txnMode == "MERCHANT_ONLINE"
                ? "Customer and Merchant Online"
                : transaction.txnMode == "CUSTOMER_MERCHANT_ONLINE"
                ? "Customer and Merchant Online"
                : transaction.txnMode == "CUSTOMER_OFFLINE"
                ? "Customer Offline"
                : ""}
            </div>
          </div>

          <div className="mt-4">
            <Link
              to={from}
              role="button"
              className="btn btn-secondary wdz-btn-grey wdz-btn-md"
            >
              {t("Back")}
            </Link>
            {/* {transaction &&
              transaction.direction === "INCOMING" &&
              isEmailValid && (
                <Link
                  to={`${RouteType.REFUND_DISPUTE}/${transactionId}`}
                  role="button"
                  className="btn btn-primary ms-2 wdz-btn-primary"
                  data-testid="p2pButton"
                >
                  {t("Reverse Transaction")}
                </Link>
              )} */}
          </div>
          <Modal
            show={showModal}
            centered
            onEscapeKeyDown={() => setShowModal(false)}
          >
            <Modal.Body>
              <div className="Invoice-modal-head d-flex justify-content-between align-items-center">
                <h5>
                  {t("Scan")}
                  <span
                    className="wdz-font-color"
                    style={{ margin: "0 5px 0 5px" }}
                  >
                    {t("QR Code")}
                  </span>
                  {t("to make payment")}
                </h5>
                <img
                  src="/images/Cross.svg"
                  onClick={() => setShowModal(false)}
                  style={{ height: "20px" }}
                />
              </div>
              <div className="row mt-4">
                <div className="col-md-12">
                  <div className="row" style={{ textAlign: "center" }}>
                    <div className="col-12">
                      <img
                        src={"/images/" + transaction.asset + ".svg"}
                        alt={transaction.asset}
                        width="20px"
                        height="20px"
                      />
                      <span
                        className={`${
                          transaction.direction === "INCOMING"
                            ? "text-success"
                            : "text-danger"
                        }`}
                        style={{ marginLeft: "10px" }}
                      >
                        <b>
                          {transaction.direction === "OUTGOING" && "-"}
                          {formatter(transaction.totalAmount, {
                            asset: transaction.asset,
                          })}
                        </b>
                      </span>
                    </div>
                    <div className="col-12">
                      <span>{transaction.fiatAsset}</span>
                      <span style={{ marginLeft: "10px" }}>
                        <b>{transaction.fiatAmount}</b>
                      </span>
                    </div>
                  </div>
                </div>
                <div className="col-md-12 mt-4">
                  <div className="qrCodeContainer">
                    {transaction.blockchainTxId &&
                    transaction.blockchainTxId.length > 0 ? (
                      <QRCode
                        value={transaction.blockchainTxId}
                        // value="bitcoin:18G8pwXnXEn4YPDxBp7v5xNEkMvZwjjyCr?amount=0.00800519&message=Transaction%2520ID%253A%2520a1630887-59ca-4dc6-9eea-f881aa6ecd8f"
                        size={200}
                      />
                    ) : null}
                  </div>
                </div>
                <div className="col-md-12 mt-4">
                  <div style={{ textAlign: "center" }}>
                    <p>
                      {t(
                        "You have 60 seconds to complete the transaction with the currently displayed QR code"
                      )}
                    </p>
                  </div>
                </div>
              </div>
              <div className="col-md-12 mt-4">
                <Form.Group className="mb-3 col-8" style={{ margin: "0 auto" }}>
                  <Form.Label>{t("Wallet Address")}</Form.Label>
                  <div className="d-flex">
                    <Form.Control
                    autoComplete="off" type="text"
                    aria-autocomplete='both' aria-haspopup="false"
                       placeholder="Wallet Address"
                      disabled
                      className="col-md-6"
                      value={transaction.blockchainAddress || ""}
                      style={{
                        borderRight: 0,
                        borderRadius: 0,
                        width: "85%",
                      }}
                    />
                    <img
                      src="/images/Copy.svg"
                      onClick={() => copyAddress(transaction.blockchainAddress)}
                      style={{
                        padding: "10px",
                        background: "#e9ecef",
                        cursor: "pointer",
                      }}
                    />
                  </div>
                  {showCopyText ? (
                    <h6 style={{ color: "red" }} className="mt-2">
                      Copied
                    </h6>
                  ) : null}
                </Form.Group>
              </div>
            </Modal.Body>
            {/* <Modal.Footer>
              <button
                type="submit"
                className="btn btn-primary wdz-btn-primary"
                data-testid="confirmButton"
                style={{ width: "100%", height: "50px", fontSize: "20px" }}
              >
                {t("Continue")}
              </button>
            </Modal.Footer> */}
          </Modal>
        </>
      )}
    </>
  );
}

export default TransactionDetail;
