import React from "react"
import { fireEvent, screen, waitFor } from "@testing-library/react"

import { renderWithQueryClient } from "../../testUtils"
import ResetSuccess from "./ResetSuccess"

const mockedNavigate = jest.fn()
jest.mock("react-router-dom", () => {
  const originalRouterDom = jest.requireActual("react-router-dom")
  return {
    __esModule: true,
    ...originalRouterDom,
    useNavigate: () => mockedNavigate
  }
})

/*test("renders success page", () => {
  renderWithQueryClient(<ResetSuccess />)

  const linkElement = screen.getByRole("heading", { level: 2 })
  expect(linkElement.innerHTML).toBe("Password reset successful")
})*/

test("link to sign in page", async () => {
  const wrapper = renderWithQueryClient(<ResetSuccess />)

  const submitButton = wrapper.getByRole("button")

  await waitFor(() => {
    fireEvent.click(submitButton)
  })
  // Check that the mocked navigate function was called
  expect(mockedNavigate).toHaveBeenCalledTimes(1)
})
