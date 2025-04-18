/* eslint-disable no-console */
import React from "react"
import { MemoryRouter } from "react-router-dom"
import { fireEvent, waitFor } from "@testing-library/react"
import { RouteType } from "src/constants/routeTypes"
import { renderWithQueryClient } from "src/testUtils"

import CreateAPIKey from "./CreateAPIKey"
const original = console.error

beforeEach(() => {
  console.error = jest.fn()
})

afterEach(() => {
  console.error = original
})
test("renders back link", async () => {
  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <CreateAPIKey />
    </MemoryRouter>
  )
  const linkElement = wrapper.getByTestId("backButton")

  expect(linkElement.attributes.getNamedItem("href")?.value).toBe(
    RouteType.ADMIN_API_KEYS
  )
})

test("error getting API", async () => {
  // Mock a failed response from the server
  // response.ok != true
  // Error message is returned in the response body
  const tmpFetch = fetch
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve({ message: "MERCHANT_NOT_FOUND" })
    })
  ) as jest.Mock

  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <CreateAPIKey />
    </MemoryRouter>
  )
  const generateAPIButton = wrapper.getByTestId("generateAPI")
  await fireEvent.click(generateAPIButton)
  expect(
    await waitFor(() => wrapper.getByText("Merchant not found"))
  ).toBeInTheDocument()

  global.fetch = tmpFetch
})

test("success getting API", async () => {
  const tmpFetch = fetch
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      text: () =>
        Promise.resolve({
          username: "test username",
          password: "test password",
          basicKey: "test basicKey"
        })
    })
  ) as jest.Mock

  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <CreateAPIKey />
    </MemoryRouter>
  )
  const generateAPIButton = wrapper.getByTestId("generateAPI")
  await fireEvent.click(generateAPIButton)
  expect(
    await waitFor(() => wrapper.getByText("API key generated"))
  ).toBeInTheDocument()

  expect(
    await waitFor(() => wrapper.getByText("test username"))
  ).toBeInTheDocument()

  expect(
    await waitFor(() => wrapper.getByText("test password"))
  ).toBeInTheDocument()

  expect(
    await waitFor(() => wrapper.getByText("test basicKey"))
  ).toBeInTheDocument()

  global.fetch = tmpFetch
})
