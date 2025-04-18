import React from "react"
import { MemoryRouter } from "react-router-dom"
import { render, screen } from "@testing-library/react"
import { RouteType } from "src/constants/routeTypes"

import Admin from "."

test("renders users link", async () => {
  render(
    <MemoryRouter>
      <Admin />
    </MemoryRouter>
  )
  const linkElement = screen.getByTestId<HTMLAnchorElement>("usersLink")

  expect(linkElement.attributes.getNamedItem("href")?.value).toBe(
    RouteType.ADMIN_USERS
  )
})

test("renders api keys link", async () => {
  render(
    <MemoryRouter>
      <Admin />
    </MemoryRouter>
  )
  const linkElement = screen.getByTestId<HTMLAnchorElement>("apiKeysLink")

  expect(linkElement.attributes.getNamedItem("href")?.value).toBe(
    RouteType.ADMIN_API_KEYS
  )
})

test("renders back link", async () => {
  render(
    <MemoryRouter>
      <Admin />
    </MemoryRouter>
  )
  const linkElement = screen.getByTestId<HTMLAnchorElement>("backButton")

  expect(linkElement.attributes.getNamedItem("href")?.value).toBe(
    RouteType.HOME
  )
})
