import React from "react"
import { screen } from "@testing-library/react"
import { MemoryRouter } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"
import { MerchantContextWrapper, renderWithQueryClient } from "src/testUtils"

import Dashboard from "./Dashboard"

const mockedNavigate = jest.fn()
jest.mock("react-router-dom", () => {
  const originalRouterDom = jest.requireActual("react-router-dom")
  return {
    __esModule: true,
    ...originalRouterDom,
    useNavigate: () => mockedNavigate
  }
})

// TODO replace this with a test to ensure the merchants name is displayed
test("renders dashboard title", async () => {
  const tmpFetch = global.fetch

  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve([{ name: "Test Merchant" }])
    })
  ) as jest.Mock

  renderWithQueryClient(
    <MemoryRouter>
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
            defaultRefundableFiatValue: 500,
            tnc:"Terms And Condition"
          },
          role: "test"
        }}
      >
        <Dashboard />
      </MerchantContextWrapper>
    </MemoryRouter>
  )
  //const linkElement = screen.getByRole("heading", { level: 2 })
  const linkElement = await screen.getByRole("heading", { level: 2 })

  expect(linkElement.innerHTML).toBe("Test Merchant")

  global.fetch = tmpFetch
})

test("renders M2C Transaction button", async () => {
  renderWithQueryClient(
    <MemoryRouter>
      <Dashboard />
    </MemoryRouter>
  )
  const linkElement =
    screen.getByTestId<HTMLAnchorElement>("p2pTransactionLink")
  expect(linkElement).toBeInTheDocument()
  expect(linkElement.href).toContain(RouteType.P2P_TRANSACTION)
})

test("does not render administration button if not admin", async () => {
  renderWithQueryClient(
    <MemoryRouter>
      <Dashboard />
    </MemoryRouter>
  )

  expect(
    screen.queryByTestId<HTMLAnchorElement>("adminLink")
  ).not.toBeInTheDocument()
})

/*
test("renders administration button if admin", async () => {
  renderWithQueryClient(
    <MemoryRouter>
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
            merchantId: "test"
          },
          role: "MERCHANT_ADMIN"
        }}
      >
        <Dashboard />
      </MerchantContextWrapper>
    </MemoryRouter>
  )
  const linkElement = screen.getByTestId<HTMLAnchorElement>("adminLink")
  expect(linkElement).toBeInTheDocument()
  expect(linkElement.href).toContain(RouteType.ADMIN)
})
*/
