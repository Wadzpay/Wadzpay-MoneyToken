import React from "react"
import { fireEvent, screen, waitFor } from "@testing-library/react"
import { MemoryRouter } from "react-router-dom"
import { OnboardingContextWrapper, renderWithQueryClient } from "src/testUtils"
import { Auth } from "aws-amplify"

import RequestResetPasswordCode from "./RequestResetPasswordCode"

const RequestResetPasswordCodeWrapper = () => {
  return (
    <OnboardingContextWrapper>
      <MemoryRouter>
        <RequestResetPasswordCode />
      </MemoryRouter>
    </OnboardingContextWrapper>
  )
}

const mockedNavigate = jest.fn()
jest.mock("react-router-dom", () => {
  const originalRouterDom = jest.requireActual("react-router-dom")
  return {
    __esModule: true,
    ...originalRouterDom,
    useNavigate: () => mockedNavigate
  }
})

test("renders Reset Password form", () => {
  renderWithQueryClient(<RequestResetPasswordCodeWrapper />)

  const emailElement = screen.getByTestId("email")
  const passwordElement = screen.getByTestId("newPassword")
  const passwordConfirmElement = screen.getByTestId("confirmPassword")

  expect(emailElement).toBeDefined()
  expect(passwordElement).toBeDefined()
  expect(passwordConfirmElement).toBeDefined()
})

test("form displayed if phone number is present", async () => {
  renderWithQueryClient(<RequestResetPasswordCodeWrapper />)

  const linkElement = screen.getByRole("heading", { level: 2 })
  expect(linkElement.innerHTML).toBe("Reset Password")
})

test("successful Reset Password flow", async () => {
  const wrapper = renderWithQueryClient(<RequestResetPasswordCodeWrapper />)

  const formElement = wrapper.getByRole("form")
  const signInButton = wrapper.getByRole("button", { name: "Next" })

  formElement.onsubmit = jest.fn()
  await waitFor(() => {
    fireEvent.click(signInButton)
  })
  expect(formElement.onsubmit).toHaveBeenCalledTimes(1)
})

test("validation: required inputs", async () => {
  const wrapper = renderWithQueryClient(<RequestResetPasswordCodeWrapper />)
  const formElement = wrapper.getByRole("form")

  fireEvent.submit(formElement)
  const emailError = await waitFor(() =>
    screen.getByText("Please enter your email")
  )

  const passwordError = await waitFor(() =>
    screen.getByText("Please enter your password")
  )

  const passwordConfirmError = await waitFor(() =>
    screen.getByText("Please confirm your password")
  )

  expect(emailError).toBeInTheDocument()
  expect(passwordError).toBeInTheDocument()
  expect(passwordConfirmError).toBeInTheDocument()
})

test("validation: email format", async () => {
  const wrapper = renderWithQueryClient(<RequestResetPasswordCodeWrapper />)
  const formElement = wrapper.getByRole("form")
  const emailElement = screen.getByTestId("email")

  fireEvent.change(emailElement, { target: { value: "test" } })
  fireEvent.submit(formElement)

  const emailError = await waitFor(() =>
    screen.getByText("Please enter a valid email address")
  )
  expect(emailError).toBeInTheDocument()
})

test("validation: password format", async () => {
  const wrapper = renderWithQueryClient(<RequestResetPasswordCodeWrapper />)
  const formElement = wrapper.getByRole("form")
  const passwordElement = wrapper.getByTestId("newPassword")
  fireEvent.change(passwordElement, { target: { value: "1" } })
  fireEvent.submit(formElement)

  const passwordError = await waitFor(() =>
    screen.getByText(
      "Must contain 8 characters, one uppercase, one lowercase, one number and one special character"
    )
  )

  expect(passwordError).toBeInTheDocument()
})

test("validation: password confirm", async () => {
  const wrapper = renderWithQueryClient(<RequestResetPasswordCodeWrapper />)
  const formElement = wrapper.getByRole("form")
  const passwordElement = wrapper.getByTestId("newPassword")
  const passwordConfirmElement = wrapper.getByTestId("confirmPassword")
  fireEvent.change(passwordElement, { target: { value: "1" } })
  fireEvent.change(passwordConfirmElement, { target: { value: "2" } })
  fireEvent.submit(formElement)

  const passwordConfirmError = await waitFor(() =>
    screen.getByText("Passwords must match")
  )

  expect(passwordConfirmError).toBeInTheDocument()
})

test("handle server error response", async () => {
  Auth.forgotPassword = jest.fn().mockImplementation(
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
  await waitFor(() => {
    renderWithQueryClient(<RequestResetPasswordCodeWrapper />)
  })
  const formElement = screen.getByRole("form")
  const emailElement = screen.getByTestId("email")

  const passwordElement = screen.getByTestId("newPassword")
  const passwordConfirmElement = screen.getByTestId("confirmPassword")
  fireEvent.change(emailElement, { target: { value: "test@test.com" } })

  fireEvent.change(passwordElement, { target: { value: "123Aa@123" } })
  fireEvent.change(passwordConfirmElement, { target: { value: "123Aa@123" } })
  fireEvent.submit(formElement)

  const errorAfterSubmit = await waitFor(() =>
    screen.getByText("Network error")
  )
  expect(errorAfterSubmit).toBeInTheDocument()
})
