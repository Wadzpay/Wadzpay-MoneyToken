/* eslint-disable no-console */
import React from "react"
import { fireEvent, screen, waitFor } from "@testing-library/react"
import env from "src/env"
import { renderWithQueryClient } from "src/testUtils"
import { MemoryRouter } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"

import CreateAccount from "./CreateAccount"

const mockedNavigate = jest.fn()
jest.mock("react-router-dom", () => {
  const originalRouterDom = jest.requireActual("react-router-dom")
  return {
    __esModule: true,
    ...originalRouterDom,
    useNavigate: () => mockedNavigate
  }
})

const PHONE_NUMBER = "9999999999"

const original = console.error

beforeEach(() => {
  console.error = jest.fn()
})

afterEach(() => {
  console.error = original
})

test("renders correct default country", () => {
  renderWithQueryClient(
    <MemoryRouter>
      <CreateAccount />
    </MemoryRouter>
  )

  const countrySelectElement = screen.getByTestId<HTMLSelectElement>("country")
  expect(countrySelectElement.value).toBe(env.DEFAULT_COUNTRY)

  const phoneInputElement = screen.getByTestId<HTMLInputElement>("countryCode")
  expect(phoneInputElement.value).toBe("+91") // TODO remove this hardcoded value
})

test("submit button disabled on click", async () => {
  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <CreateAccount />
    </MemoryRouter>
  )

  const formElement = wrapper.getByRole("form")
  const buttonElement = screen.getByRole<HTMLButtonElement>("button", {
    name: "Next"
  })
  const phoneElement = screen.getByTestId<HTMLInputElement>("phoneNumberInput")

  fireEvent.change(phoneElement, { target: { value: PHONE_NUMBER } })

  expect(buttonElement.disabled).toBe(false)

  fireEvent.submit(formElement)
  const buttonAfterSubmit = await waitFor(() => buttonElement)

  expect(buttonAfterSubmit.disabled).toBe(true)
})

test("handle server error response", async () => {
  // Mock a failed response from the server
  // response.ok != true
  // Error message is returned in the response body
  const tmpFetch = fetch
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve({ message: "INVALID_PHONE_NUMBER" })
    })
  ) as jest.Mock

  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <CreateAccount />
    </MemoryRouter>
  )

  const formElement = wrapper.getByRole("form")
  const phoneElement = screen.getByTestId<HTMLInputElement>("phoneNumberInput")
  fireEvent.change(phoneElement, { target: { value: PHONE_NUMBER } })

  fireEvent.submit(formElement)
  const errorAfterSubmit = await waitFor(() =>
    screen.getByText("Provided phone number is not valid")
  )
  const buttonElement = screen.getByRole<HTMLButtonElement>("button", {
    name: "Next"
  })
  expect(errorAfterSubmit).toBeInTheDocument()

  const buttonAfterSubmit = await waitFor(() => buttonElement)
  expect(buttonAfterSubmit.disabled).toBe(false)

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

  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <CreateAccount />
    </MemoryRouter>
  )

  const formElement = wrapper.getByRole("form")
  const phoneElement = screen.getByTestId<HTMLInputElement>("phoneNumberInput")
  fireEvent.change(phoneElement, { target: { value: PHONE_NUMBER } })

  await waitFor(() => {
    fireEvent.submit(formElement)
  })

  // Check that the mocked navigate function was called
  expect(mockedNavigate).toHaveBeenCalledTimes(1)

  global.fetch = tmpFetch
})

test("show correct default country code when selected", () => {
  renderWithQueryClient(
    <MemoryRouter>
      <CreateAccount />
    </MemoryRouter>
  )

  const countrySelectElement = screen.getByTestId<HTMLSelectElement>("country")

  fireEvent.change(countrySelectElement, {
    target: { value: "ID" }
  })

  expect(countrySelectElement.value).toBe("ID")

  const phoneInputElement = screen.getByTestId<HTMLInputElement>("countryCode")
  expect(phoneInputElement.value).toBe("+62") // TODO remove this hardcoded value
})

test("mobile number must be greater than 7 characters", async () => {
  // Actual phone number is country code + phone number (e.g. +6112345678)
  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <CreateAccount />
    </MemoryRouter>
  )

  const formElement = wrapper.getByRole("form")

  const phoneElement = screen.getByTestId<HTMLInputElement>("phoneNumberInput")
  fireEvent.change(phoneElement, { target: { value: "1234" } })

  await waitFor(() => {
    fireEvent.submit(formElement)
  })
  expect(
    screen.getByText("Please enter a valid phone number")
  ).toBeInTheDocument()

  fireEvent.change(phoneElement, { target: { value: "123456" } })

  await waitFor(() => {
    fireEvent.submit(formElement)
  })
  expect(
    screen.queryByText("Please enter a valid phone number")
  ).not.toBeInTheDocument()
})

test("mobile number must be less than 15 characters", async () => {
  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <CreateAccount />
    </MemoryRouter>
  )

  const formElement = wrapper.getByRole("form")

  const phoneElement = screen.getByTestId<HTMLInputElement>("phoneNumberInput")
  fireEvent.change(phoneElement, { target: { value: "12345678911234" } })

  await waitFor(() => {
    fireEvent.submit(formElement)
  })
  expect(
    screen.getByText("Please enter a valid phone number")
  ).toBeInTheDocument()

  fireEvent.change(phoneElement, { target: { value: "1234567891123" } })

  await waitFor(() => {
    fireEvent.submit(formElement)
  })
  expect(
    screen.queryByText("Please enter a valid phone number")
  ).not.toBeInTheDocument()
})

test("cancel button takes user to the phone number page", async () => {
  renderWithQueryClient(
    <MemoryRouter>
      <CreateAccount />
    </MemoryRouter>
  )

  const linkElement = screen.getByRole<HTMLAnchorElement>("button", {
    name: "Cancel"
  })
  expect(linkElement).toBeInTheDocument()
  expect(linkElement.attributes.getNamedItem("href")?.value).toBe(
    RouteType.HOME
  )
})
