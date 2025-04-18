import React, { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { yupResolver } from "@hookform/resolvers/yup"
import { useForm } from "react-hook-form"
import { Link, useLocation, useParams } from "react-router-dom"
import Modal from "react-bootstrap/Modal"
import { p2pTransactionForm } from "src/constants/formTypes"
import { useMerchantTransaction, useP2pTransaction } from "src/api/user"
import { useValidationSchemas } from "src/constants/validationSchemas"
import { RouteType } from "src/constants/routeTypes"
import Row from "react-bootstrap/Row"
import Col from "react-bootstrap/Col"
import Card from "src/helpers/Card"
import DOMPurify from "dompurify"

interface Test {
  test?: boolean
}
const P2PTransaction: React.FC<Test> = () => {
  const { t } = useTranslation()
  const { transactionId } = useParams()
  const location = useLocation()

  const { from }: { from: string } = transactionId
    ? { from: `${RouteType.TRANSACTION_DETAIL}/${transactionId}` }
    : location.state || { from: RouteType.HOME }

  const { data: transaction } = transactionId
    ? // eslint-disable-next-line react-hooks/rules-of-hooks
      useMerchantTransaction(transactionId)
    : { data: undefined }

  const assets = [
    { label: "Bitcoin", value: "BTC" },
    { label: "Ethereum", value: "ETH" },
    { label: "USDT", value: "USDT" },
    { label: "WTK", value: "WTK" }
  ]
  const [asset, setAsset] = useState<string>("BTC")
  const [sendBy, setSendBy] = useState<string>("email")
  const [showModal, setShowModal] = useState(false)
  const { p2pSchema } = useValidationSchemas()
  const { mutate: submitP2pTransaction, error, isSuccess } = useP2pTransaction()
  const {
    getValues,
    setValue,
    reset,
    handleSubmit,
    register,
    formState: { isSubmitted, isSubmitting, errors }
  } = useForm<p2pTransactionForm>({
    resolver: yupResolver(p2pSchema)
  })

  useEffect(() => {
    if (transaction) {
      setValue("asset", transaction.asset)
    }
  }, [transaction])

  const onSubmit = () => {
    setShowModal(false)
    submitP2pTransaction({
      ...{
        amount: getValues().amount.toString(),
        asset: getValues().asset,

        description: getValues().description
      },
      ...(sendBy === "email"
        ? { receiverEmail: getValues().receiverEmail }
        : {}),
      ...(sendBy === "phone"
        ? { receiverPhone: getValues().receiverPhone }
        : {}),
      ...(sendBy === "username"
        ? { receiverUsername: getValues().receiverUsername }
        : {})
    })
  }

  const submitP2p = () => {
    setShowModal(true)
  }

  return (
    <Card>
      <Row>
        <Col md={{ span: 5, offset: 0 }}>
          <div>
            <h2>{t("Refund & Dispute")}</h2>
          </div>
          <div className="wp-form">
            {(!isSuccess || !isSubmitted) && (
              <form  autoComplete="off"  onSubmit={handleSubmit(submitP2p)} role="form" noValidate>
                {error && (
                  <div className="alert alert-danger" role="alert">
                    {error.message}
                  </div>
                )}
                <div className="form-group mt-4">
                  <label htmlFor="amount">{t("Amount")}</label>
                  <input aria-autocomplete='both' aria-haspopup="false"
                    {...register("amount")}
/*                     autoComplete="true" 
 */                    placeholder={t("Amount")}
                    data-testid="amount"
                    type="text"
                    min="0"
                    className={`form-control ${
                      errors.amount?.message ? "is-invalid" : ""
                    }`}
                    defaultValue={transaction?.amount}
                    aria-describedby="amountError"
                    onKeyPress={(event) => {
                      if (/\+|-/.test(event.key)) {
                        event.preventDefault()
                      }
                    }}
                  />
                  {errors.amount?.message && (
                    <div id="amountError" className="invalid-feedback">
                      {errors.amount?.message}
                    </div>
                  )}
                </div>
                <div className="form-group mt-4">
                  <label htmlFor="asset">{t("Token")}</label>
                  <select
                    {...register("asset")}
                    className="form-control"
                    data-testid="asset"
                    defaultValue={asset}
                    onChange={(x) => {
                      const sanitizedInput = DOMPurify.sanitize(x.target.value)
                      setAsset(sanitizedInput)
                    }}
                  >
                    {assets.map((asset) => (
                      <option key={asset.value} value={asset.value}>
                        {asset.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="form-group mt-4">
                  <label htmlFor="sendBy">{t("Send By")}</label>

                  <select
                    {...register("sendBy")}
                    placeholder={t("Send By")}
                    data-testid="sendBy"
                    className="form-control"
                    aria-describedby="sendBy"
                    onChange={(x) => {
                      const sanitizedInput = DOMPurify.sanitize(x.target.value)
                      setSendBy(sanitizedInput)
                    }}
                  >
                    <option value="email">{t("Email")}</option>
                    <option value="phone">{t("Phone")}</option>
                    <option value="username">{t("Username")}</option>
                  </select>
                </div>

                {sendBy === "email" && (
                  <div className="form-group mt-4">
                    <label htmlFor="receiverEmail">{t("Receiver Email")}</label>

                    <input aria-autocomplete='both' aria-haspopup="false"
                      {...register("receiverEmail")}
/*                       autoComplete="true" 
 */                      placeholder={t("Email")}
                      data-testid="receiverEmail"
                      type="email"
                      className={`form-control ${
                        errors.receiverEmail?.message ? "is-invalid" : ""
                      }`}
                      aria-describedby="receiverEmail"
                      defaultValue={transaction?.senderName}
                    />
                    {errors.receiverEmail?.message && (
                      <div
                        id="receiverEmailError"
                        className="invalid-feedback"
                        data-testid="receiverEmailError"
                      >
                        {errors.receiverEmail?.message}
                      </div>
                    )}
                  </div>
                )}

                {sendBy === "phone" && (
                  <div className="form-group mt-4">
                    <label htmlFor="receiverPhone">{t("Receiver Phone")}</label>

                    <input aria-autocomplete='both' aria-haspopup="false"
                      {...register("receiverPhone")}
                      placeholder={t("Phone Number")}
/*                       autoComplete="true" 
 */                      data-testid="receiverPhone"
                      type="text"
                      className={`form-control ${
                        errors.receiverPhone?.message ? "is-invalid" : ""
                      }`}
                      aria-describedby="receiverPhoneError"
                    />
                    {errors.receiverPhone?.message && (
                      <div id="receiverPhoneError" className="invalid-feedback">
                        {errors.receiverPhone?.message}
                      </div>
                    )}
                  </div>
                )}

                {sendBy === "username" && (
                  <div className="form-group mt-4">
                    <label htmlFor="receiverUsername">
                      {t("Receiver Username")}
                    </label>

                    <input aria-autocomplete='both' aria-haspopup="false"
                      {...register("receiverUsername")}
                      placeholder={t("Username")}
/*                       autoComplete="true" 
 */                      data-testid="receiverUsername"
                      type="text"
                      className={`form-control ${
                        errors.receiverUsername?.message ? "is-invalid" : ""
                      }`}
                      aria-describedby="receiverUsername"
                    />
                    {errors.receiverUsername?.message && (
                      <div
                        id="receiverUsernameError"
                        className="invalid-feedback"
                      >
                        {errors.receiverUsername?.message}
                      </div>
                    )}
                  </div>
                )}

                <div className="form-group mt-4">
                  <label htmlFor="description">{t("Description")}</label>
                  <input aria-autocomplete='both' aria-haspopup="false"
                    {...register("description")}
                    placeholder={t("Description")}
                    data-testid="description"
                    type="text"
/*                     autoComplete="true" 
 */                    className={`form-control ${
                      errors.description?.message ? "is-invalid" : ""
                    }`}
                    maxLength={160}
                    aria-describedby="description"
                  />
                  {errors.description?.message && (
                    <div id="descriptionError" className="invalid-feedback">
                      {errors.description?.message}
                    </div>
                  )}
                </div>

                <div className="col mt-4" style={{ textAlign: "center" }}>
                  <button
                    type="submit"
                    className="btn btn-primary wdz-btn-primary wdz-btn-xl"
                    disabled={isSubmitting}
                    data-bs-toggle="modal"
                    data-bs-target="#staticBackdrop"
                  >
                    Submit
                  </button>
                  {/* <Link
                    to={from}
                    data-testid="backButton"
                    className={`btn btn-secondary ${
                      isSubmitting ? "disabled" : ""
                    }`}
                    style={{ marginLeft: "2%" }}
                    role="button"
                  >
                    {t("Cancel")}
                  </Link> */}
                </div>
              </form>
            )}
            {isSuccess && isSubmitted && (
              <>
                <div
                  className="alert alert-success"
                  role="alert"
                  data-testid="success"
                >
                  {t("Transaction complete")}
                </div>
                <div className="mt-2">
                  <Link
                    to={RouteType.HOME}
                    data-testid="backButton"
                    className="btn btn-primary"
                    role="button"
                  >
                    {t("Done")}
                  </Link>
                  <button
                    data-testid="addAnotherButton"
                    className="btn btn-secondary ms-2 "
                    role="button"
                    onClick={() =>
                      reset({
                        amount: "",
                        asset: "BTC",
                        receiverEmail: "",
                        description: ""
                      })
                    }
                  >
                    {t("Make another transaction")}
                  </button>
                </div>
              </>
            )}
          </div>
          <Modal show={showModal}>
            <Modal.Body>
              {t("Are you sure you want to make this transaction?")}
            </Modal.Body>
            <Modal.Footer>
              <button
                type="submit"
                className="btn btn-primary"
                onClick={() => onSubmit()}
                data-testid="confirmButton"
              >
                {t("Confirm")}
              </button>
              <button
                className="btn btn-secondary"
                style={{ marginLeft: "2%" }}
                onClick={() => setShowModal(false)}
                data-testid="cancelConfirmButton"
              >
                {t("Cancel")}
              </button>
            </Modal.Footer>
          </Modal>
        </Col>
      </Row>
    </Card>
  )
}

export default P2PTransaction
