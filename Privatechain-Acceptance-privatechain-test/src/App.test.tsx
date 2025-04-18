/* eslint-disable no-console */
import React from "react"
import { screen, waitFor } from "@testing-library/react"
import { Auth } from "aws-amplify"

import {
  MerchantContextWrapper,
  mockCurrentAuthenticatedUser,
  renderWithQueryClient
} from "./testUtils"
import App from "./App"
import { RouteType } from "./constants/routeTypes"

const tmpFetch = global.fetch

const original = console.error

beforeEach(() => {
  console.error = jest.fn()
})

afterEach(() => {
  console.error = original
})

const merchantContextWrapper = ({ children }: { children: JSX.Element }) => (
  <MerchantContextWrapper
    merchantDetails={{
      merchant: {
        name: "Test Merchant",
        countryOfRegistration: "test",
        registrationCode: "test",
        primaryContactFullName: "test",
        primaryContactEmail: "test",
        companyType: "test",
        industryType: "test",
        primaryContactPhoneNumber: "test",
        merchantId: "test",
        defaultRefundableFiatValue: 5,
        tnc:"Terms And Condition"
      },
      role: "test"
    }}
  >
    {children}
  </MerchantContextWrapper>
)

export const renderWithMerchant = (element: JSX.Element) =>
  renderWithQueryClient(merchantContextWrapper({ children: element }))

beforeEach(() => {
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve([])
    })
  ) as jest.Mock
})

afterEach(() => {
  global.fetch = tmpFetch
})

test("renders application header", async () => {
  Auth.currentUserInfo = jest.fn().mockImplementation(
    () =>
      new Promise(() => {
        return true
      })
  )
  await waitFor(() => {
    renderWithMerchant(<App />)
  })
  const linkElement = screen.getByTestId("header")
  expect(linkElement.innerHTML).toBe("Merchant Dashboard")
})

test("renders create account form on create account page", async () => {
  window.history.pushState({}, "Test page", RouteType.CREATE_ACCOUNT)
  await waitFor(() => {
    renderWithQueryClient(<App />)
  })

  const linkElement = screen.getByRole("heading", { level: 2 })
  expect(linkElement.innerHTML).toBe("Create Your Account")
})

test("renders sign in form on sign in page", async () => {
  window.history.pushState({}, "Test page", "/sign-in")
  await waitFor(() => {
    renderWithQueryClient(<App />)
  })

  const linkElement = screen.getByRole("heading", { level: 2 })
  expect(linkElement.innerHTML).toBe("Sign In")
})

test("renders M2C Transaction page", async () => {
  window.history.pushState({}, "Test page", "/m2c-transaction")
  Auth.currentAuthenticatedUser = jest
    .fn()
    .mockImplementation(mockCurrentAuthenticatedUser)
  await waitFor(() => {
    renderWithMerchant(<App />)
  })
  const headerElement = screen.getByRole("heading", { level: 2 })
  expect(headerElement.innerHTML).toBe("M2C Transaction")
})

test("renders admin page", async () => {
  window.history.pushState({}, "Test page", "/admin")
  Auth.currentAuthenticatedUser = jest
    .fn()
    .mockImplementation(mockCurrentAuthenticatedUser)
  await waitFor(() => {
    renderWithMerchant(<App />)
  })
  const headerElement = screen.getByRole("heading", { level: 2 })
  expect(headerElement.innerHTML).toBe("Administration")
})

test("renders admin users page", async () => {
  window.history.pushState({}, "Test page", "/admin/users")
  Auth.currentAuthenticatedUser = jest
    .fn()
    .mockImplementation(mockCurrentAuthenticatedUser)
  await waitFor(() => {
    renderWithMerchant(<App />)
  })
  const headerElement = screen.getByRole("heading", { level: 2 })
  expect(headerElement.innerHTML).toBe("Users")
})

test("renders admin invite user page", async () => {
  window.history.pushState({}, "Test page", "/admin/users/invite")
  Auth.currentAuthenticatedUser = jest
    .fn()
    .mockImplementation(mockCurrentAuthenticatedUser)
  await waitFor(() => {
    renderWithMerchant(<App />)
  })
  const headerElement = screen.getByRole("heading", { level: 2 })
  expect(headerElement.innerHTML).toBe("Invite User")
})

test("renders admin api keys page", async () => {
  window.history.pushState({}, "Test page", "/admin/api-keys")
  Auth.currentAuthenticatedUser = jest
    .fn()
    .mockImplementation(mockCurrentAuthenticatedUser)
  await waitFor(() => {
    renderWithMerchant(<App />)
  })
  const headerElement = screen.getByRole("heading", { level: 2 })
  expect(headerElement.innerHTML).toBe("API Keys")
})

test("renders admin generate api key page", async () => {
  window.history.pushState({}, "Test page", "/admin/api-keys/generate")
  Auth.currentAuthenticatedUser = jest
    .fn()
    .mockImplementation(mockCurrentAuthenticatedUser)
  await waitFor(() => {
    renderWithMerchant(<App />)
  })
  const headerElement = screen.getByRole("heading", { level: 2 })
  expect(headerElement.innerHTML).toBe("Generate API Key")
})

test("handle accept invitation redirect to create account", async () => {
  window.history.pushState(
    {},
    "Test page",
    "/accept-invite?email=test@example.com"
  )
  await waitFor(() => {
    renderWithMerchant(<App />)
  })
  const headerElement = screen.getByRole("heading", { level: 2 })
  expect(headerElement.innerHTML).toBe("Create Your Account")
})

test("redirect to merchant details page if no merchant exists", async () => {
  window.history.pushState({}, "Test page", "/")
  Auth.currentAuthenticatedUser = jest
    .fn()
    .mockImplementation(mockCurrentAuthenticatedUser)

  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: false,
      json: () =>
        Promise.resolve({ status: 404, message: "MERCHANT_NOT_FOUND" })
    })
  ) as jest.Mock

  await waitFor(() => {
    renderWithMerchant(<App />)
  })
  const headerElement = screen.getByRole("heading", { level: 2 })
  expect(headerElement.innerHTML).toBe("Merchant Details")
})
