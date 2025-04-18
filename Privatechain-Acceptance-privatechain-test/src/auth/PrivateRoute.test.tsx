import React from "react"
import { render, screen } from "@testing-library/react"
import App from "src/App"
import { renderWithQueryClient, UserContextWrapper } from "src/testUtils"
import { Routes, Route } from "react-router"
import { BrowserRouter } from "react-router-dom"
import { RouteType } from "src/constants/routeTypes"

import PrivateRoute from "./PrivateRoute"

const renderWithRouter = (ui: JSX.Element, { route = "/" } = {}) => {
  window.history.pushState({}, "Test page", route)
  return render(ui)
}

jest.mock("aws-amplify")

test("should render private route if user has been authenticated", async () => {
  const tmpFetch = global.fetch

  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve([{}])
    })
  ) as jest.Mock

  renderWithQueryClient(
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
          <Route path="/" element={<PrivateRoute />}>
            <Route path="/" element={<div>Test private route</div>} />
          </Route>
        </Routes>
      </BrowserRouter>
    </UserContextWrapper>
  )

  expect(
    await screen.findByText("Test private route", {}, { timeout: 2000 })
  ).toBeInTheDocument()

  global.fetch = tmpFetch
})

/*test("should redirect to sign in page if user not authenticated", async () => {
  render(
    <UserContextWrapper>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<PrivateRoute />}>
            <Route path="/" element={<div>Test private route</div>} />
          </Route>
          <Route
            path={RouteType.SIGN_IN}
            element={<div>Sign in page</div>}
          ></Route>
        </Routes>
      </BrowserRouter>
    </UserContextWrapper>
  )

  expect(
    await screen.findByText("Sign in page", {}, { timeout: 2000 })
  ).toBeInTheDocument()
})*/

/*test("should not render private component if user is unauthenticated", async () => {
  renderWithRouter(<App />, { route: "/" })
  const headerElement = await screen.findByRole(
    "heading",
    { level: 2 },
    { timeout: 2000 }
  )
  expect(headerElement.innerHTML).toBe("Sign In")
})*/
