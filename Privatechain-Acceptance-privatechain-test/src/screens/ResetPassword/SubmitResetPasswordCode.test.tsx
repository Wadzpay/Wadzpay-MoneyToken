/* eslint-disable no-console */
import React from "react"
import { fireEvent, screen, waitFor } from "@testing-library/react"
import { OnboardingContextWrapper, renderWithQueryClient } from "src/testUtils"
import { StaticRouter } from "react-router-dom/server"
import { Auth } from "aws-amplify"

import SubmitResetPasswordCode from "./SubmitResetPasswordCode"

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

const location = {
  state: { email: "test@test.com", newPassword: "@A123456789a" }
}

const renderWrapper = (element: JSX.Element) =>
  renderWithQueryClient(
    <OnboardingContextWrapper>
      <StaticRouter location={location}>{element}</StaticRouter>
    </OnboardingContextWrapper>
  )

test("renders correct code format", () => {
  renderWrapper(<SubmitResetPasswordCode />)

  const codeElement = screen.getByTestId("codeInput")
  expect(codeElement).toBeDefined()
})

test("successful form submit flow", async () => {
  const wrapper = renderWrapper(<SubmitResetPasswordCode />)

  const formElement = wrapper.getByRole("form")
  const submitButton = wrapper.getByRole("button", { name: "Next" })

  formElement.onsubmit = jest.fn()
  await waitFor(() => {
    fireEvent.click(submitButton)
  })
  expect(formElement.onsubmit).toHaveBeenCalledTimes(1)
})

test("display error if code is not 6 digits", async () => {
  const wrapper = renderWrapper(<SubmitResetPasswordCode />)

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
  Auth.forgotPasswordSubmit = jest.fn().mockImplementation(
    (email) =>
      new Promise((resolve, reject) => {
        const userExists = "test@test.com"
        if (email === userExists) {
          return reject({
            code: "CodeMismatchException",
            name: "CodeMismatchException",
            message: "Invalid verification code provided, please try again."
          })
        }
      })
  )
  const wrapper = renderWrapper(<SubmitResetPasswordCode />)

  const formElement = wrapper.getByRole("form")
  const codeElement = wrapper.getByTestId("codeInput")

  // Send a valid number to bypass the yup validation
  fireEvent.change(codeElement, { target: { value: "123456" } })

  await fireEvent.submit(formElement)
  const errorAfterSubmit = await waitFor(() => screen.getByTestId("errorCode"))
  expect(errorAfterSubmit).toHaveTextContent(
    "Invalid verification code provided, please try again."
  )

  const buttonElement = screen.getByRole<HTMLButtonElement>("button", {
    name: "Next"
  })
  const buttonAfterSubmit = await waitFor(() => buttonElement)
  expect(buttonAfterSubmit.disabled).toBe(false)
})

test("handle server success response", async () => {
  Auth.forgotPasswordSubmit = jest.fn().mockImplementation(
    (email) =>
      new Promise((resolve) => {
        const userExists = "test@test.com"
        if (email === userExists) {
          const signedUser = {
            username: "abcdfg123",
            attributes: { email, name: "John Rambo", phone: "+460777777777" },
            signInUserSession: {
              accessToken: { jwtToken: "123456" }
            }
          }
          return resolve(signedUser)
        }
      })
  )

  const wrapper = renderWrapper(<SubmitResetPasswordCode />)

  const formElement = wrapper.getByRole("form")
  const codeElement = screen.getByTestId<HTMLInputElement>("codeInput")
  fireEvent.change(codeElement, { target: { value: "123456" } })

  await waitFor(() => {
    fireEvent.submit(formElement)
  })

  // Check that the mocked navigate function was called
  expect(mockedNavigate).toHaveBeenCalledTimes(1)
})

test("handle server error response", async () => {
  Auth.forgotPasswordSubmit = jest.fn().mockImplementation(
    (email) =>
      new Promise((resolve, reject) => {
        const userExists = "test@test.com"
        if (email === userExists) {
          return reject({
            code: "NetworkError",
            name: "NetworkError",
            message: "Network error"
          })
        }
      })
  )
  const wrapper = renderWrapper(<SubmitResetPasswordCode />)

  const formElement = wrapper.getByRole("form")
  const codeElement = wrapper.getByTestId("codeInput")

  // Send a valid number to bypass the yup validation
  fireEvent.change(codeElement, { target: { value: "123456" } })

  await fireEvent.submit(formElement)
  const errorAfterSubmit = await waitFor(() => screen.getByTestId("errorCode"))
  expect(errorAfterSubmit).toHaveTextContent("Network error")

  const buttonElement = screen.getByRole<HTMLButtonElement>("button", {
    name: "Next"
  })
  const buttonAfterSubmit = await waitFor(() => buttonElement)
  expect(buttonAfterSubmit.disabled).toBe(false)
})
