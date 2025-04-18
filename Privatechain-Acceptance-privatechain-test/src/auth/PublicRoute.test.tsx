import React from "react"
import { render, screen } from "@testing-library/react"
import { UserContextWrapper } from "src/testUtils"
import { BrowserRouter, Routes, Route } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"

import PublicRoute from "./PublicRoute"

test("should render public route if user has not been authenticated", async () => {
  render(
    <UserContextWrapper>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<PublicRoute />}>
            <Route path="/" element={<div>Test public route</div>} />
          </Route>
        </Routes>
      </BrowserRouter>
    </UserContextWrapper>
  )

  expect(
    await screen.findByText("Test public route", {}, { timeout: 2000 })
  ).toBeInTheDocument()
})

test("should redirect to home page if user has been authenticated", async () => {
  render(
    <UserContextWrapper
      user={{
        attributes: {
          email: "test@test.com",
          phone_number: "+123456578",
          sub: ""
        },
        username: "testuser"
      }}
    >
      <BrowserRouter>
        <Routes>
          <Route path="/sign-in" element={<PublicRoute />}>
            <Route path="/sign-in" element={<div>Test public route</div>} />
          </Route>
          <Route
            path={RouteType.HOME}
            element={<div>Redirected to dashboard</div>}
          />
        </Routes>
      </BrowserRouter>
    </UserContextWrapper>
  )
  expect(
    await screen.findByText("Redirected to dashboard", {}, { timeout: 2000 })
  ).toBeInTheDocument()
})
