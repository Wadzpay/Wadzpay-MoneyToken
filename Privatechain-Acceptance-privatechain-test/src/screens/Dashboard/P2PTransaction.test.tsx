/* eslint-disable no-console */
import React from "react"
import { screen, fireEvent, waitFor } from "@testing-library/react"
import { createBrowserHistory } from "history"
import { BrowserRouter, MemoryRouter } from "react-router-dom"
import { renderWithQueryClient } from "src/testUtils"
import { RouteType } from "src/constants/routeTypes"

import P2PTransaction from "./P2PTransaction"

const original = console.error

beforeEach(() => {
  console.error = jest.fn()
})

afterEach(() => {
  console.error = original
})

test("renders P2P form", () => {
  renderWithQueryClient(
    <MemoryRouter>
      <P2PTransaction />
    </MemoryRouter>
  )
  const linkElement = screen.getByRole("heading", { level: 2 })
  expect(linkElement.innerHTML).toBe("M2C Transaction")

  const sendByElement = screen.getByTestId("sendBy")
  expect(sendByElement).toBeInTheDocument()

  const receiverEmailElement = screen.getByTestId("receiverEmail")
  expect(receiverEmailElement).toBeInTheDocument()
})

test("P2P amount invalid", async () => {
  renderWithQueryClient(
    <MemoryRouter>
      <P2PTransaction />
    </MemoryRouter>
  )

  const amountElement = screen.getByTestId("amount")
  fireEvent.change(amountElement, { target: { value: "abcd" } })

  await waitFor(() => {
    fireEvent.submit(amountElement)
  })

  expect(screen.getByText("Amount must be a number")).toBeInTheDocument()

  fireEvent.change(amountElement, { target: { value: "0.000.111" } })

  await waitFor(() => {
    fireEvent.submit(amountElement)
  })
  expect(screen.getByText("Amount must be a number")).toBeInTheDocument()
})

test("email required", async () => {
  renderWithQueryClient(
    <MemoryRouter>
      <P2PTransaction />
    </MemoryRouter>
  )

  const emailElement = screen.getByTestId("receiverEmail")
  fireEvent.change(emailElement, { target: { value: "" } })

  await waitFor(() => {
    fireEvent.submit(emailElement)
  })
  expect(screen.getByTestId("receiverEmailError")).toBeInTheDocument()
})

test("renders default back link", async () => {
  renderWithQueryClient(
    <MemoryRouter>
      <P2PTransaction />
    </MemoryRouter>
  )
  const linkElement = screen.getByTestId<HTMLAnchorElement>("backButton")

  expect(linkElement.attributes.getNamedItem("href")?.value).toBe(
    RouteType.HOME
  )
})

test("renders state back link", async () => {
  const backLink = "/dashboard?page=2"
  const history = createBrowserHistory()
  history.push("/", { from: backLink })

  renderWithQueryClient(
    <BrowserRouter>
      <P2PTransaction />
    </BrowserRouter>
  )
  const linkElement = screen.getByTestId<HTMLAnchorElement>("backButton")

  expect(linkElement.attributes.getNamedItem("href")?.value).toBe(backLink)
})

test("cancel confirmation", async () => {
  renderWithQueryClient(
    <MemoryRouter>
      <P2PTransaction test={true} />
    </MemoryRouter>
  )
  const amountElement = screen.getByTestId("amount")
  fireEvent.change(amountElement, { target: { value: "1" } })

  const emailElement = screen.getByTestId("receiverEmail")
  fireEvent.change(emailElement, { target: { value: "test@test.com" } })

  const formElement = screen.getByRole("form")

  await waitFor(() => {
    fireEvent.submit(formElement)
  })

  const confirmButton = screen.getByTestId("cancelConfirmButton")

  await waitFor(() => {
    fireEvent.click(confirmButton)
  })

  expect(screen.getByText("Amount")).toBeInTheDocument()
})

test("P2P success email transfer", async () => {
  const tmpFetch = fetch
  // Mock a success response from the server
  // response.ok == true
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      text: () => ""
    })
  ) as jest.Mock
  renderWithQueryClient(
    <MemoryRouter>
      <P2PTransaction test={true} />
    </MemoryRouter>
  )
  const amountElement = screen.getByTestId("amount")
  fireEvent.change(amountElement, { target: { value: "1" } })

  const emailElement = screen.getByTestId("receiverEmail")
  fireEvent.change(emailElement, { target: { value: "test@test.com" } })

  const formElement = screen.getByRole("form")

  await waitFor(() => {
    fireEvent.submit(formElement)
  })

  const confirmButton = screen.getByTestId("confirmButton")

  await waitFor(() => {
    fireEvent.click(confirmButton)
  })

  expect(screen.getByText("Transaction complete")).toBeInTheDocument()
  global.fetch = tmpFetch
})

