/* eslint-disable no-console */
import React from "react"
import { screen, waitFor } from "@testing-library/react"
import { renderWithQueryClient } from "src/testUtils"
import { MemoryRouter } from "react-router-dom"

import P2PTransaction from "./P2PTransaction"

jest.mock("react-router-dom", () => {
  const originalRouterDom = jest.requireActual("react-router-dom")
  return {
    __esModule: true,
    ...originalRouterDom,
    useParams: () => {
      return {
        transactionId: "123456578"
      }
    }
  }
})

test("render pre filled details if transaction id included", async () => {
  const tmpFetch = global.fetch
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () =>
        Promise.resolve({
          uuid: "87654321",
          order_uuid: null,
          external_order_id: null,
          createdAt: "2021-12-14T03:36:33.845621Z",
          amount: 0.0001,
          asset: "ETH",
          fiatAmount: null,
          fiatAsset: null,
          status: "SUCCESSFUL",
          transactionType: "PEER_TO_PEER",
          senderName: "sender@test.com",
          receiverName: "receiver@test.com",
          direction: "INCOMING",
          description: "test test",
          totalAmount: 0.0001005,
          feePercentage: 0.5,
          feeAmount: 5e-7,
          blockchainTxId: null,
          id: "123456578"
        })
    })
  ) as jest.Mock

  renderWithQueryClient(
    <MemoryRouter>
      <P2PTransaction />
    </MemoryRouter>
  )

  const amountInput = await waitFor(() =>
    screen.getByTestId<HTMLInputElement>("amount")
  )
  expect(amountInput.value).toBe("0.0001")

  const assetInput = await waitFor(() =>
    screen.getByTestId<HTMLInputElement>("asset")
  )
  expect(assetInput.value).toBe("ETH")

  const receiverEmailInput = await waitFor(() =>
    screen.getByTestId<HTMLInputElement>("receiverEmail")
  )
  expect(receiverEmailInput.value).toBe("sender@test.com")

  global.fetch = tmpFetch
})
