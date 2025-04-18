/* eslint-disable no-console */
import React from "react"
import { waitFor, screen } from "@testing-library/react"
import { BrowserRouter } from "react-router-dom"
import { createBrowserHistory } from "history"
import { OnboardingContextWrapper, renderWithQueryClient } from "src/testUtils"
import { RouteType } from "src/constants/routeTypes"

import TransactionDetail from "./TransactionDetails"

const transactionId = "ABCD123456"

jest.mock("react-router-dom", () => {
  const originalRouterDom = jest.requireActual("react-router-dom")
  return {
    __esModule: true,
    ...originalRouterDom,
    useParams: () => ({
      transactionId
    })
  }
})

const original = console.error

beforeEach(() => {
  console.error = jest.fn()
})

afterEach(() => {
  console.error = original
})

const renderWrapper = (element: JSX.Element) => {
  return renderWithQueryClient(
    <OnboardingContextWrapper>
      <BrowserRouter>{element}</BrowserRouter>
    </OnboardingContextWrapper>
  )
}
test("renders title", () => {
  const wrapper = renderWrapper(<TransactionDetail />)
  const linkElement = wrapper.getByTestId("title")
  expect(linkElement.innerHTML).toBe("Transaction Details")
})

test("renders details", async () => {
  const tmpFetch = global.fetch

  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () =>
        Promise.resolve({
          uuid: "88280b20-2b19-4062-9f7a-06e5b9f20683",
          order_uuid: null,
          external_order_id: null,
          createdAt: "2021-12-14T11:36:33.845621Z",
          amount: 0.0001,
          asset: "BTC",
          fiatAmount: 123.45,
          fiatAsset: null,
          status: "SUCCESSFUL",
          transactionType: "PEER_TO_PEER",
          senderName: "wipeb11618",
          receiverName: "kebawo2196@kingsready.com",
          direction: "OUTGOING",
          description: "test description",
          totalAmount: 0.0001005,
          feePercentage: 0.5,
          feeAmount: 0.0000005,
          blockchainTxId: null,
          id: "88280b20-2b19-4062-9f7a-06e5b9f20683"
        })
    })
  ) as jest.Mock
  const wrapper = renderWrapper(<TransactionDetail />)
  const title = wrapper.getByTestId("title")
  const date = await waitFor(() => wrapper.getByTestId("date"))
  //const senderName = await waitFor(() => wrapper.getByTestId("senderName"))
  //const receiverName = await waitFor(() => wrapper.getByTestId("receiverName"))
  const token = await waitFor(() => wrapper.getByTestId("token"))
  const tokenImage = token.querySelector("img")
  const tokenSpan = token.querySelector("span")
  const totalAmount = await waitFor(() => wrapper.getByTestId("totalAmount"))
  const fiatAmount = await waitFor(() => wrapper.getByTestId("fiatAmount"))
  const direction = await waitFor(() => wrapper.getByTestId("direction"))
  const feeAmount = await waitFor(() => wrapper.getByTestId("feeAmount"))
  /*  const transactionType = await waitFor(() =>
    wrapper.getByTestId("transactionType")
  )*/
  // const description = await waitFor(() => wrapper.getByTestId("description"))
  const status = await waitFor(() => wrapper.getByTestId("status"))
  const transactionId = await waitFor(() =>
    wrapper.getByTestId("transactionId")
  )

  expect(title.innerHTML).toBe("Transaction Details")
  expect(date.innerHTML).toBe("December 14, 2021 11:36 AM") // UTC
  //expect(senderName.innerHTML).toBe("wipeb11618")
  //expect(receiverName.innerHTML).toBe("kebawo2196@kingsready.com")
  expect(tokenImage?.attributes.getNamedItem("src")?.value).toBe(
    "/images/BTC.svg"
  )
  expect(tokenSpan?.innerHTML).toBe("BTC")
  expect(totalAmount.innerHTML).toBe("-0.00010050")
  expect(fiatAmount.innerHTML).toBe("123.45 ")
  expect(direction.innerHTML).toBe("Outgoing")
  expect(status.innerHTML).toBe("Successful")
  expect(feeAmount.innerHTML).toBe("0.00000050")
  // expect(transactionType.innerHTML).toBe("Peer To Peer")
  // expect(description.innerHTML).toBe("test description")
  expect(transactionId.innerHTML).toBe("88280b20-2b19-4062-9f7a-06e5b9f20683")

  global.fetch = tmpFetch
})

test("test back button to dashboard page", async () => {
  const tmpFetch = global.fetch

  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve({})
    })
  ) as jest.Mock

  const wrapper = renderWrapper(<TransactionDetail />)
  const backButton = await waitFor(() => wrapper.getByText("Back"))

  expect(backButton.attributes.getNamedItem("href")?.value).toBe(RouteType.HOME)

  global.fetch = tmpFetch
})

test("renders state back link", async () => {
  const tmpFetch = global.fetch

  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve({})
    })
  ) as jest.Mock

  const backLink = "/dashboard?page=2"
  const history = createBrowserHistory()
  history.push("/", { from: backLink })

  const wrapper = renderWrapper(<TransactionDetail />)
  const backButton = await waitFor(() => wrapper.getByText("Back"))

  expect(backButton.attributes.getNamedItem("href")?.value).toBe(backLink)

  global.fetch = tmpFetch
})

test("reverse transaction button does not appear if there is no email address", async () => {
  const tmpFetch = global.fetch

  // Incoming, no email NOT in doc
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () =>
        Promise.resolve({
          senderName: "No email address",
          direction: "INCOMING",
          description: "test test",
          totalAmount: 0.0001005,
          id: "88280b20-2b19-4062-9f7a-06e5b9f20683"
        })
    })
  ) as jest.Mock

  renderWrapper(<TransactionDetail />)
  const p2pButton = await waitFor(() => screen.queryByTestId("p2pButton"))
  expect(p2pButton).toBe(null)

  global.fetch = tmpFetch
})

test("reverse transaction button does not appear if outgoing", async () => {
  const tmpFetch = global.fetch

  // Outgoing, email NOT in doc
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () =>
        Promise.resolve({
          senderName: "email@test.com",
          direction: "OUTGOING",
          description: "test test",
          totalAmount: 0.0001005,
          id: "88280b20-2b19-4062-9f7a-06e5b9f20683"
        })
    })
  ) as jest.Mock

  renderWrapper(<TransactionDetail />)
  const p2pButton = await waitFor(() => screen.queryByTestId("p2pButton"))
  expect(p2pButton).toBe(null)
  global.fetch = tmpFetch
})

/*test("reverse transaction button appears if incoming and email", async () => {
  const tmpFetch = global.fetch

  // Incoming, email in doc
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () =>
        Promise.resolve({
          senderName: "email@test.com",
          direction: "INCOMING",
          description: "test test",
          totalAmount: 0.0001005,
          id: "88280b20-2b19-4062-9f7a-06e5b9f20683"
        })
    })
  ) as jest.Mock

  renderWrapper(<TransactionDetail />)
  const p2pButton = await waitFor(() =>
    screen.getByTestId<HTMLAnchorElement>("p2pButton")
  )

  expect(p2pButton?.attributes.getNamedItem("href")?.value).toBe(
    `${RouteType.P2P_TRANSACTION}/${transactionId}`
  )

  global.fetch = tmpFetch
})*/
