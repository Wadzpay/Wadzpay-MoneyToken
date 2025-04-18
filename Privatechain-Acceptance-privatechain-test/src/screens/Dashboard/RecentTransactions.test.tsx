/* eslint-disable no-console */
import React from "react"
import { fireEvent, waitFor } from "@testing-library/react"
import { Auth } from "aws-amplify"
import { MemoryRouter } from "react-router-dom"
import {
  mockCurrentAuthenticatedUser,
  renderWithQueryClient
} from "src/testUtils"
import { RouteType } from "src/constants/routeTypes"

import RecentTransactions from "./RecentTransactions"

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

test("renders transaction table", () => {
  Auth.currentUserInfo = jest.fn().mockImplementation(
    () =>
      new Promise(() => {
        return true
      })
  )
  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <RecentTransactions />
    </MemoryRouter>
  )
  const table = wrapper.getByRole("table")
  expect(table).toBeInTheDocument()
})

test("test show table data", async () => {
  Auth.currentAuthenticatedUser = jest
    .fn()
    .mockImplementation(mockCurrentAuthenticatedUser)
  const tmpFetch = fetch
  // Mock a success response from the server
  // response.ok == true
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () =>
        Promise.resolve([
          {
            uuid: "88280b20-2b19-4062-9f7a-06e5b9f20683",
            order_uuid: null,
            external_order_id: null,
            createdAt: "2021-12-14T03:36:33.845621Z",
            amount: 0.0001,
            asset: "BTC",
            fiatAmount: null,
            fiatAsset: null,
            status: "SUCCESSFUL",
            transactionType: "PEER_TO_PEER",
            senderName: "wipeb11618",
            receiverName: "kebawo2196@kingsready.com",
            direction: "OUTGOING",
            description: "testtest",
            totalAmount: 0.0001005,
            feePercentage: 0.5,
            feeAmount: 5e-7,
            blockchainTxId: null,
            id: "88280b20-2b19-4062-9f7a-06e5b9f20683"
          }
        ])
    })
  ) as jest.Mock

  const wrapper = await renderWithQueryClient(
    <MemoryRouter>
      <RecentTransactions />
    </MemoryRouter>
  )
  const showResult = await waitFor(() =>
    wrapper.getByText("kebawo2196@kingsready.com")
  )
  const showPagination = await waitFor(() => wrapper.getByTestId("paginate"))
  expect(showResult).toBeInTheDocument()
  expect(showPagination).toBeInTheDocument()
  global.fetch = tmpFetch
})

test("test show table error", async () => {
  Auth.currentUserInfo = jest.fn().mockImplementation(
    () =>
      new Promise(() => {
        return true
      })
  )

  const tmpFetch = fetch
  // Mock an error response from the server
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () =>
        Promise.resolve({
          message: "Something went wrong, network request has failed."
        })
    })
  ) as jest.Mock

  const wrapper = await renderWithQueryClient(
    <MemoryRouter>
      <RecentTransactions />
    </MemoryRouter>
  )
  const getError = await waitFor(() => wrapper.findByTestId("errorMessage"))
  expect(getError.innerHTML).toBe(
    "Something went wrong, network request has failed."
  )
  global.fetch = tmpFetch
})

test("test navigate", async () => {
  Auth.currentUserInfo = jest.fn().mockImplementation(
    () =>
      new Promise(() => {
        return true
      })
  )

  const tmpFetch = fetch
  // Mock a success response from the server
  // response.ok == true
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () =>
        Promise.resolve([
          {
            uuid: "88280b20-2b19-4062-9f7a-06e5b9f20683",
            order_uuid: null,
            external_order_id: null,
            createdAt: "2021-12-14T03:36:33.845621Z",
            amount: 0.0001,
            asset: "BTC",
            fiatAmount: null,
            fiatAsset: null,
            status: "SUCCESSFUL",
            transactionType: "PEER_TO_PEER",
            senderName: "wipeb11618",
            receiverName: "kebawo2196@kingsready.com",
            direction: "OUTGOING",
            description: "testtest",
            totalAmount: 0.0001005,
            feePercentage: 0.5,
            feeAmount: 5e-7,
            blockchainTxId: null,
            id: "88280b20-2b19-4062-9f7a-06e5b9f20683"
          }
        ])
    })
  ) as jest.Mock

  const wrapper = await renderWithQueryClient(
    <MemoryRouter>
      <RecentTransactions />
    </MemoryRouter>
  )
  const showResult = await waitFor(() =>
    wrapper.getByText("kebawo2196@kingsready.com")
  )
  fireEvent.click(showResult)
  expect(mockedNavigate).toHaveBeenCalledTimes(1)
  expect(mockedNavigate).toHaveBeenCalledWith(
    `${RouteType.TRANSACTION_DETAIL}/88280b20-2b19-4062-9f7a-06e5b9f20683`,
    { state: { from: "/" } }
  )
  global.fetch = tmpFetch
})
