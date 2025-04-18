import React from "react"
import { render, screen, fireEvent, waitFor } from "@testing-library/react"
import { renderWithQueryClient } from "src/testUtils"
import { Auth } from "aws-amplify"

import { UserContextProvider } from "../context/User"
import SignIn from "./SignIn"

const mockedNavigate = jest.fn()
jest.mock("react-router-dom", () => {
  const originalRouterDom = jest.requireActual("react-router-dom")
  return {
    __esModule: true,
    ...originalRouterDom,
    useNavigate: () => mockedNavigate
  }
})

describe("Sign In", () => {
  test("renders dashboard title", () => {
    render(<SignIn />)

    const linkElement = screen.getByRole("heading", { level: 2 })
    expect(linkElement.innerHTML).toBe("Sign In")
  })

  test("renders sign in form", () => {
    render(<SignIn />)
    const emailElement = screen.getByTestId("email")
    const passwordElement = screen.getByTestId("password")

    expect(emailElement).toBeDefined()
    expect(passwordElement).toBeDefined()
  })

  test("successful login flow", async () => {
    const wrapper = render(<SignIn />)

    const formElement = wrapper.getByRole("form")
    const signInButton = wrapper.getByRole("button", { name: "Sign In" })

    formElement.onsubmit = jest.fn()
    await waitFor(() => {
      fireEvent.click(signInButton)
    })
    expect(formElement.onsubmit).toHaveBeenCalledTimes(1)
  })

  test("validation: required inputs", async () => {
    const wrapper = renderWithQueryClient(<SignIn />)
    const formElement = wrapper.getByRole("form")

    fireEvent.submit(formElement)
    const emailError = await waitFor(() =>
      screen.getByText("Please enter your email")
    )

    const passwordError = await waitFor(() =>
      screen.getByText("Please enter your password")
    )

    expect(emailError).toBeInTheDocument()
    expect(passwordError).toBeInTheDocument()
  })

  test("validation: email format", async () => {
    const wrapper = renderWithQueryClient(<SignIn />)
    const formElement = wrapper.getByRole("form")
    const emailElement = wrapper.getByTestId("email")

    fireEvent.change(emailElement, { target: { value: "test" } })
    fireEvent.submit(formElement)

    const emailError = await waitFor(() =>
      screen.getByText("Please enter a valid email address")
    )
    expect(emailError).toBeInTheDocument()
  })

  test("sign in with wrong details", async () => {
    Auth.signIn = jest.fn().mockImplementation(
      (email, pass) =>
        new Promise((resolve, reject) => {
          const userExists = "test@test.com"
          if (email === userExists && pass === "12345678") {
            const signedUser = {
              username: "abcdfg123",
              attributes: { email, name: "John Rambo", phone: "+460777777777" },
              signInUserSession: {
                accessToken: { jwtToken: "123456" }
              }
            }
            return resolve(signedUser)
          }

          if (email === userExists) {
            return reject({
              code: "NotAuthorizedException",
              name: "NotAuthorizedException",
              message: "Incorrect username or password."
            })
          }
        })
    )

    Auth.currentUserInfo = jest.fn().mockImplementation(
      () =>
        new Promise(() => {
          return true
        })
    )

    const wrapper = renderWithQueryClient(
      <UserContextProvider>
        <SignIn />
      </UserContextProvider>
    )
    const formElement = wrapper.getByRole("form")
    const emailElement = wrapper.getByTestId("email")
    const passwordElement = wrapper.getByTestId("password")
    fireEvent.change(emailElement, { target: { value: "test@test.com" } })
    fireEvent.change(passwordElement, { target: { value: "password124" } })
    fireEvent.submit(formElement)
    const errorMessage = await waitFor(() =>
      screen.getByText("Incorrect username or password.")
    )
    expect(errorMessage).toBeInTheDocument()

    const passwordElementAfterSubmit = wrapper.getByTestId(
      "password"
    ) as HTMLInputElement
    expect(passwordElementAfterSubmit.value).toBe("")
  })

  test("Sign in successfully", async () => {
    const wrapper = renderWithQueryClient(
      <UserContextProvider>
        <SignIn />
      </UserContextProvider>
    )
    Auth.currentUserInfo = jest.fn().mockImplementation(
      () =>
        new Promise(() => {
          return true
        })
    )
    const formElement = wrapper.getByRole("form")
    const emailElement = wrapper.getByTestId("email")
    const passwordElement = wrapper.getByTestId("password")
    fireEvent.change(emailElement, { target: { value: "test@test.com" } })
    fireEvent.change(passwordElement, { target: { value: "12345678" } })
    await waitFor(() => {
      fireEvent.submit(formElement)
    })
    // Check that the mocked navigate function was called
    expect(mockedNavigate).toHaveBeenCalledTimes(1)
  })
})
