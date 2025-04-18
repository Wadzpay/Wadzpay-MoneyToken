import React from "react"
import { render, screen } from "@testing-library/react"
import { MerchantContextWrapper, UserContextWrapper } from "src/testUtils"
import { Auth } from "aws-amplify"
import MerchantName from "src/components/ui/MerchantName"

test("merchant details returned if user, merchant", async () => {
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
      <MerchantContextWrapper
        merchantDetails={{
          merchant: {
            name: "Test Merchant",
            countryOfRegistration: "test",
            registrationCode: "test",
            primaryContactFullName: "test",
            primaryContactEmail: "test",
            companyType: "test",
            industryType: "test",
            primaryContactPhoneNumber: "test",
            merchantId: "test",
            defaultRefundableFiatValue: 500,
            tnc:"Terms And Condition"
          },
          role: "test"
        }}
      >
        <MerchantName />
      </MerchantContextWrapper>
    </UserContextWrapper>
  )

  expect(
    await screen.findByText("Test Merchant", {}, { timeout: 2000 })
  ).toBeInTheDocument()
})

test("merchant details not returned if user, no merchant", async () => {
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
      <MerchantContextWrapper>
        <MerchantName />
      </MerchantContextWrapper>
    </UserContextWrapper>
  )

  expect(await screen.queryByText("Test Merchant", {})).toBeNull()
})

test("merchant details not returned if no user", async () => {
  Auth.currentAuthenticatedUser = jest.fn().mockImplementation(
    () =>
      new Promise(() => {
        return true
      })
  )

  render(
    <UserContextWrapper>
      <MerchantContextWrapper>
        <MerchantName />
      </MerchantContextWrapper>
    </UserContextWrapper>
  )

  expect(await screen.queryByText("Test Merchant", {})).toBeNull()
})
