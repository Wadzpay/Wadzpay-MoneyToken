import React from "react"
import { fireEvent, screen, waitFor } from "@testing-library/react"
import { MemoryRouter } from "react-router-dom"
import { OnboardingContextWrapper, renderWithQueryClient } from "src/testUtils"
import { RouteType } from "src/constants/routeTypes"

import AccountDetails from "./AccountDetails"

const AccountDetailsWrapper = ({ phoneNumber }: { phoneNumber?: string }) => {
  return (
    <OnboardingContextWrapper phoneNumber={phoneNumber}>
      <MemoryRouter>
        <AccountDetails />
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

test("renders account details form", () => {
  renderWithQueryClient(<AccountDetailsWrapper phoneNumber="+61412345678" />)

  const emailInputElement = screen.getByTestId("email")

  const passwordElement = screen.getByTestId("confirmPassword")

  const passwordConfirmElement = screen.getByTestId("newPassword")

  expect(emailInputElement).toBeDefined()
  expect(passwordElement).toBeDefined()
  expect(passwordConfirmElement).toBeDefined()
})

test("redirect to create account if phone number is missing", async () => {
  renderWithQueryClient(<AccountDetailsWrapper />)

  expect(mockedNavigate).toHaveBeenCalledWith(RouteType.CREATE_ACCOUNT)
})

test("form displayed if phone number is present", async () => {
  renderWithQueryClient(<AccountDetailsWrapper phoneNumber="+61412345678" />)

  const linkElement = screen.getByRole("heading", { level: 2 })
  expect(linkElement.innerHTML).toBe("Account Details")
})

test("successful account details flow", async () => {
  const wrapper = renderWithQueryClient(
    <AccountDetailsWrapper phoneNumber="+61412345678" />
  )

  const formElement = wrapper.getByRole("form")
  const signInButton = wrapper.getByRole("button", { name: "Next" })

  formElement.onsubmit = jest.fn()
  await waitFor(() => {
    fireEvent.click(signInButton)
  })
  expect(formElement.onsubmit).toHaveBeenCalledTimes(1)
})

test("validation: required inputs", async () => {
  const wrapper = renderWithQueryClient(
    <AccountDetailsWrapper phoneNumber="+61412345678" />
  )
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
  const wrapper = renderWithQueryClient(
    <AccountDetailsWrapper phoneNumber="+61412345678" />
  )
  const formElement = wrapper.getByRole("form")
  const emailElement = wrapper.getByTestId("email")

  fireEvent.change(emailElement, { target: { value: "test" } })
  fireEvent.submit(formElement)

  const emailError = await waitFor(() =>
    screen.getByText("Please enter a valid email address")
  )
  expect(emailError).toBeInTheDocument()
})

test("validation: password format", async () => {
  const wrapper = renderWithQueryClient(
    <AccountDetailsWrapper phoneNumber="+61412345678" />
  )
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
  const wrapper = renderWithQueryClient(
    <AccountDetailsWrapper phoneNumber="+61412345678" />
  )
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

test("prev button takes user to the phone number page", async () => {
  renderWithQueryClient(<AccountDetailsWrapper phoneNumber="+61412345678" />)

  const linkElement = screen.getByRole<HTMLAnchorElement>("button", {
    name: "Back"
  })
  expect(linkElement).toBeInTheDocument()
  expect(linkElement.href).toContain(RouteType.CREATE_ACCOUNT)
})
