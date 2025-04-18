import React, { useState } from "react"
import { fireEvent, render, waitFor } from "@testing-library/react"
import { useForm } from "react-hook-form"
import { CreateAccountForm } from "src/constants"

import PhoneNumberField from "./PhoneNumberField"

const PREFIX = "+1"
const PHONE_NUMBER = "1234567890"

const CreateAccount = () => {
  const {
    control,
    getValues,
    register,
    formState: { errors }
  } = useForm<CreateAccountForm>()

  const clickSubmit = () => {
    setPhoneNumber(getValues().phoneNumber)
  }

  const [phoneNumber, setPhoneNumber] = useState("")

  return (
    <>
      <span>{phoneNumber}</span>
      <PhoneNumberField
        control={control}
        errorMessage={errors.phoneNumber?.message}
        label="Phone Number"
        name="phoneNumber"
        phoneNumberPrefix={PREFIX}
        register={register}
      />
      <button onClick={clickSubmit}></button>
    </>
  )
}

test("joins prefix and phone number when getValues called", async () => {
  const createAccount = render(<CreateAccount />)

  const phoneNumberField = createAccount.getByTestId("phoneNumberInput")
  const buttonElement = createAccount.getByRole("button")

  fireEvent.change(phoneNumberField, { target: { value: PHONE_NUMBER } })
  fireEvent.click(buttonElement)

  const concatNumber = await waitFor(() =>
    createAccount.getByText(PREFIX + PHONE_NUMBER)
  )
  expect(concatNumber).toBeInTheDocument()
})
