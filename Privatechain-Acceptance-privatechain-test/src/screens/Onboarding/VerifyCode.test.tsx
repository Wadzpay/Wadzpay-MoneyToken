/* eslint-disable no-console */
import React from "react"
import { fireEvent, screen, waitFor } from "@testing-library/react"
import { MemoryRouter } from "react-router-dom"
import { renderWithQueryClient } from "src/testUtils"
import { useVerifyPhoneOTP, useSendPhoneOTP } from "src/api/otp"

import VerifyCode from "./VerifyCode"

const original = console.error

beforeEach(() => {
  console.error = jest.fn()
})

afterEach(() => {
  console.error = original
})

const VerifyCodeHelper = ({ onSuccess }: { onSuccess?: () => void }) => {
  const useVerifyCode = useVerifyPhoneOTP()
  const useRequestCode = useSendPhoneOTP()

  return (
    <VerifyCode
      title="Test verify code"
      timerMinutes={0}
      extraParams={{ phoneNumber: "+123456789" }}
      onSuccess={onSuccess || (() => null)}
      useVerifyCode={useVerifyCode}
      useRequestCode={useRequestCode}
    />
  )
}

test("display error if code is not 6 digits", async () => {
  const wrapper = renderWithQueryClient(<VerifyCodeHelper />)
  const formElement = wrapper.getByRole("form")
  const codeElement = wrapper.getByTestId("codeInput")

  // Empty code display error
  fireEvent.change(codeElement, { target: { value: "" } })
  fireEvent.submit(formElement)
  expect(
    await waitFor(() => screen.getByText("Please enter verification code"))
  ).toBeInTheDocument()

  // Text display error
  fireEvent.change(codeElement, { target: { value: "test" } })
  fireEvent.submit(formElement)

  // Correct length text display error
  expect(
    await waitFor(() => screen.getByText("The code must be 6 digits"))
  ).toBeInTheDocument()

  // 5 digit code display error
  fireEvent.change(codeElement, { target: { value: "test56" } })
  fireEvent.submit(formElement)
  expect(
    await waitFor(() => screen.getByText("The code must be 6 digits"))
  ).toBeInTheDocument()

  // 7 digit code display error
  fireEvent.change(codeElement, { target: { value: "12345" } })
  fireEvent.submit(formElement)
  expect(
    await waitFor(() => screen.getByText("The code must be 6 digits"))
  ).toBeInTheDocument()

  // 6 digit code success
  fireEvent.change(codeElement, { target: { value: "123456" } })
  await waitFor(() => fireEvent.submit(formElement))
  expect(
    await waitFor(() => screen.queryByText("The code must be 6 digits"))
  ).not.toBeInTheDocument()
})

test("handle server error response", async () => {
  // Mock a failed response from the server
  // response.ok != true
  // Error message is returned in the response body
  const tmpFetch = fetch
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve({ message: "INVALID_INPUT_FORMAT" })
    })
  ) as jest.Mock

  const wrapper = renderWithQueryClient(<VerifyCodeHelper />)

  const formElement = wrapper.getByRole("form")
  const codeElement = wrapper.getByTestId("codeInput")

  // Send a valid number to bypass the yup validation
  fireEvent.change(codeElement, { target: { value: "123456" } })

  fireEvent.submit(formElement)
  const errorAfterSubmit = await waitFor(() => screen.getByTestId("errorCode"))
  expect(errorAfterSubmit).toHaveTextContent("Provided data format is invalid")

  const buttonElement = screen.getByRole<HTMLButtonElement>("button", {
    name: "Next"
  })
  const buttonAfterSubmit = await waitFor(() => buttonElement)
  expect(buttonAfterSubmit.disabled).toBe(false)

  global.fetch = tmpFetch
})

test("phone number included in server request", async () => {
  let sentPhoneNumber = ""
  const tmpFetch = fetch
  global.fetch = jest.fn((url: string, options) => {
    const body = JSON.parse(options.body)
    sentPhoneNumber = body.phoneNumber
  }) as jest.Mock

  const wrapper = renderWithQueryClient(<VerifyCodeHelper />)
  const formElement = wrapper.getByRole("form")
  const codeElement = wrapper.getByTestId("codeInput")

  fireEvent.change(codeElement, { target: { value: "123456" } })
  await waitFor(() => fireEvent.submit(formElement))

  expect(sentPhoneNumber).toBe("+123456789")
  global.fetch = tmpFetch
})

test("handle server success response", async () => {
  const tmpFetch = fetch
  // Mock a success response from the server
  // response.ok == true
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      text: () => ""
    })
  ) as jest.Mock
  const mockedOnSuccess = jest.fn()

  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <VerifyCodeHelper onSuccess={mockedOnSuccess} />
    </MemoryRouter>
  )

  const formElement = wrapper.getByRole("form")
  const codeElement = screen.getByTestId<HTMLInputElement>("codeInput")
  fireEvent.change(codeElement, { target: { value: "123456" } })

  await waitFor(() => {
    fireEvent.submit(formElement)
  })

  // Check that the mocked navigate function was called
  expect(mockedOnSuccess).toHaveBeenCalledTimes(1)

  global.fetch = tmpFetch
})

test("Resend OTP", async () => {
  const wrapper = renderWithQueryClient(<VerifyCodeHelper />)

  await new Promise((r) => setTimeout(r, 2000))
  expect(wrapper.getByTestId("resendOTP")).toBeInTheDocument()
})
