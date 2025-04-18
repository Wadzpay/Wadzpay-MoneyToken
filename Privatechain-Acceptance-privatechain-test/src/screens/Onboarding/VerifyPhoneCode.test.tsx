import React from "react"
import { fireEvent, screen, waitFor } from "@testing-library/react"
import { MemoryRouter } from "react-router-dom"
import { OnboardingContextWrapper, renderWithQueryClient } from "src/testUtils"
import { RouteType } from "src/constants/routeTypes"

import VerifyPhoneCode from "./VerifyPhoneCode"

const renderWrapper = (element: JSX.Element, phoneNumber?: string) =>
  renderWithQueryClient(
    <OnboardingContextWrapper phoneNumber={phoneNumber}>
      <MemoryRouter>{element}</MemoryRouter>
    </OnboardingContextWrapper>
  )

const mockedNavigate = jest.fn()

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate
}))

test("redirect to create account if phone number is missing", async () => {
  renderWrapper(<VerifyPhoneCode />)

  expect(mockedNavigate).toHaveBeenCalledWith(RouteType.CREATE_ACCOUNT)
})

test("form displayed if phone number is present", async () => {
  renderWrapper(<VerifyPhoneCode />, "+61412345678")

  expect(
    await waitFor(() => screen.getByText("Verify Phone Number"))
  ).toBeInTheDocument()
})

test("prev button takes user to the phone number page", async () => {
  renderWrapper(<VerifyPhoneCode />, "+61412345678")

  const prevButton = screen.getByRole("button", { name: "Back" })
  expect(prevButton).toBeInTheDocument()

  fireEvent.click(prevButton)

  expect(mockedNavigate).toHaveBeenCalledWith(RouteType.CREATE_ACCOUNT)
})
