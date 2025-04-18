import React from "react"
import { MemoryRouter } from "react-router-dom"
import { fireEvent, screen, waitFor } from "@testing-library/react"
import { RouteType } from "src/constants/routeTypes"
import { renderWithQueryClient } from "src/testUtils"

import InviteUser from "./InviteUser"

test("renders invite users form", () => {
  renderWithQueryClient(
    <MemoryRouter>
      <InviteUser />
    </MemoryRouter>
  )

  const emailInputElement = screen.getByTestId("emailInput")
  expect(emailInputElement).toBeDefined()
})

test("renders back link", async () => {
  renderWithQueryClient(
    <MemoryRouter>
      <InviteUser />
    </MemoryRouter>
  )
  const linkElement = screen.getByTestId<HTMLAnchorElement>("backButton")

  expect(linkElement.attributes.getNamedItem("href")?.value).toBe(
    RouteType.ADMIN_USERS
  )
})

test("successful invite user flow", async () => {
  const tmpFetch = fetch

  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      text: () => Promise.resolve("")
    })
  ) as jest.Mock

  renderWithQueryClient(
    <MemoryRouter>
      <InviteUser />
    </MemoryRouter>
  )
  const emailElement = screen.getByTestId("emailInput")
  const submitButton = screen.getByRole("button", { name: "Invite" })

  fireEvent.change(emailElement, { target: { value: "test@example.com" } })

  await waitFor(() => {
    fireEvent.click(submitButton)
  })

  expect(
    await screen.findByText("Invitation submitted", {}, { timeout: 500 })
  ).toBeInTheDocument()

  global.fetch = tmpFetch
})

test("validation: required inputs", async () => {
  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <InviteUser />
    </MemoryRouter>
  )
  const formElement = wrapper.getByRole("form")

  fireEvent.submit(formElement)

  expect(
    await waitFor(() => screen.getByText("Please enter a valid email address"))
  ).toBeInTheDocument()
})

test("validation: email format", async () => {
  const wrapper = renderWithQueryClient(
    <MemoryRouter>
      <InviteUser />
    </MemoryRouter>
  )
  const formElement = wrapper.getByRole("form")
  const emailElement = wrapper.getByTestId("emailInput")

  fireEvent.change(emailElement, { target: { value: "test" } })
  fireEvent.submit(formElement)

  expect(
    await waitFor(() => screen.getByText("Please enter a valid email address"))
  ).toBeInTheDocument()
})
