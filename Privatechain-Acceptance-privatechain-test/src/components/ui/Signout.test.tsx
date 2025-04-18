import React from "react"
import { fireEvent } from "@testing-library/dom"
import { render, waitFor } from "@testing-library/react"
import { UserContextWrapper } from "src/testUtils"

import SignOut from "./SignOut"

const mockedSignOutAsync = jest.fn()
jest.mock("src/auth/AuthManager", () => {
  const originalAuthManager = jest.requireActual("src/auth/AuthManager")
  return {
    __esModule: true,
    ...originalAuthManager,
    signOutAsync: () => mockedSignOutAsync()
  }
})

test("user signed out when sign out clicked", async () => {
  const signedInUser = render(
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
      <SignOut />
    </UserContextWrapper>
  )
  const buttonElement = signedInUser.getByRole("button")

  await waitFor(() => {
    fireEvent.click(buttonElement)
  })

  expect(mockedSignOutAsync).toHaveBeenCalled()
})
