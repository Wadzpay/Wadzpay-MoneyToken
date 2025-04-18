import React from "react"
import { screen } from "@testing-library/react"
import { MemoryRouter } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"

import { renderWithQueryClient } from "../../testUtils"
import Success from "./Success"

const mockedNavigate = jest.fn()
jest.mock("react-router-dom", () => {
  const originalRouterDom = jest.requireActual("react-router-dom")
  return {
    __esModule: true,
    ...originalRouterDom,
    useNavigate: () => mockedNavigate
  }
})

test("renders success page", () => {
  renderWithQueryClient(
    <MemoryRouter>
      <Success />
    </MemoryRouter>
  )

  const linkElement = screen.getByRole("heading", { level: 2 })
  expect(linkElement.innerHTML).toBe("Your account was created.")
})

test("link to sign in page", async () => {
  renderWithQueryClient(
    <MemoryRouter>
      <Success />
    </MemoryRouter>
  )

  const linkElement = screen.getByRole<HTMLAnchorElement>("link", {
    name: "Sign In"
  })

  expect(linkElement).toBeInTheDocument()
  expect(linkElement.href).toContain(RouteType.SIGN_IN)
})
