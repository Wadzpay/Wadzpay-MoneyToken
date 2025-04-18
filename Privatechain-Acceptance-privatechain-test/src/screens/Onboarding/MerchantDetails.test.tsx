/* eslint-disable no-console */
import React from "react"
import { fireEvent, screen, waitFor } from "@testing-library/react"
import { MemoryRouter } from "react-router-dom"
import { OnboardingContextWrapper, renderWithQueryClient } from "src/testUtils"

import MerchantDetails from "./MerchantDetails"

const MerchantDetailsWrapper = ({ phoneNumber }: { phoneNumber?: string }) => {
  return (
    <OnboardingContextWrapper phoneNumber={phoneNumber}>
      <MemoryRouter>
        <MerchantDetails />
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

const original = console.error

beforeEach(() => {
  console.error = jest.fn()
})

afterEach(() => {
  console.error = original
})

test("renders merchant details form", () => {
  renderWithQueryClient(<MerchantDetailsWrapper />)

  const nameInput = screen.getByTestId("nameInput")
  const countryOfRegistrationInput = screen.getByTestId(
    "countryOfRegistrationInput"
  )
  const registrationCodeElement = screen.getByTestId("registrationCodeInput")
  const primaryContactFullNameInput = screen.getByTestId(
    "primaryContactFullNameInput"
  )
  const primaryContactEmailInput = screen.getByTestId(
    "primaryContactEmailInput"
  )
  const companyTypeInput = screen.getByTestId("companyTypeInput")
  const industryTypeInput = screen.getByTestId("industryTypeInput")
  const primaryContactPhoneNumberInput = screen.getByTestId(
    "primaryContactPhoneNumberInput"
  )

  expect(nameInput).toBeDefined()
  expect(countryOfRegistrationInput).toBeDefined()
  expect(registrationCodeElement).toBeDefined()
  expect(primaryContactFullNameInput).toBeDefined()
  expect(primaryContactEmailInput).toBeDefined()
  expect(companyTypeInput).toBeDefined()
  expect(industryTypeInput).toBeDefined()
  expect(primaryContactPhoneNumberInput).toBeDefined()
})

/* TODO: put this back in once the API can handle retrieving the merchant and user details
test("redirect to create merchant if phone number is missing", async () => {
  renderWithQueryClient(<MerchantDetailsWrapper />)

  expect(mockedNavigate).toHaveBeenCalledWith(RouteType.CREATE_ACCOUNT)
})
*/

test("form displayed if phone number is present", async () => {
  renderWithQueryClient(<MerchantDetailsWrapper phoneNumber="+61412345678" />)

  const linkElement = screen.getByRole("heading", { level: 2 })
  expect(linkElement.innerHTML).toBe("Merchant Details")
})

test("successful merchant details flow", async () => {
  const wrapper = renderWithQueryClient(
    <MerchantDetailsWrapper phoneNumber="+61412345678" />
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
    <MerchantDetailsWrapper phoneNumber="+61412345678" />
  )
  const formElement = wrapper.getByRole("form")

  fireEvent.submit(formElement)

  const nameError = await waitFor(() =>
    screen.getByText("Please enter the merchant name")
  )
  const countryError = await waitFor(() =>
    screen.getByText("Please enter the Country of Registration")
  )
  const contactNameError = await waitFor(() =>
    screen.getByText("Please enter the Primary Contact Full Name")
  )
  const emailError = await waitFor(() =>
    screen.getByText("Please enter the Primary Contact Email")
  )
  const phoneNumberError = await waitFor(() =>
    screen.getByText("Please enter the Primary Contact Phone Number")
  )

  expect(nameError).toBeInTheDocument()
  expect(countryError).toBeInTheDocument()
  expect(contactNameError).toBeInTheDocument()
  expect(emailError).toBeInTheDocument()
  expect(phoneNumberError).toBeInTheDocument()
})

test("validation: email format", async () => {
  const wrapper = renderWithQueryClient(
    <MerchantDetailsWrapper phoneNumber="+61412345678" />
  )
  const formElement = wrapper.getByRole("form")
  const emailElement = wrapper.getByTestId("primaryContactEmailInput")

  fireEvent.change(emailElement, { target: { value: "test" } })
  fireEvent.submit(formElement)

  const emailError = await waitFor(() =>
    screen.getByText("Please enter a valid email address")
  )
  expect(emailError).toBeInTheDocument()
})

test("validation: merchant already exists", async () => {
  // Mock a failed response from the server
  // response.ok != true
  // Error message is returned in the response body
  const tmpFetch = fetch
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve({ message: "MERCHANT_ALREADY_EXISTS" })
    })
  ) as jest.Mock

  const wrapper = renderWithQueryClient(
    <MerchantDetailsWrapper phoneNumber="+61412345678" />
  )

  fireEvent.change(wrapper.getByTestId("nameInput"), {
    target: { value: "test" }
  })
  fireEvent.change(wrapper.getByTestId("countryOfRegistrationInput"), {
    target: { value: "AU" }
  })
  fireEvent.change(wrapper.getByTestId("registrationCodeInput"), {
    target: { value: "test" }
  })
  fireEvent.change(wrapper.getByTestId("primaryContactFullNameInput"), {
    target: { value: "test" }
  })
  fireEvent.change(wrapper.getByTestId("primaryContactEmailInput"), {
    target: { value: "test@test.com" }
  })
  fireEvent.change(wrapper.getByTestId("primaryContactPhoneNumberInput"), {
    target: { value: "12345678" }
  })
  fireEvent.change(wrapper.getByTestId("companyTypeInput"), {
    target: { value: "test" }
  })
  fireEvent.change(wrapper.getByTestId("industryTypeInput"), {
    target: { value: "ACCOUNTING" }
  })
  fireEvent.change(wrapper.getByTestId("merchantIdeInput"), {
    target: { value: "test" }
  })

  const signInButton = wrapper.getByRole("button", { name: "Next" })
  fireEvent.click(signInButton)

  const errorAfterSubmit = await waitFor(() =>
    screen.getByText("The merchant name is already in use")
  )

  expect(errorAfterSubmit).toBeInTheDocument()
  global.fetch = tmpFetch
})
