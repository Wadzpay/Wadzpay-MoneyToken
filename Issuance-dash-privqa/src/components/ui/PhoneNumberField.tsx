import React, { useEffect, useState } from "react"
import { useController, UseFormRegister } from "react-hook-form"
import { FieldControl, FieldName } from "src/constants/formTypes"
import { CreateAccountForm } from "src/constants"

type Props = {
  control: FieldControl
  errorMessage: string | undefined
  label: string
  name: FieldName
  phoneNumberPrefix: string
  register: UseFormRegister<CreateAccountForm>
}

const PhoneNumberField: React.FC<Props> = (props: Props) => {
  const { control, errorMessage, label, name, phoneNumberPrefix, register } =
    props
  const { field } = useController({ control, name })

  const splitNumberOnPrefix =
    phoneNumberPrefix &&
    field.value &&
    (field.value as string).split(phoneNumberPrefix)
  const [phoneNumber, setPhoneNumber] = useState(
    splitNumberOnPrefix && splitNumberOnPrefix?.length >= 2
      ? splitNumberOnPrefix[1]
      : ""
  )

  useEffect(() => {
    _onChange(phoneNumber)
  }, [phoneNumberPrefix])

  const _onChange = (value: string) => {
    setPhoneNumber(value)
    field.onChange(phoneNumberPrefix + value)
  }
  register("phoneNumber")

  return (
    <div className="form-group mt-4">
      <label htmlFor="phoneNumber">{label}</label>
      <div className="row">
        <div className="col-4 pe-1">
          <input
            type="text"
            value={phoneNumberPrefix}
            readOnly
            data-testid="countryCode"
            className="form-control"
          />
        </div>
        <div className="col-8 ps-1">
          <input
            data-testid="phoneNumberInput"
            type="tel"
            value={phoneNumber}
            onChange={(x) => _onChange(x.target.value)}
            className={`form-control ${errorMessage ? "is-invalid" : ""}`}
            aria-describedby="phoneNumberError"
          />
          {errorMessage && (
            <div id="phoneNumberError" className="invalid-feedback">
              {errorMessage}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default PhoneNumberField
