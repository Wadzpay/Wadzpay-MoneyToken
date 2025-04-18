/* eslint-disable no-console */
import React from "react"
import { screen, waitFor } from "@testing-library/react"
import {
  mockCurrentAuthenticatedUser,
  renderWithQueryClient
} from "src/testUtils"
import { MemoryRouter } from "react-router-dom"
import { Auth } from "aws-amplify"

import Balances from "./Balances"

const original = console.error

beforeEach(() => {
  console.error = jest.fn()
})

afterEach(() => {
  console.error = original
})

test("renders account balance Section", () => {
  renderWithQueryClient(
    <MemoryRouter>
      <Balances />
    </MemoryRouter>
  )
  const element = screen.getByTestId("account-balance").textContent
  expect(element).toBe("Account Balance")
})

test("render balances ", async () => {
  const wrapper = await renderWithQueryClient(
    <MemoryRouter>
      <Balances />
    </MemoryRouter>
  )
  const element = await waitFor(() => wrapper.queryAllByTestId("balances"))
  expect(element).not.toEqual([])
})

test("do not render balance amount if response is null  ", async () => {
  Auth.currentAuthenticatedUser = jest
    .fn()
    .mockImplementation(mockCurrentAuthenticatedUser)

  const tmpFetch = global.fetch

  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve([{}])
    })
  ) as jest.Mock

  const wrapper = await renderWithQueryClient(
    <MemoryRouter>
      <Balances />
    </MemoryRouter>
  )

  const element = await waitFor(() => wrapper.queryAllByText("balances"))
  expect(element).toEqual([])

  global.fetch = tmpFetch
})
