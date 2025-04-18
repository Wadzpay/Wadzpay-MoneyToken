import React from "react"
import { screen } from "@testing-library/dom"
import { Auth } from "aws-amplify"
import { render } from "@testing-library/react"
import { UserContextWrapper } from "src/testUtils"

import SignedOutUser from "./SignedOutUser"

/*test("renders content if signed in", async () => {
  render(
    <UserContextWrapper>
      <SignedOutUser>
        <div data-testid="userSignedIn">User Signed Out</div>
      </SignedOutUser>
    </UserContextWrapper>
  )
  expect(
    await screen.findByText("User Signed Out", {}, { timeout: 2000 })
  ).toBeInTheDocument()
})*/

test("does not render content if signed in", async () => {
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
      <SignedOutUser>
        <div data-testid="userSignedIn">User Signed In</div>
      </SignedOutUser>
    </UserContextWrapper>
  )

  expect(await screen.queryByText("User Signed In", {})).toBeNull()
})