/* TODO work out why this test is failing
test("P2P success phone transfer", async () => {
  const tmpFetch = fetch
  // Mock a success response from the server
  // response.ok == true
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      text: () => ""
    })
  ) as jest.Mock
  renderWithQueryClient(
    <MemoryRouter>
      <P2PTransaction test={true} />
    </MemoryRouter>
  )
  const amountElement = screen.getByTestId("amount")
  fireEvent.change(amountElement, { target: { value: "1" } })

  const sendByElement = screen.getByTestId<HTMLSelectElement>("sendBy")
  fireEvent.change(sendByElement, { target: { value: "phone" } })

  screen.debug(undefined, 300000)
  const phoneElement = screen.getByTestId("receiverPhone")
  fireEvent.change(phoneElement, { target: { value: "+123456789" } })

  const formElement = screen.getByRole("form")

  await waitFor(() => {
    fireEvent.submit(formElement)
  })

  const confirmButton = screen.getByTestId("confirmButton")

  await waitFor(() => {
    fireEvent.click(confirmButton)
  })

  expect(screen.getByText("Transaction complete")).toBeInTheDocument()
  global.fetch = tmpFetch
})
*/

/* TODO work out why this test is failing
test("P2P success username transfer", async () => {
  const tmpFetch = fetch
  // Mock a success response from the server
  // response.ok == true
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      text: () => ""
    })
  ) as jest.Mock
  renderWithQueryClient(
    <MemoryRouter>
      <P2PTransaction test={true} />
    </MemoryRouter>
  )
  const amountElement = screen.getByTestId("amount")
  fireEvent.change(amountElement, { target: { value: "1" } })

  const sendByElement = screen.getByTestId<HTMLSelectElement>("sendBy")
  fireEvent.change(sendByElement, { target: { value: "username" } })

  const usernameElement = screen.getByTestId("receiverUsername")
  fireEvent.change(usernameElement, { target: { value: "test" } })

  const formElement = screen.getByRole("form")

  await waitFor(() => {
    fireEvent.submit(formElement)
  })

  const confirmButton = screen.getByTestId("confirmButton")

  await waitFor(() => {
    fireEvent.click(confirmButton)
  })

  expect(screen.getByText("Transaction complete")).toBeInTheDocument()
  global.fetch = tmpFetch
})
*/

test("P2P fail transfer", async () => {
  const tmpFetch = fetch
  // Mock a success response from the server
  // response.ok == true
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve({ message: "USER_NOT_FOUND" })
    })
  ) as jest.Mock

  renderWithQueryClient(
    <MemoryRouter>
      <P2PTransaction test={true} />
    </MemoryRouter>
  )
  const amountElement = screen.getByTestId("amount")
  fireEvent.change(amountElement, { target: { value: "1" } })

  const emailElement = screen.getByTestId("receiverEmail")
  fireEvent.change(emailElement, { target: { value: "test@test.com" } })

  const formElement = screen.getByRole("form")

  await waitFor(() => {
    fireEvent.submit(formElement)
  })

  const confirmButton = screen.getByTestId("confirmButton")

  await waitFor(() => {
    fireEvent.click(confirmButton)
  })

  expect(screen.getByText("The user was not found.")).toBeInTheDocument()
  global.fetch = tmpFetch
})

test("P2P insufficient fund to transfer", async () => {
  const tmpFetch = fetch
  // Mock a success response from the server
  // response.ok == true
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve({ message: "INSUFFICIENT_FUNDS" })
    })
  ) as jest.Mock

  renderWithQueryClient(
    <MemoryRouter>
      <P2PTransaction test={true} />
    </MemoryRouter>
  )
  const amountElement = screen.getByTestId("amount")
  fireEvent.change(amountElement, { target: { value: "1" } })

  const emailElement = screen.getByTestId("receiverEmail")
  fireEvent.change(emailElement, { target: { value: "test@test.com" } })

  const formElement = screen.getByRole("form")

  await waitFor(() => {
    fireEvent.submit(formElement)
  })

  const confirmButton = screen.getByTestId("confirmButton")

  await waitFor(() => {
    fireEvent.click(confirmButton)
  })

  expect(screen.getByText("Insufficient funds.")).toBeInTheDocument()
  global.fetch = tmpFetch
})
