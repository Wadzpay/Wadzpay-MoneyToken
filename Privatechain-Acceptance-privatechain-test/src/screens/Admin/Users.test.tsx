import React from "react"
import { MemoryRouter } from "react-router-dom"
import { screen } from "@testing-library/react"
import { RouteType } from "src/constants/routeTypes"
import { renderWithQueryClient } from "src/testUtils"

import Users from "./Users"

test("renders back link", async () => {
  const tmpFetch = fetch

  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve([])
    })
  ) as jest.Mock

  renderWithQueryClient(
    <MemoryRouter>
      <Users />
    </MemoryRouter>
  )
  const linkElement = screen.getByTestId<HTMLAnchorElement>("backButton")

  expect(linkElement.attributes.getNamedItem("href")?.value).toBe(
    RouteType.ADMIN
  )

  global.fetch = tmpFetch
})
