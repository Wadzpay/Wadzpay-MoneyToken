import React from "react"
import { MemoryRouter } from "react-router-dom"
import { render, screen } from "@testing-library/react"
import { RouteType } from "src/constants/routeTypes"

import APIKeys from "./APIKeys"

test("renders back link", async () => {
  render(
    <MemoryRouter>
      <APIKeys />
    </MemoryRouter>
  )
  const linkElement = screen.getByTestId<HTMLAnchorElement>("backButton")

  expect(linkElement.attributes.getNamedItem("href")?.value).toBe(
    RouteType.ADMIN
  )
})
