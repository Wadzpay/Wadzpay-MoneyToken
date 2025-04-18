/* eslint-disable no-console */
import React from "react"
import { fireEvent, screen, waitFor } from "@testing-library/react"
import { OnboardingContextWrapper, renderWithQueryClient } from "src/testUtils"
import { BrowserRouter as Router } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"

import VerifyEmailCode from "./VerifyEmailCode"

const mockedNavigate = jest.fn()

jest.mock("react-router-dom", () => {
  const originalRouterDom = jest.requireActual("react-router-dom")
  return {
    __esModule: true,
    ...originalRouterDom,
    useNavigate: () => mockedNavigate
  }
})

const original = console.error

beforeEach(() => {
  console.error = jest.fn()
})

afterEach(() => {
  console.error = original
})

const renderWrapper = (element: JSX.Element) =>
  renderWithQueryClient(
    <OnboardingContextWrapper
      phoneNumber="+61412345678"
      accountDetails={{ email: "test@test.com" }}
    >
      <Router>{element}</Router>
    </OnboardingContextWrapper>
  )

test("renders correct code format", () => {
  const wrapper = renderWrapper(<VerifyEmailCode />)

  const codeElement = wrapper.getByTestId("codeInput")
  expect(codeElement).toBeDefined()
})

test("successful form submit flow", async () => {
  const tmpFetch = fetch
  // Mock a success response from the server
  // response.ok == true
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      text: () => ""
    })
  ) as jest.Mock

  const wrapper = renderWrapper(<VerifyEmailCode />)

  const formElement = wrapper.getByRole("form")
  const codeElement = wrapper.getByTestId("codeInput")

  fireEvent.change(codeElement, { target: { value: "123456" } })
  await waitFor(() => {
    fireEvent.submit(formElement)
  })

  expect(mockedNavigate).toHaveBeenCalledWith(RouteType.ONBOARDING_SUCCESS)

  global.fetch = tmpFetch
})

test("display error if code is not 6 digits", async () => {
  const wrapper = renderWrapper(<VerifyEmailCode />)

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
  await waitFor(() => {
    fireEvent.submit(formElement)
  })
  expect(
    screen.queryByText("The code must be 6 digits")
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
  const wrapper = renderWrapper(<VerifyEmailCode />)

  const formElement = wrapper.getByRole("form")
  const codeElement = wrapper.getByTestId("codeInput")

  // Send a valid number to bypass the yup validation
  fireEvent.change(codeElement, { target: { value: "123456" } })

  fireEvent.submit(formElement)
  const errorAfterSubmit = await waitFor(() =>
    screen.getByText("Provided data format is invalid.")
  )

  expect(errorAfterSubmit).toBeInTheDocument()

  const buttonElement = screen.getByRole<HTMLButtonElement>("button", {
    name: "Next"
  })
  const buttonAfterSubmit = await waitFor(() => buttonElement)
  expect(buttonAfterSubmit.disabled).toBe(false)

  global.fetch = tmpFetch
})

test("all data included in server request", async () => {
  let sentEmail = ""
  const tmpFetch = fetch
  global.fetch = jest.fn((url: string, options) => {
    const body = JSON.parse(options.body)
    sentEmail = body.email
  }) as jest.Mock

  const wrapper = renderWithQueryClient(
    <OnboardingContextWrapper
      phoneNumber="+61412345678"
      accountDetails={{
        email: "test@test.com",
        newPassword: "@Aa123456789",
        confirmPassword: "@Aa123456789"
      }}
    >
      <Router>
        <VerifyEmailCode />
      </Router>
    </OnboardingContextWrapper>
  )

  const formElement = wrapper.getByRole("form")
  const codeElement = wrapper.getByTestId("codeInput")

  fireEvent.change(codeElement, { target: { value: "123456" } })
  await waitFor(() => fireEvent.submit(formElement))

  expect(sentEmail).toBe("test@test.com")
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

  const wrapper = renderWrapper(<VerifyEmailCode />)

  const formElement = wrapper.getByRole("form")
  const codeElement = screen.getByTestId<HTMLInputElement>("codeInput")
  fireEvent.change(codeElement, { target: { value: "123456" } })

  await waitFor(() => {
    fireEvent.submit(formElement)
  })

  // Check that the mocked navigate function was called
  expect(mockedNavigate).toHaveBeenCalledTimes(1)

  global.fetch = tmpFetch
})

test("prev button takes user to the email details page", async () => {
  renderWrapper(<VerifyEmailCode />)

  const prevButton = screen.getByRole("button", { name: "Back" })
  expect(prevButton).toBeInTheDocument()

  fireEvent.click(prevButton)

  expect(mockedNavigate).toHaveBeenCalledWith(RouteType.ACCOUNT_DETAILS)
})
