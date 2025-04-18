import React from "react"
import { render, screen } from "@testing-library/react"
import { BrowserRouter } from "react-router-dom"
import App from "src/App"
import { Auth } from "aws-amplify"

import Page404 from "./Page404"

const renderWithRouter = (ui: JSX.Element, { route = "/" } = {}) => {
  window.history.pushState({}, "Test page", route)

  return render(ui)
}

test("renders error 404", () => {
  render(
    <BrowserRouter>
      <Page404 />
    </BrowserRouter>
  )
  const linkElement = screen.getByRole("heading", { level: 2 })
  expect(linkElement.innerHTML).toBe(
    "The page you were looking for does not exist."
  )
})

test("check back to home href", () => {
  render(
    <BrowserRouter>
      <Page404 />
    </BrowserRouter>
  )
  const linkEl = screen.getByRole("link", { name: "Back to home" })
  expect(linkEl).toHaveAttribute("href", "/")
})

test("check on random page", async () => {
  Auth.currentUserInfo = jest.fn().mockImplementation(
    () =>
      new Promise((resolve) => {
        const currentUser = {
          username: "abc123",
          email: "demo@test.com",
          accessToken: "123cvb123",
          name: "John Rambo",
          phone: "+46761022312",
          phoneVerified: false,
          attributes: {
            sub: "abc123"
          }
        }

        return resolve(currentUser)
      })
  )
  renderWithRouter(<App />, { route: "/something-that-does-not-match" })
  const linkElement = await screen.findByTestId("title")
  expect(linkElement.innerHTML).toBe(
    "The page you were looking for does not exist."
  )
})
