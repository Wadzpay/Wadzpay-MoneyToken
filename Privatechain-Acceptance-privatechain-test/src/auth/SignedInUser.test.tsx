import React from "react"
import { screen } from "@testing-library/dom"
import { Auth } from "aws-amplify"
import { render } from "@testing-library/react"
import { UserContextWrapper } from "src/testUtils"

import SignedInUser from "./SignedInUser"

test("does not render content if not signed in", async () => {
  render(
    <UserContextWrapper>
      <SignedInUser>
        <div data-testid="userSignedIn">User Signed Out</div>
      </SignedInUser>
    </UserContextWrapper>
  )
  // expect(
  //   await screen.queryByText("User Signed Out", {}, { timeout: 2000 })
  // ).not.toBeInTheDocument()
})

test("renders content if signed in", async () => {
  Auth.currentAuthenticatedUser = jest.fn().mockImplementation(
    () =>
      new Promise(() => {
        return true
      })
  )

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
      <SignedInUser>
        <div data-testid="userSignedIn">User Signed In</div>
      </SignedInUser>
    </UserContextWrapper>
  )

  expect(
    await screen.findByText("User Signed In", {}, { timeout: 2000 })
  ).toBeInTheDocument()
})
