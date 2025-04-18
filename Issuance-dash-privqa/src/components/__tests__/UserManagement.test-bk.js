import React from "react"
import { render, screen } from "@testing-library/react"
import "@testing-library/jest-dom"
import { Provider } from "react-redux"
import configureStore from "redux-mock-store"
import UserManagement from "src/screens/userManagement/UserManagement"

const mockStore = configureStore([])

describe("UserManagement Component", () => {
  let store

  beforeEach(() => {
    // Initial state for the store
    store = mockStore({
      appConfig: {
        buttonColor: "#ff0000" // Example button color
      }
    })
  })

  it("should render the page heading", () => {
    render(
      <Provider store={store}>
        <UserManagement />
      </Provider>
    )

    // Check if the title "MBSB - User Management" exists
    expect(screen.getByText("MBSB - User Management")).toBeInTheDocument()
  })

  it("should display an image", () => {
    render(
      <Provider store={store}>
        <UserManagement />
      </Provider>
    )

    // Check if the image is rendered with the correct src attribute
    const imgElement = screen.getByRole("img")
    expect(imgElement).toBeInTheDocument()
    expect(imgElement).toHaveAttribute("src", "/images/user-management.svg")
  })

  it('should display the "Create User" button with correct background color', () => {
    render(
      <Provider store={store}>
        <UserManagement />
      </Provider>
    )

    // Check if the button is rendered and has the correct background color
    const button = screen.getByRole("button", { name: "Create User" })
    expect(button).toBeInTheDocument()
    expect(button).toHaveStyle("background: #ff0000")
  })

  it('should display the "Coming soon" tooltip', () => {
    render(
      <Provider store={store}>
        <UserManagement />
      </Provider>
    )

    // Check if the button contains a tooltip with "Coming soon"
    expect(screen.getByText("Create User")).toBeInTheDocument()
  })

  it("should display a message when no users are available", () => {
    render(
      <Provider store={store}>
        <UserManagement />
      </Provider>
    )

    // Check if the message about no users is displayed
    expect(
      screen.getByText("It seems to be no users to manage")
    ).toBeInTheDocument()
    expect(
      screen.getByText("Write here lorem ipsum text - dummy text")
    ).toBeInTheDocument()
  })
})
