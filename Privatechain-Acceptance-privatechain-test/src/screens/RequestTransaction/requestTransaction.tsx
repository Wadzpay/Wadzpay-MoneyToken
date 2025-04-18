import React, { useContext, useEffect, useRef, useState } from "react"
import Card from "src/helpers/Card"
import Form from "react-bootstrap/esm/Form"
import { requestTransactionForm } from "src/constants/formTypes"
import { yupResolver } from "@hookform/resolvers/yup"
import { useValidationSchemas } from "src/constants/validationSchemas"
import { useForm } from "react-hook-form"
import { useGetExchangeRate } from "src/api/onRamp"
import { Asset, FiatAsset } from "src/constants/types"
import { useRecentPayment } from "src/api/user"
import DOMPurify from "dompurify"

function RequestTransaction(): JSX.Element {
  const [inputError, setInputError] = useState("")
  const [txData, setInitiateData] = useState({
    email: "",
    phoneNo: "",
    fiatType: "SGD",
    fiatAmount: "",
    digitalCurrency: "USDT",
    digitalAmount: ""
  })

  const { mutate: recentPaymentApi, error, isSuccess } = useRecentPayment()

  const initialData = {
    email: "",
    phoneNo: "",
    fiatType: "SGD",
    fiatAmount: "",
    digitalCurrency: "USDT",
    digitalAmount: ""
  }

  const validateEmail = (email: string) => {
    const regex = /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w\w+)+$/
    if (!regex.test(email)) {
      setInputError("Invalid Email")
      return false
    } else {
      const emails = email.split("@")
      if (emails[0].length > 64) {
        setInputError("Invalid Email")
        return false
      } else if (emails[1].length > 255) {
        setInputError("Invalid Email")
        return false
      } else {
        setInputError("")
        return true
      }
    }
  }
  const [fiatAsset, setFiatAsset] = useState<FiatAsset>(
    txData.fiatType == "SGD" ? "SGD" : "AED"
  )

  const { data: exchangeRatesData } = useGetExchangeRate(fiatAsset)

  const { requestTransactionSchema } = useValidationSchemas()
  const [digitalCurrencyConversionVal, setDigitalCurrencyConversionVal] =
    useState<number>()
  const [digitalCurrencyType, setDigitalCurrencyType] = useState<Asset>("USDT")

  const {
    handleSubmit,
    getValues,
    register,
    reset,
    formState: { isSubmitted, errors }
  } = useForm<requestTransactionForm>({
    resolver: yupResolver(requestTransactionSchema)
  })

  const formRef = useRef()

  const handleReset = () => {
    setInputError("")
    setInitiateData(initialData)
  }

  const formSubmit = () => {
    setInputError("")
    if (txData.email == null || txData.email == "") {
      setInputError("Please enter Email")
      return
    } else if (!validateEmail(txData.email)) {
      return
    } else if (txData.phoneNo == null || txData.phoneNo == "") {
      setInputError("Please enter Phone Number")
      return
    } else if (!txData.phoneNo.startsWith("+")) {
      setInputError("Please enter a valid Phone Number")
      return
    } else if (txData.fiatAmount == null || txData.fiatAmount == "") {
      setInputError("Please enter Fiat Amount")
      return
    }
    recentPaymentApi({
      requesterUserName: "Customer",
      requesterEmailAddress: txData.email,
      requesterMobileNumber: txData.phoneNo,
      fiatAsset: txData.fiatType,
      fiatAmount: Number(txData.fiatAmount),
      digitalAsset: txData.digitalCurrency,
      externalOrderId: Date.now().toString()
    })
  }

  const onFiatAmntValueChange = (event: any) => {
    if (exchangeRatesData && digitalCurrencyType) {
      const dgCVal = exchangeRatesData[digitalCurrencyType]
      const digitalAmount: any = Number(event) * dgCVal
      setInitiateData({
        ...txData,
        ["fiatAmount"]: event,
        ["digitalAmount"]: digitalAmount.toFixed(8)
      })
    }
  }

  const changeFiatCurrency = (event: any) => {
    const selectedIndexFiat = event.target.options.selectedIndex
    setInitiateData({
      ...txData,
      ["fiatType"]: event.target.options[selectedIndexFiat].getAttribute("id")
    })
    setFiatAsset(event.target.options[selectedIndexFiat].getAttribute("id"))
  }

  const convertCurrency = (event: any) => {
    const selectedIndexDigital = event.target.options.selectedIndex
    setDigitalCurrencyConversionVal(Number(event.currentTarget.value))
    const digitalAmount: any =
      Number(txData.fiatAmount) * Number(event.currentTarget.value)
    setInitiateData({
      ...txData,
      ["digitalAmount"]: digitalAmount.toFixed(8),
      ["digitalCurrency"]:
        event.target.options[selectedIndexDigital].getAttribute("id")
    })
    setDigitalCurrencyType(
      event.target.options[selectedIndexDigital].getAttribute("id")
    )
  }

  useEffect(() => {
    onFiatAmntValueChange(txData.fiatAmount)
  }, [exchangeRatesData])

  return (
    <Card>
      <h3>Request Payment</h3>
      <div className="table-responsive" style={{ overflowX: "visible" }}>
        <div className="col-md-3 mt-4 ml-2">
          <Form autoComplete="off" id="request-transaction-form">
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
            {isSuccess && !inputError && !error && (
              <div
                className="alert alert-success"
                role="alert"
                onLoad={handleReset}
              >
                {"Payment Request is sent successfully"}
              </div>
            )}
            <Form.Group>
              <Form.Label>Email</Form.Label>
              <Form.Control
                type="email"
                placeholder="Email"
/*                 autoComplete="true"
 */                maxLength={320}
                onChange={(e) => {
                  const sanitizedInput = DOMPurify.sanitize(e.target.value)
                  setInitiateData({ ...txData, email: sanitizedInput })
                }}
                value={txData.email}
              />
            </Form.Group>
          </Form>
        </div>
        <div className="col-md-3 mt-4 ml-2">
          <Form>
            <Form.Group>
              <Form.Label>Phone No</Form.Label>
              <Form.Control
                type="text"
                aria-autocomplete='both' aria-haspopup="false"
                placeholder="Phone No"
                autoComplete="true"
                onChange={(e) => {
                  const sanitizedInput = DOMPurify.sanitize(e.target.value)
                  setInitiateData({ ...txData, phoneNo: sanitizedInput })
                }}
                value={txData.phoneNo}
              />
            </Form.Group>
          </Form>
        </div>
        <div className="col-md-3 mt-4 ml-2">
          <Form>
            <Form.Group>
              <Form.Label>Fiat Type</Form.Label>
              <Form.Select onChange={changeFiatCurrency}>
                <option value="AED" id="AED">
                  AED
                </option>
                <option value="SGD" id="SGD" selected>
                  SGD
                </option>
              </Form.Select>
            </Form.Group>
          </Form>
        </div>
        <div className="col-md-3 mt-4 ml-2">
          <Form autoComplete="off">
            <Form.Group>
              <Form.Label>Fiat Amount</Form.Label>
              <Form.Control
                type="number"
                aria-autocomplete='both' aria-haspopup="false"
                step="0.01"
/*                 autoComplete="true"
 */                value={txData.fiatAmount}
                placeholder="Fiat Amount"
                onChange={(event) => {
                  const sanitizedInput = DOMPurify.sanitize(event.target.value)
                  onFiatAmntValueChange(sanitizedInput)}}
              />
            </Form.Group>
          </Form>
        </div>
        <div className="col-md-3 mt-4 ml-2">
          <Form onSubmit={formSubmit} role="form">
            <Form.Group>
              <Form.Label>Digital Currency</Form.Label>
              <Form.Select onChange={convertCurrency}>
                {exchangeRatesData !== undefined
                  ? Object.entries(exchangeRatesData!)!.map(
                      (exRateKey: any) => (
                        <option
                          key={exRateKey[0]}
                          id={exRateKey[0]}
                          value={exRateKey[1]}
                          selected
                        >
                          {exRateKey[0]}
                        </option>
                      )
                    )
                  : ""}
              </Form.Select>
            </Form.Group>
          </Form>
        </div>
        <div className="col-md-3 mt-4 ml-2">
          <Form id="request-transaction-form">
            <Form.Group>
              <Form.Label>Digital Amount</Form.Label>
              <Form.Control
                type="number"
                aria-autocomplete='both' aria-haspopup="false"
/*                 autoComplete="true" 
 */                disabled
                value={txData.digitalAmount}
                placeholder="Digital Amount"
              />
            </Form.Group>
          </Form>
        </div>
        <div className="form-group row row-cols-auto">
          <div className="col mt-4">
            <button
              className="btn btn-secondary wdz-btn-grey wdz-btn-lg"
              role="button"
              onClick={() => handleReset()}
            >
              Reset
            </button>
          </div>
          <div className="col mt-4">
            <button
              type="submit"
              className="btn wdz-btn-primary"
              onClick={() => formSubmit()}
              // disabled={isLoading}
            >
              Request
            </button>
          </div>
        </div>
      </div>
    </Card>
  )
}

export default RequestTransaction
